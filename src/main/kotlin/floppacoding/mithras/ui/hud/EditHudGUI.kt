package floppacoding.mithras.ui.hud

import floppacoding.mithras.Mithras
import floppacoding.mithras.Mithras.mc
//import floppacoding.mithras.floppamap.dungeon.MapRender
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import floppacoding.mithras.ui.clickgui.util.FontUtil
import floppacoding.mithras.utils.render.ExperimentalRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.LiteralTextContent
import net.minecraft.text.MutableText
import net.minecraft.util.math.MathHelper
import java.awt.Color
import java.io.IOException

/**
 * The GUI for editing the positions and scale of HUD elements.
 *
 * @author Aton
 */
object EditHudGUI : Screen(MutableText.of(LiteralTextContent("Edit Hud GUI"))) {

    private val hudElements: ArrayList<HudElement> = arrayListOf(

    )
    private var draggingElement: HudElement? = null
    private var startOffsetX = 0
    private var startOffsetY = 0

    fun addHUDElements(newElements: List<HudElement>) {
        val nonDuplicate = newElements.filter { !hudElements.contains(it) }
        hudElements.addAll(0, nonDuplicate)
    }

    /**
     * Draw a previes of all hud elements, regardless of whether they are visible.
     */
    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, partialTicks: Float) {

        ExperimentalRenderer.draw()

        // Render a reset Button
        renderRestButton(context, mouseX, mouseY, partialTicks)

        for (element in hudElements) {
            element.renderPreview(context)
        }

        super.render(context, mouseX, mouseY, partialTicks)
    }

    private fun renderRestButton(context: DrawContext, mouseX: Int, mouseY: Int, @Suppress("UNUSED_PARAMETER") partialTicks: Float) {
        val resetText = "Rest HUD"
        val window = mc.window

        context.matrices.push()
        context.matrices.translate(
            window.scaledWidth.toDouble() / 2.0,
            window.scaledHeight.toDouble(),
            0.0
        )
        // Note: if you change these values they also have to be changed in isCursorOnReset
        val textWidth = FontUtil.getStringWidth(resetText)
        val textHeight = FontUtil.fontHeight.toDouble()
        val textX = -textWidth/2.0
        val textY = -textHeight -25
        val boxX = textX -20
        val boxY = textY -5
        val boxHeight = textHeight + 10
        val boxWidth = textWidth + 40.0

        val buttonColor = if (isCursorOnReset(mouseX, mouseY)) {
            Color(-0x00000000, false)
        }else {
            Color(-0x44eaeaeb, true).darker()
        }
        context.fill(boxX.toInt(), boxY.toInt(), (boxX + boxWidth).toInt(), (boxY + boxHeight).toInt(), buttonColor.rgb)

        FontUtil.drawString(context, resetText, textX, textY, ColorUtil.clickGUIColor.rgb)
        context.matrices.pop()
    }

    @Throws(IOException::class)
    override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
//        val mouseX = mc.mouse.x * super.width / mc.window.width
//        val mouseY = super.height - mc.mouse.y * super.height / mc.window.height - 1

        //Scaling mouse coords neccessary here if the gui scale is changed

        var i = MathHelper.clamp(amount, -1.0, 1.0).toInt()
        if (i != 0) {
            if (i > 1) {
                i = 1
            }
            if (i < -1) {
                i = -1
            }
            if (hasShiftDown()) {
                i *= 7
            }
            /** Check all hud elements for scroll action. this is used to change the scale
             * Reversed order is used to guarantee that the panel rendered on top will be handled first. */
            for (element in hudElements.reversed()) {
                if (isCursorOnElement(mouseX, mouseY, element)) {
                    element.scroll(i)
                    return true
                }
            }
        }
        return super.mouseScrolled(mouseX, mouseY, amount)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int): Boolean {
        if (mouseButton == 0) {
            if (isCursorOnReset(mouseX.toInt(), mouseY.toInt())) {
                for (element in hudElements.reversed()) {
                    element.resetElement()
                }
                return true
            }else
                for (element in hudElements.reversed()) {
                    if (isCursorOnElement(mouseX, mouseY, element)) {
                        draggingElement = element
                        startOffsetX = mouseX.toInt() - element.x
                        startOffsetY = mouseY.toInt() - element.y
                        return true
                    }
                }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (button == 0 && draggingElement != null) {
            draggingElement!!.x = mouseX.toInt() - startOffsetX
            draggingElement!!.y = mouseY.toInt() - startOffsetY
            return true
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, state: Int): Boolean {
        draggingElement = null
        return super.mouseReleased(mouseX, mouseY, state)
    }

    override fun close() {
        Mithras.moduleConfig.saveConfig()
        super.close()
    }

    override fun shouldPause(): Boolean {
        return false
    }

    private fun isCursorOnElement(mouseX: Double, mouseY: Double, element: HudElement): Boolean {
        return mouseX > element.x && mouseX < (element.x + element.width * element.scale.value)
                && mouseY > element.y&& mouseY< (element.y + element.height * element.scale.value)
    }

    private fun isCursorOnReset(mouseX: Int, mouseY: Int) : Boolean {
        val resetText = "Rest HUD"
        val window = mc.window
        // Note: if you change these values they also have to be changed in isCursorOnReset
        val textWidth = FontUtil.getStringWidth(resetText)
        val textHeight = FontUtil.fontHeight.toDouble()
        val textX = -textWidth/2.0 + window.scaledWidth.toDouble() / 2.0
        val textY = -textHeight -25 + window.scaledHeight.toDouble()
        val boxX = textX -20
        val boxY = textY -5
        val boxHeight = textHeight + 10
        val boxWidth = textWidth + 40.0
        return mouseX >= boxX && mouseX < (boxX + boxWidth) && mouseY >= boxY && mouseY < (boxY + boxHeight)
    }
}