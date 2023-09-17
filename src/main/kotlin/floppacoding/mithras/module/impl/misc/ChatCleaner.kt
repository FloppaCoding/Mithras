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
    private val dungeon         by BooleanSetting("Dungeon Messages", true, description = "Hides useless messages in dungeons.")
    private val boss            by BooleanSetting("Boss Messages", true, description = "Hides dungeon boss messages.")
    private val invFull         by BooleanSetting("Inventory Full", true, description = "Hides the Inventory Full? messages.")
    private val dungPot         by BooleanSetting("Dungeon potion", true, description = "Hides dungeon potion messages")
    private val milestones      by BooleanSetting("Milestone Messages", true, description = "Hides Milestone messages in dungeons.")
    private val hypixelMsgs     by BooleanSetting("Useless Hypixel Msgs", true, description = "Hides useless Messages")

    private val abilityHider    by BooleanSetting("Hide Ability Damage", true, description = "Hides Ability Damage from chat.")
    private val stashHider      by BooleanSetting("Hide Stash", true, description = "Hides Stash messages")
    private val sacksHider      by BooleanSetting("Hide Sacks", false, description= "Hides the sacks messages.")
    private val blocksInTheWay  by BooleanSetting("Blocks in way", true, description = "Hides There are blocks in the way! messages")
    private val comboHider      by BooleanSetting("Hide Combo", true, description = "Hides §6§l§o+50 Kill Combo messages.")
    private val autoRecombHider by BooleanSetting("Hide Auto Recomb", true, description = "Hides Auto Recombobulator messages.")

    @EventHandler(priority = EventPriority.LOWEST)
    fun onChat(event: ChatReceivedEvent) {
        if (event.type != ChatReceivedEvent.Type.GAME_MESSAGE) return
        val text = Formatting.strip(event.text.string) ?: return
        if (text.containsOneOf(dontCancel)) return // This is here, so it doesn't cancel party and guild chat messages
        when {
            dungeon         && LocationManager.inDungeons && text.containsOneOf(dung)           -> event.cancel()
            boss            && LocationManager.inDungeons && text.startsWith("[BOSS] ")           -> event.cancel()
            invFull         && text == "Inventory full? Don't forget to check out your Storage inside the SkyBlock Menu!" -> event.cancel()
            dungPot         && text.containsOneOf(dungPots)                                     -> event.cancel()
            hypixelMsgs     && text.containsOneOf(uselessMSG)                                   -> event.cancel()
            milestones      && LocationManager.inDungeons && text.containsOneOf(dungClasses)    -> event.cancel()
            blocksInTheWay  && text.startsWith("There are blocks in the way!")            -> event.cancel()
            abilityHider    && text.startsWith("Your") && text.endsWith("damage.")  -> event.cancel()
            comboHider      && text.contains("Kill Combo") && !text.contains(":")    -> event.cancel()
            autoRecombHider && text.startsWith("Your Auto-Recombobulator recombobulated") -> event.cancel()
            stashHider      && text.endsWith("Click here to pick it all up!")              -> event.cancel()
            sacksHider      && text.matches(sacksPattern) -> event.cancel()
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
        "Guided Sheep is now available",
        "is ready to use! Press DROP to activate it!",
        "used Dragon's Breath on you!",
        "[SKULL] Wither Skull: Monsters with a star next to their name have Wither Keys...sometimes.",
        "[SKULL] Wither Skull: You need a Wither Key to open this door!",
        "[STATUE] Oruo the Omniscient: I am Oruo the Omniscient. I have lived many lives. I have learned all there is to know.",
        "[STATUE] Oruo the Omniscient: Though I sit stationary in this prison that is The Catacombs, my knowledge knows no bounds.",
        "[STATUE] Oruo the Omniscient: Prove your knowledge by answering 3 questions and I shall reward you in ways that transcend time!",
        "[STATUE] Oruo the Omniscient: Answer incorrectly, and your moment of ineptitude will live on for generations.",
    )
    private val dungPots = setOf(
        "You are not allowed to use Potion Effects while in Dungeon, therefore all active effects have been paused and stored. They will be restored when you leave Dungeon!",
//        "Your active Potion Effects have been paused", // old message
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
        "Staff have banned an additional",
        "Blacklisted modifications are a bannable offense!",
        "Profile ID: "
    )

    private val sacksPattern = Regex("\\[Sacks] [+-][\\d]+ items?\\. \\(Last [\\d]+s\\.\\)")
}