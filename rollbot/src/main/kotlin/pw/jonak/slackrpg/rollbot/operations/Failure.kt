package pw.jonak.slackrpg.rollbot.operations

import pw.jonak.slackrpg.rollbot.ComparePoint
import pw.jonak.slackrpg.rollbot.Operation
import pw.jonak.slackrpg.rollbot.Result

class Failure(private val cp: ComparePoint) : Operation {
    override fun apply(sides: Int, res: List<Result>): List<Result>? {
        res.forEach {
            if (cp(it.roll)) {
                it.success = false
            }
        }
        return null
    }

    override val precedence: Int = 7

    companion object {
        fun parse(str: String): Pair<Failure?, Int> {
            val p = ComparePoint.parse(str)
            return Pair(p.first?.let { Failure(it) }, p.second)
        }
    }
}