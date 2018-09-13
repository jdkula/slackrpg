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
