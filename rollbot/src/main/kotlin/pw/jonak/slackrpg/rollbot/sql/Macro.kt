package pw.jonak.slackrpg.rollbot.sql

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import pw.jonak.slackrpg.rollbot.RollInfo

object Macros : Table() {
    val user: Column<String> = varchar("user", 255)
    val macro: Column<String> = varchar("macro", 255)
    val description: Column<String> = varchar("description", 255)
    val roll: Column<String> = varchar("roll", 255)
    val noSum: Column<Boolean> = bool("nosum")

    operator fun get(userId: String, macroName: String): Macro? {
        return transaction {
            Macros.select {
                (user eq userId) and (macro eq macroName)
            }.singleOrNull()?.let {
                Macro.from(it)
            }
        }
    }

    operator fun set(userId: String, macroName: String, roll: RollInfo) {
        if(get(userId, macroName) != null) {
            transaction {
                Macros.update({
                    (user eq userId) and (macro eq macroName)
                }) {
                    it[Macros.roll] = roll.roll
                    it[description] = roll.description
                    it[noSum] = roll.noSum
                }
            }
        } else {
            transaction {
                Macros.insert {
                    it[user] = userId
                    it[macro] = macroName
                    it[Macros.roll] = roll.roll
                    it[description] = roll.description
                    it[noSum] = roll.noSum
                }
            }
        }
    }

    fun delete(userId: String, shortcutName: String) {
        transaction {
            Macros.deleteWhere {
                (user eq userId) and (macro eq shortcutName)
            }
        }
    }
}

class Macro(val macroName: String, val rollInfo: RollInfo) {
    companion object {
        fun from(row: ResultRow): Macro {
            return Macro(
                row[Macros.macro],
                RollInfo.from(row)
            )
        }

        fun parse(command: String): Macro {
            val split = command.split(" ", limit = 2)
            val macroName = split[0].trim()

            return Macro(
                macroName,
                RollInfo.parse(split[1])
            )
        }
    }
}