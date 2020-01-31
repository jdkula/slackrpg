package pw.jonak.slackrpg.healthbot.actions

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.util.pipeline.PipelineContext
import io.ktor.response.respond

suspend fun PipelineContext<Unit, ApplicationCall>.help() {
    call.respond(
        """
            *Healthbot Help*
            How to read this help:
            `<parameters>` in angle brackets are required.
            `[parameters]` in square brackets are optional.
            `<A|B>` means that you can put either A or B in this spot.
            Required parameters always come before optional parameters.
            `/register <Name> <Max Health> [#Current Health] [#Image URL] [@npc shortname] [@hidden]` — Registers your character.
            &gt;_`#Image URL` specifies a link to an image that will be displayed as your character._
            &gt;_`@npc shortname` - Normally, this command registers to your Slack user. This allows you to specify additional characters._
            &gt;_`@hidden` - This stops other users from viewing the character's current and max health._
            &gt;_*Example:* `/register Quill 9 #https://static.thenounproject.com/png/65187-200.png #7`_
            &gt;    _Registers Quill as your primary character, with a max HP of 9 and a current HP of 7. Additionally links an image._
            &gt;_*Example:* `/register Enemy McDragon 100 @dragon @hidden`_
            &gt;    _Registers Enemy as another character, which you can reference as @dragon, and hides his max health from others. Max and current HP are both set to 100._

            `/hurt <@target> <damage> [@attacker]` — Deals <damage> damage to the <@target>, and announces it.
            &gt;_`@attacker` defaults to the character of the user who typed the command._
            &gt;_*Example:* `/hurt @dragon 10`_
            &gt;    _Quill harms Enemy McDragon for 10 damage_
            &gt;_*Example:* `/hurt @jonak 15 @dragon`_
            &gt;    _Enemy McDragon harms Quill for 15 damage_

            `/heal <@target> <heal amount> [@healer]` — Works exactly like `/hurt`: heals <@target> for <heal amount> HP, and announces it.

            `/sethealth [@target] <health|@max>` — Sets a target's HP to a given amount, and announces it. If no target is specified, defaults to the person using this command.
            &gt;_If `@max` is specified, sets the target's health to their max._

            `/setmaxhealth [@target] <health>` — Sets the target's _max_ HP to a given amount, and announces it. If no target is specified, defaults to the person using this command.

            `/unregister [@target]` — Deletes the target character if the person using this command registered it. If no target is specified, defaults to the person using this command.

            `/info [@target|mine|my|all] [@broadcast]` — Gets the target character's info. If no target is specified, defaults to the person using this command.
            &gt;_If the target is `mine` or `my`, retrieves all your characters._
            &gt;_If `all` is specified, gets status of all current characters._
            &gt;_If `@broadcast` is specified, posts it to the channel. Defaults to sending it only to the requester._
            &gt;_The person who registered a character can always see its stats, even when @hidden is used._
        """.trimIndent()
    )
}