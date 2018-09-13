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