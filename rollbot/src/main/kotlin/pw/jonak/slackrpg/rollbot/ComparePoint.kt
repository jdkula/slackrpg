package pw.jonak.slackrpg.rollbot

import InfiniteLoop

enum class Comparison {
    EQUAL,
    GREATER_THAN,
    LESS_THAN
}

class ComparePoint(private val c: Comparison, private val n: Int) {

    operator fun invoke(t: Int): Boolean =
            when (c) {
                Comparison.EQUAL -> t == n
                Comparison.LESS_THAN -> t < n
                Comparison.GREATER_THAN -> t > n
            }

    companion object {
        fun parse(str: String): Pair<ComparePoint?, Int> {
            var currentIter = 0
            val c = when(str[0]) {
                '>' -> Comparison.GREATER_THAN
                '<' -> Comparison.LESS_THAN
                '=' -> Comparison.EQUAL
                else -> if(str[0].isDigit()) Comparison.EQUAL else return Pair(null, 0)
            }
            var i = if(str[0].isDigit()) 0 else 1
            val start = i
            while(i < str.length) {
                if(currentIter > MAX_ITERATION) throw InfiniteLoop()
                currentIter += 1

                if(!str[i].isDigit()) {
                    break
                }
                i += 1
            }
            val n = str.slice(start until i).toIntOrNull()
            return Pair(n?.let { ComparePoint(c, it) }, i)
        }
    }
}