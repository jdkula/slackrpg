package pw.jonak.slackrpg.rollbot.operations

import pw.jonak.slackrpg.rollbot.ComparePoint
import pw.jonak.slackrpg.rollbot.Operation
import pw.jonak.slackrpg.rollbot.Result

class Success(private val cp: ComparePoint) : Operation {
    override fun apply(sides: Int, res: List<Result>): List<Result>? {
        res.forEach {
            if (cp(it.roll)) {
                it.success = true
            }
        }
        return null
    }

    override val precedence: Int = 6

    companion object {
        fun parse(str: String): Pair<Success?, Int> {
            val cpr = ComparePoint.parse(str)
            return Pair(cpr.first?.let { Success(it) }, cpr.second)
        }
    }
}