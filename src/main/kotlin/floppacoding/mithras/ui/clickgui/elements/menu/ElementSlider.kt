package floppacoding.mithras.ui.clickgui.elements.menu

import floppacoding.mithras.module.settings.impl.NumberSetting
import floppacoding.mithras.ui.clickgui.elements.Element
import floppacoding.mithras.ui.clickgui.elements.ElementType
import floppacoding.mithras.ui.clickgui.elements.ModuleButton
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import floppacoding.aurora.core.TextAlign
import net.minecraft.util.math.MathHelper
import org.lwjgl.glfw.GLFW
import kotlin.math.roundToInt

/**
 * Provides a slider element.
 *
 * @author Aton
 */
class ElementSlider(parent: ModuleButton, setting: NumberSetting<*>) :
    Element<NumberSetting<*>>(parent, setting, ElementType.SLIDER) {
    var dragging: Boolean = false

    override fun renderElement(mouseX: Float, mouseY: Float, partialTicks: Float): Float {
        val displayVal = "" + (setting.doubleValue * 100.0).roundToInt() / 100.0
        val hoveredORdragged = isSliderHovered(mouseX, mouseY) || dragging
        val percentBar = ((setting.doubleValue - setting.minDouble) / (setting.maxDouble - setting.minDouble)).toFloat()

        /** Render the text */
        renderer.text(displayName, 1f, 2f, ColorUtil.TEXT_COLOR)
        renderer.text(displayVal, width-1f, 2f, ColorUtil.TEXT_COLOR, textAlign = TextAlign.RIGHT_TOP)

        /** Render the slider */
        renderer.rect(0f, 12f, width, 1f, ColorUtil.SLIDER_BACKGROUND_COLOR)
        renderer.rect(0f, 12f, percentBar*width, 1f, ColorUtil.sliderColor(hoveredORdragged))
        if (percentBar > 0 && percentBar < 1) renderer.rect(
            percentBar * width - 1,
            12f, 1f, 1f,
            ColorUtil.sliderKnobColor(hoveredORdragged)
        )

        /** Calculate and set new value when dragging */
        if (dragging) {
            val diff = setting.maxDouble - setting.minDouble
            val newVal = setting.minDouble + MathHelper.clamp(((mouseX - xAbsolute) / width.toDouble()), 0.0, 1.0) * diff
            setting.doubleValue = newVal
        }

        return super.renderElement(mouseX, mouseY, partialTicks)
    }

    /**
	 * Handles interaction with this element.
     * Returns true if interacted with the element to cancel further interactions.
	 */
    override fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int): Boolean {
        if (mouseButton == 0 && isSliderHovered(mouseX, mouseY)) {
            dragging = true
            return true
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    /**
	 * Stops slider action on mouse release
	 */
    override fun mouseReleased(mouseX: Float, mouseY: Float, state: Int) {
        dragging = false
    }

    /**
     * Check for arrow keys to move the slider by one increment.
     */
    override fun keyPressed(keyCode: Int, scanCode: Int): Boolean {
        val mouseX = clickgui.getMouseX()
        val mouseY = clickgui.getMouseY()

        if (isSliderHovered(mouseX, mouseY)){
            if (keyCode == GLFW.GLFW_KEY_RIGHT){
                setting.doubleValue += setting.incrementDouble
                return true
            }
            if (keyCode == GLFW.GLFW_KEY_LEFT){
                setting.doubleValue -= setting.incrementDouble
                return true
            }
        }
        return super.keyPressed(keyCode, scanCode)
    }

    /**
	 * Checks whether the mouse is hovering the slider
	 */
    private fun isSliderHovered(mouseX: Float, mouseY: Float): Boolean {
        return mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute  && mouseY <= yAbsolute + height
    }
}