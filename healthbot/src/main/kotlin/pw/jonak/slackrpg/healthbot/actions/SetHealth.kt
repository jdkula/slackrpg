package pw.jonak.slackrpg.healthbot.actions

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.pipeline.PipelineContext
import io.ktor.response.respond
import pw.jonak.slackrpg.healthbot.characterInfo
import pw.jonak.slackrpg.healthbot.sql.Characters
import pw.jonak.slackrpg.slack.*

suspend fun PipelineContext<Unit, ApplicationCall>.setHealth(command: SlashCommand, max: Boolean = false) {
    val request = command.text.trim()
    val format = Regex("(.*?)? ?((?:-?\\d+)|@max)")
    val target = format.find(request)?.groups?.get(1)?.value
    val newHealthOption = format.find(request)?.groups?.get(2)?.value


    val targetCharacter = Characters[target, command.userId]

    val newHealth = if(newHealthOption == "@max") {
        targetCharacter?.maxHealth
    } else {
        newHealthOption?.toIntOrNull()
    }

    if (targetCharacter == null || newHealth == null) {
        send {
            ephemeralMessage {
                channel = command.channelId
                user = command.userId

                attachment {
                    fallback =
                            "I couldn't find the character you're trying to set the health of, or you haven't registered your own character yet."
                    color = "#FF0000"
                    text = fallback
                }
            }
        }
        call.respond(HttpStatusCode.OK)
        return
    }

    if(targetCharacter.user != command.userId) {
        send {
            ephemeralMessage {
                channel = command.channelId
                user = command.userId

                attachment {
                    fallback = "You didn't register the character whose health you're trying to set!"
                    color = "#FF0000"
                    text = fallback
                }
            }
        }
        call.respond(HttpStatusCode.OK)
        return
    }

    val tempHealth: Int
    val adjustedNewHealth: Int

    if(newHealth > targetCharacter.maxHealth && !max) {
        tempHealth = newHealth - targetCharacter.maxHealth
        adjustedNewHealth = targetCharacter.maxHealth
    } else {
        tempHealth = 0
        adjustedNewHealth = newHealth
    }
    if (max) {
        Characters.updateMaxHealth(targetCharacter, adjustedNewHealth)!!
    }

    val updated = Characters.updateHealth(targetCharacter, adjustedNewHealth, tempHealth)!!

    if(targetCharacter.hidden) {
        send {
            ephemeralMessage {
                channel = command.channelId
                user = command.userId

                attachment {
                    if (max) {
                        fallback = "${targetCharacter.characterName}'s max HP is now $adjustedNewHealth."
                        color = "#FF00FF"
                    } else {
                        fallback = "${targetCharacter.characterName} now has $adjustedNewHealth HP."
                        color = "#0000FF"
                    }
                    text = fallback
                }

                if (!updated.hidden) {
                    characterInfo(updated)
                }
            }
        }
    } else {
        send {
            message {
                channel = command.channelId

                attachment {
                    if (max) {
                        fallback = "${targetCharacter.characterName}'s max HP is now $adjustedNewHealth."
                        color = "#FF00FF"
                    } else {
                        fallback = "${targetCharacter.characterName} now has $adjustedNewHealth HP."
                        color = "#0000FF"
                    }
                    text = fallback
                }

                if (!updated.hidden) {
                    characterInfo(updated)
                }
            }
        }
    }

    call.respond(HttpStatusCode.OK)
}
