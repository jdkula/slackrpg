package pw.jonak.slackrpg.rollbot.actions

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext
import pw.jonak.slackrpg.rollbot.sql.Macros
import pw.jonak.slackrpg.slack.SlashCommand
import pw.jonak.slackrpg.slack.ephemeralMessage

suspend fun PipelineContext<Unit, ApplicationCall>.delete(command: SlashCommand) {
    val macroName = command.text.trim()

    ephemeralMessage {
        channel = command.channelId
        user = command.userId

        attachment {
            if (Macros[command.userId, macroName] == null) {  // Fail case
                fallback = "It doesn't look like the macro $macroName exists."
                color = "#FF0000"
            } else {  // Success case
                Macros.delete(command.userId, macroName)
                fallback = "$macroName has been deleted."
                color = "#800080"
            }
            text = fallback
        }
        send()
    }

    call.respond(HttpStatusCode.OK)
}