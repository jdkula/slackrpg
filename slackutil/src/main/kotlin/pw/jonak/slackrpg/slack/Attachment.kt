package pw.jonak.slackrpg.slack

data class Attachment(
    val fallback: String,
    val color: String? = null,
    val pretext: String? = null,
    val author_name: String? = null,
    val author_link: String? = null,
    val author_icon: String? = null,
    val title: String? = null,
    val title_link: String? = null,
    val text: String? = null,
    val fields: List<Field>? = null,
    val image_url: String? = null,
    val thumb_url: String? = null,
    val footer: String? = null,
    val footer_icon: String? = null,
    val ts: String? = null,
    val mrkdwn_in: List<String>? = null
)

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