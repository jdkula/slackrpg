package pw.jonak.slackrpg.healthbot.sql

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Characters : Table() {
    val user: Column<String> = varchar("user", 255)
    val characterId: Column<String> = varchar("character_id", 255)
    val characterName: Column<String> = varchar("character_name", 1024)
    val maxHealth: Column<Int> = integer("max_health")
    val currentHealth: Column<Int> = integer("current_health")
    val tempHealth: Column<Int> = integer("temporary_health")
    val characterImage: Column<String?> = varchar("character_image", 32768).nullable()
    val hidden: Column<Boolean> = bool("hidden")

    operator fun get(characterIdOrUser: String?, default: String? = null): Character? {
        val realCharacterId = parseUser(characterIdOrUser, default)

        return realCharacterId?.let {
            transaction {
                select {
                    (Characters.characterId eq realCharacterId)
                }.singleOrNull()?.let {
                    Character.from(it)
                }
            }
        }
    }

    fun getAll(
        selector: (Character) -> Boolean,
        user: String? = null,
        overrideHidden: Boolean = false
    ): List<Character> {
        return transaction {
            selectAll().map { Character.from(it) }.filter(selector).sortedBy { it.characterName }.sortedWith(compareBy(
                {
                    if (it.user == user && it.characterId == it.user && (overrideHidden || !it.hidden)) {
                        0
                    } else if (it.user == it.characterId && (!it.hidden)) {
                        1
                    } else if (it.user == user && overrideHidden) {
                        2
                    } else if (!it.hidden) {
                        3
                    } else {
                        4
                    }
                }, { it.characterName })
            )
        }
    }

    operator fun plusAssign(character: Character) {
        if (get(character.characterId) == null) {
            transaction {
                insert {
                    it[this.user] = character.user
                    it[this.characterId] = character.characterId
                    it[this.characterName] = character.characterName
                    it[this.maxHealth] = character.maxHealth
                    it[this.currentHealth] = character.currentHealth
                    it[this.tempHealth] = character.tempHealth
                    it[this.characterImage] = character.characterImage
                    it[this.hidden] = character.hidden
                }
            }
        } else {
            transaction {
                update({
                    (Characters.user eq character.user) and (Characters.characterId eq character.characterId)
                }) {
                    it[this.characterName] = character.characterName
                    it[this.maxHealth] = character.maxHealth
                    it[this.currentHealth] = character.currentHealth
                    it[this.tempHealth] = character.tempHealth
                    it[this.characterImage] = character.characterImage
                    it[this.hidden] = character.hidden
                }
            }
        }
    }

    fun add(character: Character) = plusAssign(character)

    fun updateHealth(character: Character, newHealth: Int, newTempHealth: Int? = null): Character? {
        transaction {
            update({
                Characters.characterId eq character.characterId
            }) {
                it[this.currentHealth] = newHealth
                if(newTempHealth != null) {
                    it[this.tempHealth] = newTempHealth
                }
            }
        }
        return get(character.characterId)
    }

    fun updateMaxHealth(character: Character, newHealth: Int): Character? {
        transaction {
            update({
                Characters.characterId eq character.characterId
            }) {
                it[this.maxHealth] = newHealth
            }
        }
        return get(character.characterId)
    }


    operator fun minusAssign(character: Character) {
        transaction {
            deleteWhere {
                characterId eq character.characterId
            }
        }
    }

    fun delete(character: Character) = minusAssign(character)
}

class Character(
    val user: String,
    val characterId: String,
    val characterName: String,
    val maxHealth: Int,
    val currentHealth: Int,
    val tempHealth: Int,
    val characterImage: String?,
    val hidden: Boolean
) {

    override operator fun equals(other: Any?): Boolean {
        return if (other is Character) other.characterId == characterId else false
    }

    override fun hashCode(): Int {
        return characterId.hashCode()
    }

    companion object {
        fun from(row: ResultRow): Character {
            return Character(
                row[Characters.user],
                row[Characters.characterId],
                row[Characters.characterName],
                row[Characters.maxHealth],
                row[Characters.currentHealth],
                row[Characters.tempHealth],
                row[Characters.characterImage],
                row[Characters.hidden]
            )
        }
    }
}

private val userFormat = Regex("^<(.+?)(?:\\|.+?)?>$")
fun parseUser(characterIdOrUser: String?, default: String?): String? {
    val found = characterIdOrUser?.let {
        userFormat.find(it)?.groups?.get(1)?.value?.trimStart('@') ?: it.trimStart('@')
    }
    return if (found.isNullOrEmpty()) default else found
}