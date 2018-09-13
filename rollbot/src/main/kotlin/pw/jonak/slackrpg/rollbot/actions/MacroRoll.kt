package pw.jonak.slackrpg.rollbot.actions

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.pipeline.PipelineContext
import io.ktor.response.respond
import pw.jonak.slackrpg.rollbot.doRoll
import pw.jonak.slackrpg.rollbot.sql.Macros
import pw.jonak.slackrpg.slack.SlashCommand
import pw.jonak.slackrpg.slack.ephemeralMessage

suspend fun PipelineContext<Unit, ApplicationCall>.macroRoll(command: SlashCommand) {
    val macroName = command.text.trim()
    val macro = Macros[command.userId, macroName]

    if (macro == null) {
        ephemeralMessage {
            user = command.userId
            channel = command.channelId

            attachment {
                fallback = "I couldn't find that macro!"
                color = "#FF0000"
                text = "I couldn't find that macro!"
            }
            send()
        }
        context.respond(HttpStatusCode.OK)
    } else {
        doRoll(command, macro.rollInfo, command.command.startsWith("/smr"))
    }
    call.respond(HttpStatusCode.OK)
}