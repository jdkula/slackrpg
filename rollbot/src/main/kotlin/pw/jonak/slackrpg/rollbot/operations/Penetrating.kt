package pw.jonak.slackrpg.rollbot.operations

import pw.jonak.slackrpg.rollbot.ComparePoint
import pw.jonak.slackrpg.rollbot.Operation
import pw.jonak.slackrpg.rollbot.Result
import java.util.*
import kotlin.collections.ArrayList

class Penetrating(private val cp: ComparePoint, private val random: Random = Random()) :
    Operation {
    override fun apply(sides: Int, res: List<Result>): List<Result>? {
        tailrec fun helper(res: List<Result>, acc: ArrayList<Result> = ArrayList()): List<Result> {
            if(res.isEmpty()) return acc
            val result = ArrayList<Result>()
            res.forEach {
                if(cp(it.roll)) {
                    result += Result(random.nextInt(sides))
                }
            }
            acc.addAll(result)
            return helper(result, acc)
        }

        return helper(res)
    }

    override val precedence: Int = 2
}