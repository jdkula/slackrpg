package pw.jonak.slackrpg.rollbot

import org.jetbrains.exposed.sql.ResultRow
import pw.jonak.slackrpg.rollbot.sql.Macros

class RollInfo(val roll: String, val description: String, val noSum: Boolean) {
    companion object {
        fun from(row: ResultRow): RollInfo {
            return RollInfo(
                row[Macros.roll],
                row[Macros.description],
                row[Macros.noSum]
            )
        }

        fun parse(command: String): RollInfo {
            val noSum = command.contains("@nosum")
            val split1 = command.replace("@nosum", "").split("#", limit = 2)
            val roll = split1[0].trim()
            val description = split1.getOrNull(1)?.trim() ?: ""

            return RollInfo(roll, description, noSum)
        }
    }
}