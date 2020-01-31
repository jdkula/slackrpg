package pw.jonak.slackrpg.healthbot.actions

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.util.pipeline.PipelineContext
import io.ktor.response.respond
import pw.jonak.slackrpg.healthbot.characterInfo
import pw.jonak.slackrpg.healthbot.sql.Characters
import pw.jonak.slackrpg.slack.*

suspend fun PipelineContext<Unit, ApplicationCall>.heal(command: SlashCommand) {
    val request = command.text.trim()
    val format = Regex("^(.+?) (\\d+) ?(.+?)?$")
    val match = format.find(request)

    val target = match?.groups?.get(1)?.value?.trim()
    val heal = match?.groups?.get(2)?.value?.trim()?.toIntOrNull()
    val enactor = match?.groups?.get(3)?.value?.trim()

    if (target == null || heal == null) {
        ephemeralMessage {
            user = command.userId
            channel = command.channelId

            attachment {
                fallback = "I couldn't understand what you're trying to get me to do."
                text = fallback
                color = "#FF0000"
            }

            send()
        }
        call.respond(HttpStatusCode.OK)
        return
    }

    val targetCharacter = Characters[target]

    if (targetCharacter == null) {
        ephemeralMessage {
            user = command.userId
            channel = command.channelId

            attachment {
                fallback = "I couldn't find your target!"
                text = fallback
                color = "#FF0000"
            }
            send()
        }
        call.respond(HttpStatusCode.OK)
        return
    }

    val fromCharacter = Characters[enactor, command.userId]

    if (fromCharacter == null) {
        ephemeralMessage {
            user = command.userId
            channel = command.channelId

            attachment {
                fallback =
                        "I couldn't find who's healing... either register a character, or specify who's attacking."
                text = fallback
                color = "#FF0000"
            }
            send()
        }
        call.respond(HttpStatusCode.OK)
        return
    }

    var newHealth: Int = targetCharacter.currentHealth + heal
    var newTempHealth: Int = targetCharacter.tempHealth

    if (targetCharacter.currentHealth + heal > targetCharacter.maxHealth) {
        newHealth = targetCharacter.maxHealth
        newTempHealth = (targetCharacter.currentHealth + heal) - targetCharacter.maxHealth + targetCharacter.tempHealth
    }

    val updated = Characters.updateHealth(targetCharacter, newHealth, newTempHealth)!!

    val targetName = if (targetCharacter == fromCharacter) "themself" else targetCharacter.characterName

    message {
        channel = command.channelId
        icon_url = fromCharacter.characterImage
        username = fromCharacter.characterName

        attachment {
            fallback = "${fromCharacter.characterName} heals $targetName for $heal health!"
            text = fallback

            color = "#00FF00"
        }

        if (!updated.hidden) {
            characterInfo(updated)
        }

        send()
    }
    call.respond(HttpStatusCode.OK)
}
