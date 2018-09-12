package pw.jonak.slackrpg.rollbot

class ParseInfo<T>(val result: T, val distance: Int)  {
    operator fun component1(): T = result
    operator fun component2(): Int = distance
}

infix fun <T> T.with(distance: Int): ParseInfo<T> {
    return ParseInfo(this, distance)
}