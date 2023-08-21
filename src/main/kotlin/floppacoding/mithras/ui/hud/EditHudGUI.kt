package floppacoding.mithras.ui.hud

import floppacoding.mithras.Mithras
import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import floppacoding.mithras.ui.nanovg.NVGR
import floppacoding.mithras.ui.nanovg.NVGScreen
import net.minecraft.util.math.MathHelper
import java.awt.Color

/**
 * The GUI for editing the positions and scale of HUD elements.
 *
 * @author Aton
 */
object EditHudGUI : NVGScreen("Edit Hud GUI", mc.options.guiScale.value.toFloat()) {

    private val hudElements: ArrayList<HudElement> = arrayListOf(

    )
    private var draggingElement: HudElement? = null
    private var startOffsetX : Float = 0f
    private var startOffsetY : Float = 0f

    private var buttonX: Float = 0f
    private var buttonY: Float = 0f
    private var buttonWidth: Float = 0f
    private var buttonHeight: Float = 0f

    fun addHUDElements(newElements: List<HudElement>) {
        val nonDuplicate = newElements.filter { !hudElements.contains(it) }
        hudElements.addAll(0, nonDuplicate)
    }

    /**
     * Draw a previes of all hud elements, regardless of whether they are visible.
     */
    override fun render(mouseX: Float, mouseY: Float, delta: Float) {
        this.scale = mc.options.guiScale.value.toFloat()

        // Render a reset Button
        renderResetButton(mouseX, mouseY)

        for (element in hudElements) {
            element.renderPreview()
        }
    }

    private fun renderResetButton(mouseX: Float, mouseY: Float) {
        val resetText = "Rest HUD"

        NVGR.push()
        val textWidth = NVGR.textWidth(resetText)
        val textHeight = NVGR.DEFAULT_FONT_HEIGHT
        val textX = -textWidth/2f + this.windowWidth / 2f
        // The height of the hotbar is 22
        val textY = -textHeight -5 -22 + this.windowHeight
        buttonX = textX -20
        buttonY = textY -5
        buttonHeight = textHeight + 10
        buttonWidth = textWidth + 40f

        val buttonColor = if (isCursorOnReset(mouseX, mouseY)) {
            Color(-0x00000000, false)
        }else {
            Color(-0x44eaeaeb, true).darker()
        }
        NVGR.rect(buttonX, buttonY, buttonWidth, buttonHeight, buttonColor.rgb)

        NVGR.text(resetText, textX, textY, ColorUtil.clickGUIColor.rgb)
        NVGR.pop()
    }

    override fun mouseScrolled(mouseX: Float, mouseY: Float, amount: Float): Boolean {
        var i = MathHelper.clamp(amount, -1f, 1f).toInt()
        if (i != 0) {
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

    override fun mouseClicked(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (button == 0) {
            if (isCursorOnReset(mouseX, mouseY)) {
                for (element in hudElements.reversed()) {
                    element.resetElement()
                }
                return true
            }else
                for (element in hudElements.reversed()) {
                    if (isCursorOnElement(mouseX, mouseY, element)) {
                        draggingElement = element
                        startOffsetX = mouseX - element.x
                        startOffsetY = mouseY - element.y
                        return true
                    }
                }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseDragged(mouseX: Float, mouseY: Float, button: Int, deltaX: Float, deltaY: Float): Boolean {
        if (button == 0 && draggingElement != null) {
            draggingElement!!.x = (mouseX - startOffsetX).coerceIn(0f, this.windowWidth - draggingElement!!.width * draggingElement!!.scale.value)
            draggingElement!!.y = (mouseY - startOffsetY).coerceIn(0f, this.windowHeight - draggingElement!!.height * draggingElement!!.scale.value)
            return true
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, button: Int): Boolean {
        draggingElement = null
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun close() {
        Mithras.moduleConfig.saveConfig()
        super.close()
    }

    private fun isCursorOnElement(mouseX: Float, mouseY: Float, element: HudElement): Boolean {
        return mouseX > element.x && mouseX < (element.x + element.width * element.scale.doubleValue)
                && mouseY > element.y&& mouseY< (element.y + element.height * element.scale.doubleValue)
    }

    private fun isCursorOnReset(mouseX: Float, mouseY: Float) : Boolean {
        return mouseX >= buttonX && mouseX < (buttonX + buttonWidth) && mouseY >= buttonY && mouseY < (buttonY + buttonHeight)
    }
}