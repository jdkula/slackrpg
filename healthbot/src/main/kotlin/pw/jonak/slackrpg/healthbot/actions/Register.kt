package pw.jonak.slackrpg.healthbot.actions

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.util.pipeline.PipelineContext
import io.ktor.response.respond
import pw.jonak.slackrpg.healthbot.characterInfo
import pw.jonak.slackrpg.healthbot.sql.Character
import pw.jonak.slackrpg.healthbot.sql.Characters
import pw.jonak.slackrpg.slack.SlashCommand
import pw.jonak.slackrpg.slack.ephemeralMessage

suspend fun PipelineContext<Unit, ApplicationCall>.register(command: SlashCommand) {
    var request = command.text.trim()

    val hidden = "@hidden" in request
    request = request.replaceFirst("@hidden", "")

    val npcPattern = Regex("@([^\\s]+)")
    val npcMatch = npcPattern.find(request)
    val npcShortName = npcMatch?.groups?.get(1)?.value
    if (npcShortName != null) request = request.replace(npcPattern, "").trim()

    val currentHealthPattern = Regex("#(\\d+)")
    val currentHealthMatch = currentHealthPattern.find(request)
    var currentHealth = currentHealthMatch?.groups?.get(1)?.value?.toIntOrNull()
    if (currentHealth != null) request = request.replace(currentHealthPattern, "").trim()

    val restPattern = Regex("^(.*?) +(?<!#)(\\d+)(?: +#<(.*?)(?:\\|.*?)?>)?$")
    val restMatch = restPattern.find(request)
    val characterName = restMatch?.groups?.get(1)?.value
    val maxHealth = restMatch?.groups?.get(2)?.value?.toIntOrNull()
    val url = restMatch?.groups?.get(3)?.value

    if (characterName == null || maxHealth == null) {
        ephemeralMessage {
            channel = command.channelId
            user = command.userId

            attachment {
                fallback = "Couldn't get character name or max health, which are required!"
                color = "#FF0000"
                text = fallback
            }
            send()
        }
        call.respond(HttpStatusCode.OK)
        return
    }

    val tempHealth: Int

    if (currentHealth != null && currentHealth > maxHealth) {
        tempHealth = currentHealth - maxHealth
        currentHealth = maxHealth
    } else {
        tempHealth = 0
    }

    val newCharacter = Character(
        command.userId,
        npcShortName ?: command.userId,
        characterName,
        maxHealth,
        Integer.min(currentHealth ?: maxHealth, maxHealth),
        tempHealth,
        url,
        hidden
    )

    val oldCharacter = Characters[npcShortName ?: command.userId]

    ephemeralMessage {
        channel = command.channelId
        user = command.userId

        if (oldCharacter != null && oldCharacter.user != command.userId) {  // Error: Shortname Taken
            attachment {
                fallback =
                        "That character shortname is already taken by somebody else. Pick a different one."
                color = "#FF0000"
                pretext = fallback
            }
        } else {  // Success
            characterInfo(newCharacter, overrideHidden = true) {
                if (oldCharacter != null) {  // Character Update
                    fallback = "Your character has been updated!"
                    color = "#0000FF"
                } else {  // Character Registration
                    fallback = "Your character has been registered!"
                    color = "#00FF77"

                }
                pretext = fallback
            }
        }
        send()
    }

    Characters += newCharacter
    call.respond(HttpStatusCode.OK)
}