package pw.jonak.slackrpg.rollbot

import com.beust.klaxon.Klaxon
import io.ktor.application.*
import io.ktor.content.default
import io.ktor.content.files
import io.ktor.content.static
import io.ktor.content.staticRootFolder
import io.ktor.features.CORS
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.pipeline.PipelineContext
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import pw.jonak.slackrpg.rollbot.actions.*
import pw.jonak.slackrpg.rollbot.sql.Macros
import pw.jonak.slackrpg.slack.*
import sun.awt.SunToolkit
import java.io.File
import java.security.InvalidParameterException
import java.sql.Connection

const val MAX_ITERATION = 999
const val ROLL_MAX_N = 500


fun Application.main() {
    Users.initialize()
    Database.connect("jdbc:sqlite:macros.db", "org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

    transaction {
        create(Macros)
    }

    install(CORS) {
        anyHost()
    }
    routing {
        get("/hello") {
            call.respond("Hello World!")
        }

        post("/dice") {
            val post = call.receiveParameters()
            val roll = post["roll"]?.replace(" ", "")
            if (roll == null) {
                context.respondText("Bad Params", ContentType.Text.Plain, HttpStatusCode.BadRequest)
            } else {
                try {
                    val rolls = Roll.parse(roll)
                    context.respondText(Klaxon().toJsonString(rolls), ContentType.Application.Json)
                } catch (e: NumberFormatException) {
                    context.respondText("Bad Dice Format", ContentType.Text.Plain, HttpStatusCode.BadRequest)
                } catch (e: SunToolkit.InfiniteLoop) {
                    context.respondText(
                        "Roll Caused Infinite Loop",
                        ContentType.Text.Plain,
                        HttpStatusCode.BadRequest
                    )
                } catch (e: InvalidParameterException) {
                    context.respondText(
                        "Number of rolls too big! (max: $ROLL_MAX_N)",
                        ContentType.Text.Plain,
                        HttpStatusCode.BadRequest
                    )
                }
            }
        }
        post("slack") {
            val command = SlashCommand.from(call.receiveParameters())
            if (command == null) {
                call.respond(HttpStatusCode.BadRequest, "Couldn't parse slash command...")
                return@post
            }

            when (command.command) {
                "/r", "/roll", "/sr", "/sroll" -> roll(command)
                "/save" -> save(command)
                "/delete" -> delete(command)
                "/mr", "/smr", "/mroll", "/smroll" -> macroRoll(command)
                "/rollhelp" -> help(command)
            }
        }

        static {
            staticRootFolder = File("./src/main/resources")
            files(".")
            static("css") {
                staticRootFolder = File("./src/main/resources")
                files("css")
            }
            static("js") {
                staticRootFolder = File("./src/main/resources")
                files("js")
            }
            static("fonts") {
                staticRootFolder = File("./src/main/resources")
                files("fonts")
            }
            default("index.html")
        }
    }
}


suspend fun PipelineContext<Unit, ApplicationCall>.doRoll(
    command: SlashCommand,
    rollInfo: RollInfo,
    secret: Boolean = false
) {
    try {
        val rolls = Roll.parse(rollInfo.roll)
        var text = rollInfo.description
        if (!secret) {
            val userName = Users[command.userId]?.profile?.real_name
            text = "_${userName}_"
            if (rollInfo.description.isNotEmpty()) {
                text += ": ${rollInfo.description}"
            }
        }
        if (rolls.size > 1 && !rollInfo.noSum) {
            text += "\n*Total: ${rolls.sumBy { it.sum() }}*"
        }
        val attachments = rolls
            .map {
                val modifier =
                    if (it.modifier > 0) " + ${it.modifier}" else if (it.modifier < 0) " - ${-it.modifier}" else ""
                val fallback_text = "${it.request}: ${it.results.joinToString(" + ")}$modifier = ${it.sum()}"
                val text = it.results.joinToString(" + ") { result ->
                    when (result.dropped) {
                        true -> "~${result.roll}~"
                        false -> "${result.roll}"
                    }
                }
                val percent_success = ((1.0 * it.sum() / it.maxPossible()) * 100).toInt()
                val color = when (it.sum()) {
                    it.maxPossible() -> "#00FF00"
                    it.minPossible() -> "#FF0000"
                    else -> when (percent_success) {
                        in 1..30 -> "#FFA500"
                        in 31..50 -> "#FFFF00"
                        in 51..70 -> "#00abff"
                        in 71..90 -> "#00f9ff"
                        else -> "#008000"
                    }
                }
                Attachment(
                    fallback = "${it.request}: $fallback_text",
                    text = text,
                    color = color,
                    title = "${it.request} = ${it.sum()}",
                    mrkdwn_in = listOf("text", "title")
                )
            }

        val message = Message(
            command.channelId,
            attachments = attachments,
            text = text
        )
        if (secret) {
            EphemeralMessage(command.userId, message).send()
        } else {
            message.send()
        }
        context.respond(HttpStatusCode.OK)
    } catch (e: NumberFormatException) {
        EphemeralMessage(
            channel = command.channelId,
            user = command.userId,
            text = "I couldn't understand what you wrote."
        ).send()
        context.respond(HttpStatusCode.OK)
    } catch (e: SunToolkit.InfiniteLoop) {
        context.respondText("Roll Caused Infinite Loop", ContentType.Text.Plain, HttpStatusCode.BadRequest)
    } catch (e: InvalidParameterException) {
        EphemeralMessage(
            channel = command.channelId,
            user = command.userId,
            text = "The limit on number of dice rolled is 500, for Slack's sanity."
        ).send()
        context.respond(HttpStatusCode.OK)
    }
}