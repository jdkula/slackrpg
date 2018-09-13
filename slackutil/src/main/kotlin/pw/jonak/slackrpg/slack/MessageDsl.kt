package pw.jonak.slackrpg.slack

@DslMarker
annotation class MessageDsl

@MessageDsl
inline fun message(addInfo: MessageBuilder.() -> Unit): Message {
    val messageStart = MessageBuilder()
    messageStart.addInfo()
    return messageStart.build()
}

@MessageDsl
inline fun ephemeralMessage(addInfo: EphemeralMessageBuilder.() -> Unit) {
    val messageStart = EphemeralMessageBuilder()
    messageStart.addInfo()
    messageStart.build()
}

interface IMessageBuilder {
    var attachments: List<Attachment>?
    fun build(): IMessage
    suspend fun send()

    @MessageDsl
    fun attachment(addInfo: AttachmentBuilder.() -> Unit) {
        val attachmentStart = AttachmentBuilder()
        attachmentStart.addInfo()
        if (this.attachments == null) {
            this.attachments = listOf(attachmentStart.toAttachment())
        } else {
            this.attachments = (this.attachments!! + attachmentStart.toAttachment())
        }
    }
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

    override suspend fun send() {
        build().send()
    }

    override fun build(): EphemeralMessage {
        return EphemeralMessage(channel, user, text, as_user, attachments, link_names, parse, thread_ts)
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

    override suspend fun send() {
        build().send()
    }

    suspend fun sendEphemeral(targetUser: String) {
        EphemeralMessage(targetUser, build()).send()
    }

    override fun build(): Message {
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

class AttachmentBuilder {
    lateinit var fallback: String
    var color: String? = null
    var pretext: String? = null
    var author_name: String? = null
    var author_link: String? = null
    var author_icon: String? = null
    var title: String? = null
    var title_link: String? = null
    var text: String? = null
    var fields: List<Field>? = null
    var image_url: String? = null
    var thumb_url: String? = null
    var footer: String? = null
    var footer_icon: String? = null
    var ts: String? = null
    var mrkdwn_in: List<String>? = null

    @MessageDsl
    fun field(addInfo: FieldBuilder.() -> Unit) {
        val fieldStart = FieldBuilder()
        fieldStart.addInfo()
        if (this.fields == null) {
            this.fields = listOf(fieldStart.toField())
        } else {
            this.fields = (this.fields!! + fieldStart.toField())
        }
    }

    fun toAttachment(): Attachment {
        return Attachment(
            fallback,
            color,
            pretext,
            author_name,
            author_link,
            author_icon,
            title,
            title_link,
            text,
            fields,
            image_url,
            thumb_url,
            footer,
            footer_icon,
            ts,
            mrkdwn_in
        )
    }
}

class FieldBuilder {
    lateinit var title: String
    lateinit var value: String
    var short: Boolean = false

    fun toField(): Field {
        return Field(title, value, short)
    }
}