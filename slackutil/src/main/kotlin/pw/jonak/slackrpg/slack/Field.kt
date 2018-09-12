package pw.jonak.slackrpg.slack

data class Field(val title: String, val value: String, val short: Boolean)

data class FieldBuilder(var title: String? = null, var value: String? = null, var short: Boolean? = null) {
    fun toField(): Field {
        return Field(title!!, value!!, short!!)
    }
}