package floppacoding.mithras.utils

import floppacoding.mithras.Mithras
import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.module.impl.render.MainSettings
import floppacoding.mithras.module.impl.render.PrefixStyle
import floppacoding.mithras.utils.ChatUtils.chatMessage
import floppacoding.mithras.utils.ChatUtils.createHoverableText
import floppacoding.mithras.utils.ChatUtils.literalText
import floppacoding.mithras.utils.ChatUtils.modMessage
import floppacoding.mithras.utils.ChatUtils.sendMessage
import floppacoding.mithras.utils.ChatUtils.setHoverEvent
import floppacoding.mithras.utils.ChatUtils.setHoverText
import net.minecraft.item.ItemStack
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.StringHelper
import org.apache.commons.lang3.StringUtils

/**
 * # A collection of utility functions for creating and sending or displaying chat messages.
 *
 * Use [chatMessage] to put messages in chat which are only visible locally and are not sent to the server.
 *
 * Use [modMessage] for client side messages in chat that start with mods chat prefix.
 *
 * Use [sendMessage] for sending a player message to the server.
 *
 * ## Some info about Minecraft methods:
 * The method [net.minecraft.client.network.ClientPlayerEntity.sendMessage] does exactly the same as
 * [net.minecraft.client.gui.hud.ChatHud.addMessage] even tho the name suggest otherwise.
 *
 *
 * ## Creating [Text] elements
 * This class offers a variety of methods that make the creating of [Text] / [MutableText] elements easier.
 * Most methods which accept string inputs generally support styled text with formatting codes containing §.
 * When the method has an additional reformat argument it can also support formatting codes initiated with &.
 *
 * ### Creating text from a (formatted) String
 * The easiest way to create a Text element is to use [literalText] which allows you to turn a string optionally
 * containing formatting into the corresponding Text. This is equivalent to using [Text.literal].
 *
 * ### Creating hover events
 * To create text with more info text as a hover event you can use [createHoverableText].
 * Alternatively you can add hover events to already existing [MutableText] with [setHoverText] or [setHoverEvent].
 *
 * ### Combining [MutableText] elements
 * To combine text elements into one use [MutableText.append]
 *
 * ### Further functionality
 * If what you need does not exist look at the native minecraft methods. One which might be particularly useful is
 * [MutableText.styled].
 *
 *
 * @author Aton
 */
@Suppress("unused")
object ChatUtils {
    val BLACK         = Formatting.BLACK.toString()
    val DARK_BLUE     = Formatting.DARK_BLUE.toString()
    val DARK_GREEN    = Formatting.DARK_GREEN.toString()
    val DARK_AQUA     = Formatting.DARK_AQUA.toString()
    val DARK_RED      = Formatting.DARK_RED.toString()
    val DARK_PURPLE   = Formatting.DARK_PURPLE.toString()
    val GOLD          = Formatting.GOLD.toString()
    val GRAY          = Formatting.GRAY.toString()
    val DARK_GRAY     = Formatting.DARK_GRAY.toString()
    val BLUE          = Formatting.BLUE.toString()
    val GREEN         = Formatting.GREEN.toString()
    val AQUA          = Formatting.AQUA.toString()
    val RED           = Formatting.RED.toString()
    val LIGHT_PURPLE  = Formatting.LIGHT_PURPLE.toString()
    val YELLOW        = Formatting.YELLOW.toString()
    val WHITE         = Formatting.WHITE.toString()
    val OBFUSCATED    = Formatting.OBFUSCATED.toString()
    val BOLD          = Formatting.BOLD.toString()
    val STRIKETHROUGH = Formatting.STRIKETHROUGH.toString()
    val UNDERLINE     = Formatting.UNDERLINE.toString()
    val ITALIC        = Formatting.ITALIC.toString()
    val RESET         = Formatting.RESET.toString()

    /**
     * Pattern to replace formatting codes with & with the § equivalent.
     *
     * This Regex will match all "&" that are directly followed by one of 0,1,2,3,4,5,6,7,8,9,a,b,c,d,e,f,k,l,m,n,o,r.
     *
     * This regex is the same as
     *
     *     Regex("(?i)&(?=[0-9A-FK-OR])").
     */
    private val formattingCodePattern = Regex("&(?=[0-9A-FK-OR])", RegexOption.IGNORE_CASE)

    /**
     * Replaces chat formatting codes using "&" as escape character with "§" as the escape character.
     * Example: "&aText &r" as input will return "§aText §r".
     */
    private fun reformatString(text: String): String {
        return formattingCodePattern.replace(text, "§")
    }

    /**
     * Remove control codes from the [receiver][String] with the [vanilla function][Formatting.strip] for it.
     */
    fun String.stripControlCodes(): String {
        return Formatting.strip(this) ?: ""
    }

    /**
     * Puts a message in chat client side with the mod prefix.
     * @param reformat Replace the "&" in formatting strings with "§".
     * @see chatMessage
     */
    @JvmOverloads
    fun modMessage(text: String, reformat: Boolean = true) {
        val message = literalText(text, reformat)
        modMessage(message)
    }

    /**
     * Puts a message in chat client side with the mod prefix.
     * @see chatMessage
     */
    fun modMessage(message: Text) = chatMessage(
        Text.literal(
            when (MainSettings.prefixStyle.value) {
                PrefixStyle.LONG   -> Mithras.CHAT_PREFIX
                PrefixStyle.SHORT  -> Mithras.SHORT_PREFIX
                PrefixStyle.CUSTOM -> reformatString( MainSettings.customPrefix.text)
            } + " "
        ).append(message)
    )

    /**
     * Print a message in chat **client side**.
     * @param reformat Replace the "&" in formatting strings with "§".
     * @see modMessage
     * @see sendMessage
     */
    @JvmOverloads
    fun chatMessage(text: String, reformat: Boolean = true) {
        val message = literalText(text, reformat)
        chatMessage(message)
    }

    /**
     * Print a message in chat **client side**.
     * @see modMessage
     * @see sendMessage
     */
    fun chatMessage(message: Text) {
        mc.inGameHud?.chatHud?.addMessage(message)
    }

    /**
     * **Send player message to the server**.
     *
     * This mimics the player using the chat gui to send the message.
     *
     * If the message starts with a "/" it will be treated like a command. Otherwise, like a chat message.
     * It will try to run teh command client side first. If that is successful the command will not be sent to the server.
     * @see chatMessage
     * @see modMessage
     */
    @JvmOverloads
    fun sendMessage(message: String, addToHistory: Boolean = false) {
        val chatText = StringHelper.truncateChat(StringUtils.normalizeSpace(message.trim()))
        if (chatText.isNotEmpty()) {
            if (addToHistory) {
                mc.inGameHud.chatHud.addToMessageHistory(chatText)
            }
            if (chatText.startsWith("/")) {
                /**
                 *  Fabric will take care of determining whether it is a client side or server side command and only
                 *  send the server side command to the server.
                 *  @see net.fabricmc.fabric.mixin.command.client.ClientPlayNetworkHandlerMixin.onSendCommand
                 */
                mc.player?.networkHandler?.sendChatCommand(chatText.substring(1))
            } else {
                mc.player?.networkHandler?.sendChatMessage(chatText)
            }
        }
    }

    /**
     * Runs the specified command. It is tried first to run the command on the client.
     * If that is not possible it is sent to the server.
     * The input is assumed to **not** include the slash "/" that signals a command.
     */
    fun command(text: String) {
        mc.player?.networkHandler?.sendChatCommand(text)
    }

    /**
     * Creates a Mutable text instance from the literal representation given by [text].
     * Supports formatting codes expressed with § and can also reformat formatting codes with &.
     * @param reformat Replace the "&" in formatting strings with "§".
     * @see Text.literal
     */
    @JvmOverloads
    fun literalText(text: String, reformat: Boolean = true) : MutableText = Text.literal(if (reformat) reformatString(text) else text)

    fun MutableText.setHoverEvent(hoverEvent: HoverEvent) : MutableText {
        return this.setStyle(this.style.withHoverEvent(hoverEvent))
    }

    fun MutableText.setHoverText(text: Text): MutableText {
        return this.setHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, text))
    }

    @JvmOverloads
    fun MutableText.setHoverText(text: String, reformat: Boolean = true): MutableText {
        return this.setHoverText(literalText(text, reformat))
    }

    fun MutableText.setHoverItem(itemStack: ItemStack): MutableText {
        return this.setHoverEvent(HoverEvent(HoverEvent.Action.SHOW_ITEM, HoverEvent.ItemStackContent(itemStack)))
    }

    fun MutableText.setClickEvent(clickEvent: ClickEvent): MutableText {
        return  this.setStyle(this.style.withClickEvent(clickEvent))
    }

    fun MutableText.setClickEvent(action: ClickEvent.Action, value: String) : MutableText {
        return this.setClickEvent(ClickEvent(action, value))
    }

    /**
     * Creates a new [MutableText] displaying [text] and showing [hoverText] when it is hovered.
     * [hoverText] can include "\n" for new lines.
     *
     * Use [MutableText.append] to combine multiple Chat components into one.
     * Use the formatting characters to format the text.
     */
    @JvmOverloads
    fun createHoverableText(text: String, hoverText: String, reformat: Boolean = true): MutableText {
        return literalText(text, reformat).setHoverText(hoverText, reformat)
    }
}