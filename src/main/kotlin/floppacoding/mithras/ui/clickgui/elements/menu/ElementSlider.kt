package floppacoding.mithras.ui.clickgui.elements.menu

import floppacoding.mithras.module.settings.impl.NumberSetting
import floppacoding.mithras.ui.clickgui.elements.Element
import floppacoding.mithras.ui.clickgui.elements.ElementType
import floppacoding.mithras.ui.clickgui.elements.ModuleButton
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import floppacoding.mithras.ui.clickgui.util.FontUtil
import net.minecraft.client.gui.DrawContext
import net.minecraft.util.math.MathHelper
import org.lwjgl.glfw.GLFW
import kotlin.math.roundToInt

/**
 * Provides a slider element.
 *
 * @author Aton
 */
class ElementSlider(parent: ModuleButton, setting: NumberSetting) :
    Element<NumberSetting>(parent, setting, ElementType.SLIDER) {
    var dragging: Boolean = false

    override fun renderElement(context: DrawContext, mouseX: Int, mouseY: Int, partialTicks: Float): Int {
        val displayval = "" + (setting.value * 100.0).roundToInt() / 100.0
        val hoveredORdragged = isSliderHovered(mouseX, mouseY) || dragging
        val percentBar = (setting.value - setting.min) / (setting.max - setting.min)

        /** Render the text */
        FontUtil.drawString(context, displayName, 1, 2, )
        FontUtil.drawString(context, displayval, width - FontUtil.getStringWidth(displayval), 2)

        /** Render the slider */
        context.fill(0, 12, width, 13, ColorUtil.sliderBackground)
        context.fill(0, 12, (percentBar * width).toInt(), 13, ColorUtil.sliderColor(hoveredORdragged))
        if (percentBar > 0 && percentBar < 1) context.fill(
            (percentBar * width - 1).toInt(), 12, ((percentBar * width).toInt().coerceAtMost(width)), 13,
            ColorUtil.sliderKnobColor(hoveredORdragged)
        )

        /** Calculate and set new value when dragging */
        if (dragging) {
            val diff = setting.max - setting.min
            val newVal = setting.min + MathHelper.clamp(((mouseX - xAbsolute) / width.toDouble()), 0.0, 1.0) * diff
            setting.value = newVal
        }

        return super.renderElement(context, mouseX, mouseY, partialTicks)
    }

    /**
	 * Handles interaction with this element.
     * Returns true if interacted with the element to cancel further interactions.
	 */
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0 && isSliderHovered(mouseX, mouseY)) {
            dragging = true
            return true
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    /**
	 * Stops slider action on mouse release
	 */
    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        dragging = false
    }

    /**
     * Check for arrow keys to move the slider by one increment.
     */
    override fun keyTyped(keyCode: Int, scanCode: Int): Boolean {
        val scaledMouseX = clickgui.getScaledMouseX()
        val scaledMouseY = clickgui.getScaledMouseY()

        if (isSliderHovered(scaledMouseX, scaledMouseY)){
            if (keyCode == GLFW.GLFW_KEY_RIGHT){
                setting.value += setting.increment
                return true
            }
            if (keyCode == GLFW.GLFW_KEY_LEFT){
                setting.value -= setting.increment
                return true
            }
        }
        return super.keyTyped(keyCode, scanCode)
    }

    /**
	 * Checks whether the mouse is hovering the slider
	 */
    private fun isSliderHovered(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute  && mouseY <= yAbsolute + height
    }
}