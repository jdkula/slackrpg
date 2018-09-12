package pw.jonak.slackrpg.rollbot.actions

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.pipeline.PipelineContext
import io.ktor.response.respond
import pw.jonak.slackrpg.rollbot.sql.Macros
import pw.jonak.slackrpg.slack.SlashCommand
import pw.jonak.slackrpg.slack.attachment
import pw.jonak.slackrpg.slack.ephemeralMessage
import pw.jonak.slackrpg.slack.send

suspend fun PipelineContext<Unit, ApplicationCall>.delete(command: SlashCommand) {
    val macroName = command.text.trim()

    if (Macros[command.userId, macroName] == null) {
        val text = "It doesn't look like the macro $macroName exists."
        send {
            ephemeralMessage {
                channel = command.channelId
                user = command.userId

                attachment {
                    fallback = text
                    color = "#FF0000"
                    this.text = text
                }
            }
        }
    } else {
        Macros.delete(command.userId, macroName)
        send {
            ephemeralMessage {
                channel = command.channelId
                user = command.userId

                attachment {
                    fallback = "$macroName has been deleted."
                    color = "#800080"
                    text = "$macroName has been deleted."
                }
            }
        }
    }

    call.respond(HttpStatusCode.OK)
}