package pw.jonak.slackrpg.rollbot.actions

import InfiniteLoop
import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.util.pipeline.PipelineContext
import io.ktor.response.respond
import io.ktor.response.respondText
import pw.jonak.slackrpg.rollbot.Roll
import pw.jonak.slackrpg.rollbot.RollInfo
import pw.jonak.slackrpg.slack.*
import java.security.InvalidParameterException

suspend fun PipelineContext<Unit, ApplicationCall>.roll(command: SlashCommand) {
    val rollInfo = RollInfo.parse(command.text)
    doRoll(command, rollInfo, secret = command.text.startsWith("/sr"))
}

suspend fun PipelineContext<Unit, ApplicationCall>.doRoll(
    command: SlashCommand,
    rollInfo: RollInfo,
    secret: Boolean = false
) {
    try {
        val rolls = Roll.parse(rollInfo.roll)
        val userName = Users[command.userId]?.profile?.real_name


        message {
            channel = command.channelId

            text = if (!secret) {
                "_${userName}_" + if (rollInfo.description.isNotEmpty()) {
                    ": ${rollInfo.description}"
                } else ""
            } else {
                rollInfo.description
            }

            if (rolls.size > 1 && !rollInfo.noSum) {
                text += "\n*Total: ${rolls.sumBy { it.sum() }}*"
            }

            rolls.forEach { roll ->
                attachment {
                    val modifier =
                        if (roll.modifier > 0) " + ${roll.modifier}" else if (roll.modifier < 0) " - ${-roll.modifier}" else ""

                    fallback = "${roll.request}: ${roll.results.joinToString(" + ")}$modifier = ${roll.sum()}"

                    text = roll.results.joinToString(" + ") { result ->
                        when (result.dropped) {
                            true -> "~${result.roll}~"
                            false -> "${result.roll}"
                        }
                    }

                    val percent_success = ((1.0 * roll.sum() / roll.maxPossible()) * 100).toInt()
                    color = when (roll.sum()) {
                        roll.maxPossible() -> "#00FF00"
                        roll.minPossible() -> "#FF0000"
                        else -> when (percent_success) {
                            in 1..30 -> "#FFA500"
                            in 31..50 -> "#FFFF00"
                            in 51..70 -> "#00abff"
                            in 71..90 -> "#00f9ff"
                            else -> "#008000"
                        }
                    }

                    title = if (roll.description != null) {
                        "${roll.description} (${roll.request}) = ${roll.sum()}"
                    } else {
                        "${roll.request} = ${roll.sum()}"
                    }

                    mrkdwn_in = listOf("text", "title")
                }
            }


            if (secret) {
                sendEphemeral(command.userId)
            } else {
                send()
            }
        }
        context.respond(HttpStatusCode.OK)
    } catch (e: NumberFormatException) {
        ephemeralMessage {
            channel = command.channelId
            user = command.userId
            text = "I couldn't understand what you wrote."

            send()
        }
        context.respond(HttpStatusCode.OK)
    } catch (e: InfiniteLoop) {
        context.respondText("Roll Caused Infinite Loop", ContentType.Text.Plain, HttpStatusCode.BadRequest)
    } catch (e: InvalidParameterException) {
        ephemeralMessage {
            channel = command.channelId
            user = command.userId
            text = "The limit on number of dice rolled is 500, for Slack's sanity."

            send()
        }
        context.respond(HttpStatusCode.OK)
    }
}