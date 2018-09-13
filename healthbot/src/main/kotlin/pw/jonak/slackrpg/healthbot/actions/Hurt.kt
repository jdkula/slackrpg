package pw.jonak.slackrpg.healthbot.actions

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.pipeline.PipelineContext
import io.ktor.response.respond
import pw.jonak.slackrpg.healthbot.characterInfo
import pw.jonak.slackrpg.healthbot.sql.Characters
import pw.jonak.slackrpg.slack.*
import java.lang.Integer.max

suspend fun PipelineContext<Unit, ApplicationCall>.hurt(command: SlashCommand) {
    val request = command.text.trim()
    val format = Regex("^(.+?) (\\d+) ?(.+?)?$")
    val match = format.find(request)

    val target = match?.groups?.get(1)?.value?.trim()
    val damage = match?.groups?.get(2)?.value?.trim()?.toIntOrNull()
    val enactor = match?.groups?.get(3)?.value?.trim()

    val targetCharacter = Characters[target]

    val fromCharacter = Characters[enactor, command.userId]

    if (targetCharacter == null || damage == null || fromCharacter == null) { // Error case
        ephemeralMessage {
            user = command.userId
            channel = command.channelId

            attachment {
                fallback = when {
                    damage == null -> "I couldn't understand what you're trying to get me to do."
                    targetCharacter == null -> "I couldn't find your target!"
                    else -> "I couldn't find who's attacking... either register a character, or specify who's attacking."
                }
                text = fallback
                color = "#FF0000"
            }
            send()
        }
        call.respond(HttpStatusCode.OK)
        return
    }

    val damageDone = max(damage - targetCharacter.tempHealth, 0)
    val newTempHealth = max(targetCharacter.tempHealth - damage, 0)

    val updated = Characters.updateHealth(
        targetCharacter,
        Integer.max(targetCharacter.currentHealth - damageDone, -targetCharacter.maxHealth),
        newTempHealth
    )!!

    val targetName = if (fromCharacter == targetCharacter) "themself" else targetCharacter.characterName

    message {
        channel = command.channelId
        icon_url = fromCharacter.characterImage
        username = fromCharacter.characterName

        attachment {
            fallback = "${fromCharacter.characterName} attacks $targetName for $damage damage!"
            text = fallback

            color = "#FF0000"
        }

        if (!updated.hidden) {
            characterInfo(updated)
        }

        send()
    }

    call.respond(HttpStatusCode.OK)
}