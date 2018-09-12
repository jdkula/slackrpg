package pw.jonak.slackrpg.rollbot.operations

import pw.jonak.slackrpg.rollbot.*
import sun.awt.SunToolkit
import java.util.*
import kotlin.collections.ArrayList

class Exploding(private val cp: ComparePoint, private val random: Random = Random()) :
    Operation {
    override fun apply(sides: Int, res: List<Result>): List<Result>? {
        var currentIter = 0
        tailrec fun helper(res: List<Result>, acc: ArrayList<Result> = ArrayList()): List<Result> {
            if(currentIter >= MAX_ITERATION) throw SunToolkit.InfiniteLoop()
            if (res.isEmpty()) return acc
            val result = ArrayList<Result>()
            res.forEach {
                if (cp(it.roll)) {
                    result += Result(random.nextInt(sides) + 1)
                }
            }
            acc.addAll(result)
            currentIter += 1
            return helper(result, acc)
        }

        return helper(res)
    }

    override val precedence: Int = 0

    companion object {
        fun parse(str: String, sides: Int, random: Random): Pair<Operation?, Int> {
            if (str.isNotEmpty()) {
                if (str.length == 1 && str[0] == '!') {
                    return Pair(
                        Exploding(
                            ComparePoint(
                                Comparison.EQUAL,
                                sides
                            ), random
                        ), 1)
                }
                if (str.length > 1 && str[0] == '!' && str[1] == '!') {
                    val cpInfo = ComparePoint.parse(str.substring(2))
                    return Pair(
                        Compounding(
                            cpInfo.first ?: ComparePoint(
                                Comparison.EQUAL,
                                sides
                            ),
                            random
                        ),
                            2 + cpInfo.second
                    )
                }
                if (str.length > 1 && str[0] == '!' && str[1] == 'p') {
                    val cpInfo = ComparePoint.parse(str.substring(2))
                    return Pair(
                        Penetrating(
                            cpInfo.first ?: ComparePoint(
                                Comparison.EQUAL,
                                sides
                            ),
                            random
                        ),
                            2 + cpInfo.second
                    )
                }
                if (str.length > 1 && str[0] == '!') {
                    val cpInfo = ComparePoint.parse(str.substring(1))
                    return Pair(
                        Exploding(
                            cpInfo.first ?: ComparePoint(
                                Comparison.EQUAL,
                                sides
                            ),
                            random
                        ),
                            1 + cpInfo.second
                    )

                }
            }
            return Pair(null, 0)

        }
    }
}