package floppacoding.mithras.utils

import floppacoding.mithras.Mithras
import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.module.impl.render.ClickGui
import net.minecraft.text.Text
import net.minecraft.util.*

/**
 * ## A collection of utility functions for creating and sending or displaying chat messages.
 *
 * Use [chatMessage] to put messages in chat which are only visible locally and are not sent to the server.
 *
 * Use [modMessage] for client side messages in chat that start with mods chat prefix.
 *
 * Use [sendChat] for sending a player message to the server.
 *
 * Use [command] to execute commands either client side or send them to the server.
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
     * Remove control codes from the [receiver][String] with the [vanilla function][StringUtils.stripControlCodes] for it.
     */
    fun String.stripControlCodes(): String {
        return Formatting.strip(this) ?: ""
    }

    /**
     * Puts a message in chat client side with the mod prefix.
     * @param reformat Replace the "&" in formatting strings with "§".
     * @see chatMessage
     */
    fun modMessage(text: String, reformat: Boolean = true) {
        val message: Text = Text.literal(if (reformat) reformatString(text) else text)
        modMessage(message)
    }

    /**
     * Puts a message in chat client side with the mod prefix.
     * @see chatMessage
     */
    fun modMessage(message: Text) = chatMessage(
        Text.literal(
            when (ClickGui.prefixStyle.index) {
                0 -> Mithras.CHAT_PREFIX; 1 -> Mithras.SHORT_PREFIX
                else -> reformatString( ClickGui.customPrefix.text)
            } + " "
        ).append(message)
    )

    /**
     * Print a message in chat **client side**.
     * @param reformat Replace the "&" in formatting strings with "§".
     * @see modMessage
     * @see sendChat
     */
    fun chatMessage(text: String, reformat: Boolean = true) {
        val message: Text = Text.literal(if (reformat) reformatString(text) else text)
        chatMessage(message)
    }

    /**
     * Print a message in chat **client side**.
     * @see modMessage
     * @see sendChat
     */
    fun chatMessage(message: Text) {
        mc.inGameHud?.chatHud?.addMessage(message)
    }

    /**
     * **Send player message to the server**.
     *
     * This mimics the player using the chat gui to send the message.
     * @see chatMessage
     * @see modMessage
     */
    fun sendChat(message: Text) {
        mc.player?.sendMessage(message)
    }

//    /**
//     * Runs the specified command. Per default sends it to the server  but has client side option.
//     * The input is assumed to **not** include the slash "/" that signals a command.
//     */
//    fun command(text: String, clientSide: Boolean = true) {
//        if (clientSide && mc.player != null) ClientCommandHandler.instance.executeCommand(mc.thePlayer, "/$text")
//        else mc.thePlayer?.sendChatMessage("/$text")
//    }

//    /**
//     * Creates a new IChatComponent displaying [text] and showing [hoverText] when it is hovered.
//     * [hoverText] can include "\n" for new lines.
//     *
//     * Use [IChatComponent.appendSibling] to combine multiple Chat components into one.
//     * Use the formatting characters to format the text.
//     */
//    fun createHoverableText(text: String, hoverText: String): IChatComponent {
//        val message: IChatComponent = ChatComponentText(text)
//        val style = ChatStyle()
//        style.chatHoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText(hoverText))
//        message.chatStyle = style
//        return message
//    }
}