package pw.jonak.slackrpg.rollbot

import com.beust.klaxon.Json


open class Result(var roll: Int, var dropped: Boolean = false, @Json(ignored = true) var rerollable: Boolean = true, var success: Boolean? = null) : Comparable<Result> {
    override operator fun compareTo(other: Result): Int {
        return roll.compareTo(other.roll)
    }

    override fun toString(): String {
        return when (dropped) {
            true -> "[/$roll]"
            false -> "$roll"
        }
    }
}