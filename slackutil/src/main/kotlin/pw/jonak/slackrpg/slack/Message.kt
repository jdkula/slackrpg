package pw.jonak.slackrpg.slack

import com.beust.klaxon.Klaxon
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType

interface IMessage {
    suspend fun send()
}

data class EphemeralMessage(
    val channel: String,
    val user: String,
    val text: String? = null,
    val as_user: Boolean? = null,
    val attachments: List<Attachment>? = null,
    val link_names: Boolean? = null,
    val parse: String? = null,
    val thread_ts: String? = null
) : IMessage {
    override suspend fun send() {
        val client = HttpClient(Apache)
        val response = client.post<String>("https://slack.com/api/chat.postEphemeral") {
            header("Authorization", "Bearer " + System.getenv("SLACK_OAUTH"))
            contentType(ContentType.Application.Json)
            body = Klaxon().toJsonString(this@EphemeralMessage)
        }
    }

    constructor(user: String, message: Message) : this(
        message.channel,
        user,
        message.text,
        message.as_user,
        message.attachments,
        message.link_names,
        message.parse,
        message.thread_ts
    )
}


class Message(
    val channel: String,
    val text: String? = null,
    val as_user: Boolean? = null,
    val attachments: List<Attachment>? = null,
    val link_names: Boolean? = null,
    val parse: String? = null,
    val thread_ts: String? = null,
    val icon_emoji: String? = null,
    val icon_url: String? = null,
    val mrkdwn: Boolean? = null,
    val reply_broadcast: Boolean? = null,
    val unfurl_links: Boolean? = null,
    val unfurl_media: Boolean? = null,
    val username: String? = null
) : IMessage {
    override suspend fun send() {
        val client = HttpClient(Apache)
        val response = client.post<String>("https://slack.com/api/chat.postMessage") {
            header("Authorization", "Bearer " + System.getenv("SLACK_OAUTH"))
            contentType(ContentType.Application.Json)
            body = Klaxon().toJsonString(this@Message)
        }
    }
}