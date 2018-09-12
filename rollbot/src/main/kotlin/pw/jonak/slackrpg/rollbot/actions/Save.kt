package pw.jonak.slackrpg.rollbot.actions

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.pipeline.PipelineContext
import io.ktor.response.respond
import pw.jonak.slackrpg.rollbot.sql.Macro
import pw.jonak.slackrpg.rollbot.sql.Macros
import pw.jonak.slackrpg.slack.SlashCommand
import pw.jonak.slackrpg.slack.attachment
import pw.jonak.slackrpg.slack.ephemeralMessage
import pw.jonak.slackrpg.slack.send

suspend fun PipelineContext<Unit, ApplicationCall>.save(command: SlashCommand) {
    val macro = Macro.parse(command.command)
    val roll = macro.rollInfo

    val responseText: String
    val color: String

    if (Macros[command.userId, macro.macroName] != null) {
        responseText =
                """Okay! We updated the macro `${macro.macroName}`. It's now linked to `${roll.roll}`, which I'll call "${roll.description}.""""
        color = "#0000FF"
    } else {
        responseText =
                """Got it. `${macro.macroName}` is now linked to `${roll.roll}`. I'll describe it as "${roll.description}" to the other players."""
        color = "#00FF00"
    }
    Macros[command.userId, macro.macroName] = roll

    send {
        ephemeralMessage {
            channel = command.channelId
            user = command.userId

            attachment {
                fallback = responseText
                this.color = color
                text = responseText
            }
        }
    }

    call.respond(HttpStatusCode.OK)
}