package floppacoding.mithras.module.impl.misc

import floppacoding.mithras.events.ChatReceivedEvent
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.impl.BooleanSetting
import floppacoding.mithras.utils.Extensions.containsOneOf
import floppacoding.mithras.utils.LocationManager
import meteordevelopment.orbit.EventHandler
import meteordevelopment.orbit.EventPriority
import net.minecraft.util.Formatting

/**
 * A modules meant for removing spammy messages from chat.
 * @author Stivais, Aton
 */
object ChatCleaner : Module(
    "Chat Cleaner",
    category = Category.MISC,
    description = "Cleans chat from spam."
) {
    private val dungeon = BooleanSetting("Dungeon Messages", true, description = "Hides useless messages in dungeons.")
    private val dungPot = BooleanSetting("Dungeon potion", true, description = "Hides dungeon potion messages")
    private val milestones = BooleanSetting("Milestone Messages", true, description = "Hides Milestone messages in dungeons.")
    private val hypixelMsgs = BooleanSetting("Useless Hypixel Msgs", true, description = "Hides useless Messages")

    private val abilityHider = BooleanSetting("Hide Ability Damage", true, description = "Hides Ability Damage from chat.")
    private val stashHider = BooleanSetting("Hide Stash", true, description = "Hides Stash Messages")
    private val blocksInTheWay = BooleanSetting("Blocks in way", true, description = "Hides There are blocks in the way! messages")
    private val comboHider = BooleanSetting("Hide Combo", true, description = "Hides §6§l§o+50 Kill Combo messages.")
    private val autoRecombHider = BooleanSetting("Hide Auto Recomb", true, description = "Hides Auto Recombobulator messages.")

    init {
        this.addSettings(
            dungeon,
            dungPot,
            milestones,
            abilityHider,
            blocksInTheWay,
            comboHider,
            autoRecombHider,
        )
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onChat(event: ChatReceivedEvent) {
        if (event.type != ChatReceivedEvent.Type.GAME_MESSAGE) return
        val text = Formatting.strip(event.text.string) ?: return
        if (text.containsOneOf(dontCancel)) return // This is here, so it doesn't cancel party and guild chat messages
        when {
            dungeon.enabled         && LocationManager.inDungeons && text.containsOneOf(dung)           -> event.cancel()
            dungPot.enabled         && text.containsOneOf(dungPots)                                     -> event.cancel()
            hypixelMsgs.enabled     && text.containsOneOf(uselessMSG)                                   -> event.cancel()
            milestones.enabled      && LocationManager.inDungeons && text.containsOneOf(dungClasses)    -> event.cancel()
            blocksInTheWay.enabled  && text.startsWith("There are blocks in the way!")            -> event.cancel()
            abilityHider.enabled    && text.startsWith("Your") && text.endsWith("damage.")  -> event.cancel()
            comboHider.enabled      && text.contains("Kill Combo") && !text.contains(":")    -> event.cancel()
            autoRecombHider.enabled && text.startsWith("Your Auto-Recombobulator recombobulated") -> event.cancel()
            stashHider.enabled      && text.endsWith("Click here to pick it all up!")              -> event.cancel()
        }
    }


    private val dontCancel = listOf("Party >", "Guild >")
    private val dung = setOf(
        "DUNGEON BUFF!",
        "A Blessing of",
        "Granted you +",
        "unlocked Undead Essence",
        "unlocked Wither Essence",
        "found a Wither Essence",
        "has obtained",
        "RIGHT CLICK on a",
        "Guided Sheep is now available"
    )
    private val dungPots = setOf(
        "Your active Potion Effects have been paused",
        "BUFF! You have gained",
        "You can no longer consume"
    )
    private val dungClasses = setOf(
        "stats are doubled because you are the only player using this class!",
        "[Mage]",
        "[Archer]",
        "[Berserk]",
        "[Tank]",
        "[Healer]",
        "Milestone"
    )
    private val uselessMSG = setOf(
        "You are playing on profile:",
        "Welcome to Hypixel SkyBlock!",
        "from playing Skyblock!",
        "[WATCHDOG ANNOUNCEMENT]",
        "Watchdog has banned",
        "Staff have banned an additional"
    )
}