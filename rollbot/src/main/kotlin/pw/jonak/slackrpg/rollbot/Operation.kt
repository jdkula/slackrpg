package pw.jonak.slackrpg.rollbot

interface Operation : Comparable<Operation> {
    fun apply(sides: Int, res: List<Result>): List<Result>?
    val precedence: Int

    override fun compareTo(other: Operation): Int {
        return precedence.compareTo(other.precedence)
    }
}