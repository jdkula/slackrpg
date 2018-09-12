package pw.jonak.slackrpg.slack

class MessageDsl {
    val messages: ArrayList<IMessage> = ArrayList()
}

suspend fun send(configure: MessageDsl.() -> Unit) {
    val dsl = MessageDsl()
    dsl.configure()
    dsl.messages.forEach {
        it.send()
    }
}

fun MessageDsl.message(addInfo: MessageBuilder.() -> Unit) {
    val messageStart = MessageBuilder()
    messageStart.addInfo()
    messages += messageStart.toMessage()
}

//fun message(addInfo: MessageBuilder.() -> Unit): Message {
//    val messageStart = MessageBuilder()
//    messageStart.addInfo()
//    return messageStart.toMessage()
//}

fun MessageDsl.ephemeralMessage(addInfo: EphemeralMessageBuilder.() -> Unit) {
    val messageStart = EphemeralMessageBuilder()
    messageStart.addInfo()
    messages += messageStart.toMessage()
}

//fun ephemeralMessage(addInfo: EphemeralMessageBuilder.() -> Unit): EphemeralMessage {
//    val messageStart = EphemeralMessageBuilder()
//    messageStart.addInfo()
//    return messageStart.toMessage()
//}

fun IMessageBuilder.attachment(addInfo: AttachmentBuilder.() -> Unit) {
    val attachmentStart = AttachmentBuilder()
    attachmentStart.addInfo()
    if(this.attachments == null) {
        this.attachments = listOf(attachmentStart.toAttachment())
    } else {
        this.attachments = (this.attachments!! + attachmentStart.toAttachment())
    }
}

fun AttachmentBuilder.field(addInfo: FieldBuilder.() -> Unit) {
    val fieldStart = FieldBuilder()
    fieldStart.addInfo()
    if(this.fields == null) {
        this.fields = listOf(fieldStart.toField())
    } else {
        this.fields = (this.fields!! + fieldStart.toField())
    }
}