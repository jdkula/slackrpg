package pw.jonak.slackrpg.rollbot.actions

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.pipeline.PipelineContext
import io.ktor.response.respond
import pw.jonak.slackrpg.rollbot.sql.Macro
import pw.jonak.slackrpg.rollbot.sql.Macros
import pw.jonak.slackrpg.slack.SlashCommand
import pw.jonak.slackrpg.slack.ephemeralMessage

suspend fun PipelineContext<Unit, ApplicationCall>.save(command: SlashCommand) {
    val macro = Macro.parse(command.command)
    val roll = macro.rollInfo

    Macros[command.userId, macro.macroName] = roll

    ephemeralMessage {
        channel = command.channelId
        user = command.userId

        attachment {
            if (Macros[command.userId, macro.macroName] != null) {  // Updated
                fallback =
                        """Okay! We updated the macro `${macro.macroName}`. It's now linked to `${roll.roll}`, which I'll call "${roll.description}.""""
                color = "#0000FF"
            } else {  // New
                fallback =
                        """Got it. `${macro.macroName}` is now linked to `${roll.roll}`. I'll describe it as "${roll.description}" to the other players."""
                color = "#00FF00"
            }

            text = fallback
        }
        send()
    }

    call.respond(HttpStatusCode.OK)
}