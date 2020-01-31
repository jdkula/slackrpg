package pw.jonak.slackrpg.healthbot.actions

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.util.pipeline.PipelineContext
import io.ktor.response.respond
import pw.jonak.slackrpg.healthbot.sql.Characters
import pw.jonak.slackrpg.slack.SlashCommand
import pw.jonak.slackrpg.slack.ephemeralMessage

suspend fun PipelineContext<Unit, ApplicationCall>.unregister(command: SlashCommand) {
    val request = command.text.trim()
    val format = Regex("(.*)?")
    val target = format.find(request)?.groups?.get(1)?.value

    val targetCharacter = Characters[target, command.userId]

    if (targetCharacter == null || targetCharacter.user != command.userId) {
        ephemeralMessage {
            channel = command.channelId
            user = command.userId

            attachment {
                fallback = if (targetCharacter == null) {
                    "I couldn't find the character you're trying to unregister, or you haven't registered your own character yet."
                } else {
                    "The character you're trying to unregister doesn't belong to you."
                }
                color = "#FF0000"
                text = fallback
            }

            send()
        }
        call.respond(HttpStatusCode.OK)
        return
    }

    Characters -= targetCharacter

    ephemeralMessage {
        channel = command.channelId
        user = command.userId

        attachment {
            fallback = "${targetCharacter.characterName} has been deleted."
            text = fallback
            color = "#0000FF"
        }

        send()
    }

    call.respond(HttpStatusCode.OK)

}
