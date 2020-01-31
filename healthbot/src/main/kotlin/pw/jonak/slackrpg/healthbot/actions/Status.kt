package pw.jonak.slackrpg.healthbot.actions

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.util.pipeline.PipelineContext
import io.ktor.response.respond
import pw.jonak.slackrpg.healthbot.characterInfo
import pw.jonak.slackrpg.healthbot.sql.Character
import pw.jonak.slackrpg.healthbot.sql.Characters
import pw.jonak.slackrpg.healthbot.sql.parseUser
import pw.jonak.slackrpg.slack.*

suspend fun PipelineContext<Unit, ApplicationCall>.info(command: SlashCommand) {
    var request = command.text.trim()
    val broadcast = "@broadcast" in request
    if (broadcast) request = request.replace("@broadcast", "").trim()

    val selector: (Character) -> Boolean = when (request) {
        "all" -> { _ -> true }
        "mine", "my", "self", "" -> { character -> character.user == command.userId }
        else -> {
            val cached = parseUser(request, command.userId);
            { character -> character.characterId == cached }
        }
    }

    val characters = Characters.getAll(selector, command.userId, !broadcast)

    message {
        text = "*Status Update!*"
        channel = command.channelId

        if (characters.isEmpty()) {
            attachment {
                fallback = "I couldn't find any characters matching your search!"
                text = fallback
            }
        } else {
            characters.forEach {
                characterInfo(it, overrideHidden = !broadcast && (it.user == command.userId))
            }
        }
        if (broadcast) {
            send()
        } else {
            sendEphemeral(command.userId)
        }
    }

    call.respond(HttpStatusCode.OK)
}