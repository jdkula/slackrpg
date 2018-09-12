package pw.jonak.slackrpg.rollbot.actions

import io.ktor.application.ApplicationCall
import io.ktor.pipeline.PipelineContext
import pw.jonak.slackrpg.rollbot.RollInfo
import pw.jonak.slackrpg.rollbot.doRoll
import pw.jonak.slackrpg.slack.SlashCommand

suspend fun PipelineContext<Unit, ApplicationCall>.roll(command: SlashCommand) {
    val rollInfo = RollInfo.parse(command.command)
    doRoll(command, rollInfo, secret = command.command.startsWith("/sr"))
}