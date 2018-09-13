package pw.jonak.slackrpg.rollbot.operations

import pw.jonak.slackrpg.rollbot.Operation
import pw.jonak.slackrpg.rollbot.Result

class Description(val text: String) : Operation {
    override val precedence: Int = 100
    override fun apply(sides: Int, res: List<Result>): List<Result>? = null

    companion object {
        fun parse(str: String): Pair<Description, Int> {
            var end = 1
            var brackets = 1
            while(end <= str.length) {
                when(str[end]) {
                    '[' -> brackets++
                    ']' -> brackets--
                }
                if(brackets == 0) break
                end++
            }

            return Pair(Description(str.substring(1, end)), end)
        }
    }
}
