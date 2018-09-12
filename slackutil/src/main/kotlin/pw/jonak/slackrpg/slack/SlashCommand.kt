package pw.jonak.slackrpg.slack

import io.ktor.http.Parameters

data class SlashCommand(
    val token: String,
    val teamId: String,
    val teamDomain: String,
    val enterpriseId: String?,
    val enterpriseName: String?,
    val channelId: String,
    val channelName: String,
    val userId: String,
    val userName: String,
    val command: String,
    val text: String,
    val responseUrl: String,
    val triggerId: String?
) {
    companion object {
        fun from(urlEncoded: Parameters): SlashCommand? {
            return try {
                SlashCommand(
                    urlEncoded["token"]!!,
                    urlEncoded["team_id"]!!,
                    urlEncoded["team_domain"]!!,
                    urlEncoded["enterprise_id"],
                    urlEncoded["enterprise_name"],
                    urlEncoded["channel_id"]!!,
                    urlEncoded["channel_name"]!!,
                    urlEncoded["user_id"]!!,
                    urlEncoded["user_name"]!!,
                    urlEncoded["command"]!!,
                    urlEncoded["text"]!!,
                    urlEncoded["response_url"]!!,
                    urlEncoded["trigger_id"]
                )
            } catch (e: KotlinNullPointerException) {
                null
            }
        }
    }
}