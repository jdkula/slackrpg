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

interface IMessageBuilder {
    var attachments: List<Attachment>?
    fun toMessage(): IMessage
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

class EphemeralMessageBuilder : IMessageBuilder {
    override var attachments: List<Attachment>? = null
    lateinit var channel: String
    lateinit var user: String
    var text: String? = null
    var as_user: Boolean? = null
    var link_names: Boolean? = null
    var parse: String? = null
    var thread_ts: String? = null

    override fun toMessage(): EphemeralMessage {
        return EphemeralMessage(channel, user, text, as_user, attachments, link_names, parse, thread_ts)
    }
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

class MessageBuilder : IMessageBuilder {
    lateinit var channel: String
    var text: String? = null
    var as_user: Boolean? = null
    override var attachments: List<Attachment>? = null
    var link_names: Boolean? = null
    var parse: String? = null
    var thread_ts: String? = null
    var icon_emoji: String? = null
    var icon_url: String? = null
    var mrkdwn: Boolean? = null
    var reply_broadcast: Boolean? = null
    var unfurl_links: Boolean? = null
    var unfurl_media: Boolean? = null
    var username: String? = null

    override fun toMessage(): Message {
        return Message(
            channel,
            text,
            as_user,
            attachments,
            link_names,
            parse,
            thread_ts,
            icon_emoji,
            icon_url,
            mrkdwn,
            reply_broadcast,
            unfurl_links,
            unfurl_media,
            username
        )
    }
}