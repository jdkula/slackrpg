package pw.jonak.slackrpg.rollbot.actions

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext

suspend fun PipelineContext<Unit, ApplicationCall>.help() {
    call.respond(
        """
        *Rollbot Help*
        How to read this help:
        `<parameters>` in angle brackets are required.
        `[parameters]` in square brackets are optional.
        Required parameters always come before optional parameters.
        `/roll <roll> [#Description] [@nosum]` — Computes your roll and posts it into the chat. (alias: `/r`)
        &gt;_`@nosum` suppresses a total from being displayed. This is useful if you're rolling two dice for different reasons._
        &gt;_`#description` - Optionally describes what you're rolling in the message Rollbot posts._
        &gt;_*Example:* `/roll 1d20+5 + 1d8+3 #My cool Scimitar attack @nosum`_
        &gt;    _Rolls 1d20+5 and 1d8+3 separately, without summing them together. Also announces to the world how cool your scimitar attack is._

        `/sroll <roll> [#Description] [@nosum]` — Same as `/roll`, but only you can see the result. (alias: `/sr`)

        `/save <macroname> <roll> [#Description] [@nosum]` — Saves a given roll definition and names it.
        &gt;_Notes: `<macroname>` must not contain spaces, and is *case sensitive*._

        `/delete <macroname>` — Deletes a given roll macro.

        `/mroll <macroname>` — Retrieves a stored macro and rolls it for you. (alias: `/mr`)

        `/smroll <macroname>` — Same as `/mroll`, but only you can see the result. (alias: `/smr`)
        """.trimIndent()
    )
}