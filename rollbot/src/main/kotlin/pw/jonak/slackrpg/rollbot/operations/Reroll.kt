package pw.jonak.slackrpg.rollbot.operations

import InfiniteLoop
import pw.jonak.slackrpg.rollbot.ComparePoint
import pw.jonak.slackrpg.rollbot.MAX_ITERATION
import pw.jonak.slackrpg.rollbot.Operation
import pw.jonak.slackrpg.rollbot.Result
import java.util.*

class Reroll(cp: ComparePoint, rerollOnce: Boolean, private val random: Random = Random()) : Operation {
    private val cps: HashMap<ComparePoint, Boolean> = HashMap()

    init {
        cps += cp to rerollOnce
    }

    override fun apply(sides: Int, res: List<Result>): List<Result>? {
        var currentIter = 0
        res.forEach {
            var rerolled = false
            do {
                if (currentIter > MAX_ITERATION) throw InfiniteLoop()
                currentIter += 1

                for (cp in cps.keys) {
                    if (it.rerollable && cp(it.roll)) {
                        it.roll = random.nextInt(sides) + 1
                        if (cps[cp] == true) {
                            it.rerollable = false
                        }
                        rerolled = true
                    }
                }
            } while (rerolled && it.rerollable)
        }
        return null
    }

    fun addComparePoint(cp: ComparePoint, rerollOnce: Boolean) {
        cps += cp to rerollOnce
    }

    override val precedence: Int = 3

    companion object {
        fun parse(str: String, oldReroll: Reroll?, random: Random): Pair<Reroll?, Int> {
            var rerollOnce = false
            var distance = 1
            if (str.length > 2 && str[1] == 'o') {
                rerollOnce = true
                distance = 2
            }
            val (cp, past) = ComparePoint.parse(str.substring(distance))
            if (cp == null) return Pair(null, 0)
            if (oldReroll != null) {
                oldReroll.addComparePoint(cp, rerollOnce)
                return null to past
            }
            return Reroll(cp, rerollOnce, random) to distance + past - 1
        }
    }
}