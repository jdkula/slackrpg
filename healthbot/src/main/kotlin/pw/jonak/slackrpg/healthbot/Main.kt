package pw.jonak.slackrpg.healthbot

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import pw.jonak.slackrpg.healthbot.actions.*
import pw.jonak.slackrpg.healthbot.sql.Character
import pw.jonak.slackrpg.healthbot.sql.Characters
import pw.jonak.slackrpg.slack.*
import java.sql.Connection

fun Application.main() {
    Users.initialize()
    Database.connect("jdbc:sqlite:health.db", "org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

    transaction {
        SchemaUtils.create(Characters)
    }

    install(CORS) {
        anyHost()
    }
    routing {
        post("slack") {
            val command = SlashCommand.from(call.receiveParameters())
            if (command == null) {
                call.respond(HttpStatusCode.BadRequest, "Couldn't parse slash command...")
                return@post
            }

            when (command.command) {
                "/register" -> register(command)
                "/hurt" -> hurt(command)
                "/heal" -> heal(command)
                "/sethealth" -> setHealth(command)
                "/setmaxhealth" -> setHealth(command, max = true)
                "/unregister" -> unregister(command)
                "/info" -> info(command)
                "/healthhelp" -> help(command)
            }
        }
    }
}

fun IMessageBuilder.characterInfo(
    character: Character,
    overrideHidden: Boolean = false,
    configure: (AttachmentBuilder.() -> Unit)? = null
) {
    attachment {
        thumb_url = character.characterImage
        title = character.characterName

        if (character.hidden && overrideHidden) {
            text = "_${character.characterName} is hidden from normal character overviews._"
        }

        if (character.characterId != character.user) {
            title += " (@${character.characterId})"
        }

        fallback = title!!

        val author = Users[character.user]?.profile

        author_icon = author?.image_32
        if (author != null) {
            author_name = "<@${character.user}|${author.display_name}>"
        }

        if (!character.hidden || overrideHidden) {
            field {
                short = true
                title = "Max Health"
                value = "${character.maxHealth}"
            }
            field {
                short = true
                title = "Current Health"
                value = "${character.currentHealth}"
            }
            if(character.tempHealth > 0) {
                field {
                    short = true
                    title = "Temporary Health"
                    value = "${character.tempHealth}"
                }
            }

            fallback += " - ${character.currentHealth}/${character.maxHealth}"
        }

        mrkdwn_in = listOf("text")

        color = when {
            character.hidden && !overrideHidden -> null
            character.currentHealth == character.maxHealth -> "#00ABFF"
            character.currentHealth <= 0 -> "#000000"
            else -> Color.lerp(Color.RED, Color.GREEN, 1.0 * character.currentHealth / character.maxHealth).asHex()
        }

        configure?.invoke(this)
    }
}

class InvalidArgumentException(argumentName: String) : Exception(argumentName)

class Color(val red: Int, val green: Int, val blue: Int) {
    constructor(hex: String) : this(
        hex.trim('#').substring(0..1).toInt(16),
        hex.trim('#').substring(2..3).toInt(16),
        hex.trim('#').substring(4..5).toInt(16)
    )

    init {
        if (red !in 0..255) throw InvalidArgumentException("red")
        if (green !in 0..255) throw InvalidArgumentException("green")
        if(blue !in 0..255) throw InvalidArgumentException("blue")
    }

    fun asHex(): String {
        return String.format("%02X%02X%02X", red, green, blue)
    }

    companion object {
        val RED = Color(0xFF, 0x0, 0x0)
        val GREEN = Color(0x0, 0xFF, 0x0)
        val BLUE = Color(0x0, 0x0, 0xFF)

        fun lerp(a: Color, b: Color, percent: Double): Color {
            return Color(
                a.red + ((b.red - a.red) * percent).toInt(),
                a.green + ((b.green - a.green) * percent).toInt(),
                a.blue + ((b.blue - a.blue) * percent).toInt()
            )
        }
    }
}