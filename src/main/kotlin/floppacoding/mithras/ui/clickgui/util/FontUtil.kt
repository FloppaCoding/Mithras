package floppacoding.mithras.ui.clickgui.util

import floppacoding.mithras.Mithras.mc
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.StringVisitable
import net.minecraft.util.Formatting
import java.util.*

/**
 * Provides methods for rending text.
 *
 * @author Aton
 */
object FontUtil {
    private var fontRenderer: TextRenderer? = null
    fun setupFontUtils() {
        fontRenderer = mc.textRenderer
    }

    fun getStringWidth(text: String?): Int {
        return fontRenderer!!.getWidth(Formatting.strip(text))
    }

    fun getSplitHeight(text: String, wrapWidth: Int): Int {
        var dy = 0
        for (s in mc.textRenderer.wrapLines(StringVisitable.plain(text), wrapWidth)) {
            dy += mc.textRenderer.fontHeight
        }
        return dy
    }

    val fontHeight: Int
        get() = fontRenderer!!.fontHeight
    fun drawString(context: DrawContext, text: String, x: Double, y: Double, color: Int = ColorUtil.TEXT_COLOR, shadow: Boolean = false) {
        drawString(context, text, x.toInt(), y.toInt(), color, shadow)
    }

    fun drawString(context: DrawContext, text: String, x: Int, y: Int, color: Int = ColorUtil.TEXT_COLOR, shadow: Boolean = false) {
        context.drawText(fontRenderer, text, x, y, color, shadow)
    }

    fun drawStringWithShadow(context: DrawContext, text: String, x: Double, y: Double, color: Int = ColorUtil.TEXT_COLOR) {
        context.drawTextWithShadow(fontRenderer, text, x.toInt(), y.toInt(), color)
    }

    fun drawCenteredString(context: DrawContext, text: String, x: Double, y: Double, color: Int = ColorUtil.TEXT_COLOR) {
        drawString(context, text, x - fontRenderer!!.getWidth(text) / 2, y, color)
    }

    fun drawCenteredStringWithShadow(context: DrawContext, text: String, x: Double, y: Double, color: Int = ColorUtil.TEXT_COLOR) {
        drawStringWithShadow(context, text, x - fontRenderer!!.getWidth(text) / 2, y, color)
    }

    fun drawTotalCenteredString(context: DrawContext, text: String, x: Double, y: Double, color: Int = ColorUtil.TEXT_COLOR) {
        drawString(context, text, x - fontRenderer!!.getWidth(text) / 2, y - fontRenderer!!.fontHeight / 2, color)
    }

    fun drawTotalCenteredStringWithShadow(context: DrawContext, text: String, x: Double, y: Double, color: Int = ColorUtil.TEXT_COLOR) {
        drawStringWithShadow(
            context,
            text,
            x - fontRenderer!!.getWidth(text) / 2,
            y - fontRenderer!!.fontHeight / 2f,
            color
        )
    }

    /**
     * Draws a string with line wrapping.
     */
    fun drawSplitString(context: DrawContext, text: String, x: Int, y: Int, wrapWidth: Int, color: Int = ColorUtil.TEXT_COLOR) {
        context.drawTextWrapped(fontRenderer, StringVisitable.plain(text), x, y, wrapWidth, color)
    }

    /**
     * Returns a copy of the String where the first letter is capitalized.
     */
    fun String.forceCapitalize(): String {
        return this.substring(0, 1).uppercase(Locale.getDefault()) + this.substring(1, this.length)
    }

    /**
     * Returns a copy of the String where the only first letter is capitalized.
     */
    fun String.capitalizeOnlyFirst(): String {
        return this.substring(0, 1).uppercase(Locale.getDefault()) + this.substring(1, this.length).lowercase()
    }
}