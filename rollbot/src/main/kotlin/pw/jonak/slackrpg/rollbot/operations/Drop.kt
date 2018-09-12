package pw.jonak.slackrpg.rollbot.operations

import pw.jonak.slackrpg.rollbot.MAX_ITERATION
import pw.jonak.slackrpg.rollbot.Operation
import pw.jonak.slackrpg.rollbot.Result
import sun.awt.SunToolkit
import kotlin.math.min

class Drop(private val numDrop: Int, private val fromTop: Boolean) : Operation {
    override fun apply(sides: Int, res: List<Result>): List<Result>? {
        val sorted = res.sorted().filter { !it.dropped }.let { if(fromTop) it.reversed() else it }
        val stop = min(numDrop, sorted.size)
        for(i in 0 until stop) {
            sorted[i].dropped = true
        }
        return null
    }

    override val precedence: Int = 5

    companion object {
        // d[l|h]?[CP]
        fun parse(str: String): Pair<Drop?, Int> {
            var currentIter = 0
            if(str.length > 1) {
                var higher = true
                var start = 1
                if (str[1] == 'h') {
                    start += 1
                }
                if(str[1] == 'l') {
                    higher = false
                    start += 1
                }

                var end = start
                while(end < str.length) {
                    if(currentIter > MAX_ITERATION) throw SunToolkit.InfiniteLoop()
                    currentIter += 1

                    if(!str[end].isDigit()) {
                        break
                    }
                    end += 1
                }
                val n = str.slice(start until end).toIntOrNull()
                return Pair(n?.let { Drop(it, higher) }, end)
            }

            return Pair(null, 0)
        }
    }

}