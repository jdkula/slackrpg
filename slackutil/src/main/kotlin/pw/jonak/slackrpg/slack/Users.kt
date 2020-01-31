package pw.jonak.slackrpg.slack

import com.beust.klaxon.Klaxon
import com.beust.klaxon.KlaxonException
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.coroutines.runBlocking

object Users {
    lateinit var users: List<User>

    init {
        val client = HttpClient(Apache)
        runBlocking {
            val userInfo = client.get<String>("https://slack.com/api/users.list") {
                header("Authorization", "Bearer " + System.getenv("SLACK_OAUTH"))
            }
            val parsed = try {
                Klaxon().parse<Members>(userInfo)
            } catch (e: KlaxonException) {
                null
            }
            if (parsed != null) {
                users = parsed.members
            }
        }
    }

    fun initialize() {
        // No-op; by calling this method, the init block is sure to run (or have been run).
    }

    operator fun get(id: String): User? {
        return users.singleOrNull { it.id == id }
    }
}

data class Members(
    val ok: Boolean,
    val members: List<User>,
    val cache_ts: Int,
    val response_metadata: Map<String, String>
)

data class User(
    val id: String,
    val team_id: String,
    val profile: Profile,
    val name: String? = null,
    val deleted: Boolean? = null,
    val real_name: String? = null,
    val tz_label: String? = null,
    val tz_offset: Int? = null,
    val is_admin: Boolean? = null,
    val is_owner: Boolean? = null,
    val is_primary_owner: Boolean? = null,
    val is_restricted: Boolean? = null,
    val is_ultra_restricted: Boolean? = null,
    val is_bot: Boolean? = null,
    val updated: Int? = null,
    val is_app_user: Boolean,
    val color: String? = null,
    val has_2fa: Boolean? = null,
    val is_stranger: Boolean? = null,
    val tz: String? = null,
    val locale: String? = null
)

data class Profile(
    val avatar_hash: String,
    val status_text: String,
    val status_emoji: String,
    val status_expiration: Int,
    val real_name: String,
    val display_name: String,
    val real_name_normalized: String,
    val display_name_normalized: String,
    val image_24: String,
    val image_32: String,
    val image_48: String,
    val image_72: String,
    val image_192: String,
    val image_512: String,
    val team: String,
    val email: String? = null
)