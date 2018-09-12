package pw.jonak.slackrpg.rollbot.operations

import pw.jonak.slackrpg.rollbot.MAX_ITERATION
import pw.jonak.slackrpg.rollbot.Operation
import pw.jonak.slackrpg.rollbot.Result
import sun.awt.SunToolkit

class Keep(private val numKeep: Int, private val fromBottom: Boolean) : Operation {
    override fun apply(sides: Int, res: List<Result>): List<Result>? {
        val sorted = res.sorted().filter { !it.dropped }.let { if(fromBottom) it.reversed() else it }
        for(i in 0 until sorted.size - numKeep) {
            sorted[i].dropped = true
        }
        return null
    }

    override val precedence: Int = 4

    companion object {
        // k[l|h]?[CP]
        fun parse(str: String): Pair<Keep?, Int> {
            var currentIter = 0

            if(str.length > 1) {
                var lower = false
                var start = 1
                if (str[1] == 'l') {
                    lower = true
                    start += 1
                }
                if (str[1] == 'h') {
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
                return Pair(n?.let { Keep(it, lower) }, end)
            }

            return Pair(null, 0)
        }
    }
}