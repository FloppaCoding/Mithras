package floppacoding.mithras.ui.clickgui.advanced.elements.menu

import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.impl.NumberSetting
import floppacoding.mithras.ui.clickgui.advanced.AdvancedMenu
import floppacoding.mithras.ui.clickgui.advanced.elements.AdvancedElement
import floppacoding.mithras.ui.clickgui.advanced.elements.AdvancedElementType
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import floppacoding.mithras.utils.render.TextAlign
import net.minecraft.util.math.MathHelper
import org.lwjgl.glfw.GLFW
import kotlin.math.roundToInt

/**
 * Provides a slider element for the advanced gui.
 *
 * @author Aton
 */
class AdvancedElementSlider(
    parent: AdvancedMenu, module: Module, setting: NumberSetting<*>,
) : AdvancedElement<NumberSetting<*>>(parent, module, setting, AdvancedElementType.SLIDER) {
    private var dragging: Boolean = false

    /**
	 * Renders the element
	 */
    override fun renderElement(mouseX: Float, mouseY: Float, partialTicks: Float) : Float{
        val displayVal = "" + (setting.doubleValue * 100.0).roundToInt() / 100.0
        val hoveredORdragged = isSliderHovered(mouseX, mouseY) || dragging
        val percentBar = ((setting.doubleValue - setting.minDouble) / (setting.maxDouble - setting.minDouble)).toFloat()

        /** Render the text */
        renderer.text(setting.name, 1f, 2f, ColorUtil.TEXT_COLOR)
        renderer.text(displayVal, settingWidth-1f, 2f, ColorUtil.TEXT_COLOR, textAlign = TextAlign.TOP_RIGHT)

        /** Render the slider */
        renderer.rect(0f, 12f, settingWidth, 1f, ColorUtil.SLIDER_BACKGROUND_COLOR)
        renderer.rect(0f, 12f, percentBar*settingWidth, 1f, ColorUtil.sliderColor(hoveredORdragged))
        if (percentBar > 0 && percentBar < 1) renderer.rect(
            percentBar * settingWidth - 1f,
            12f, 1f, 1f,
            ColorUtil.sliderKnobColor(hoveredORdragged)
        )

        /** Calculate and set new value when dragging */
        if (dragging) {
            val diff = setting.maxDouble - setting.minDouble
            val newVal = setting.minDouble + MathHelper.clamp(((mouseX - parent.x - x) / settingWidth.toDouble()), 0.0, 1.0) * diff
            setting.doubleValue = newVal
        }

       return this.settingHeight
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
    override fun mouseReleased(mouseX: Float, mouseY: Float, button: Int) {
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
        return mouseX >= parent.x + x && mouseX <= parent.x + x + settingWidth && mouseY >= parent.y + y  && mouseY <= parent.y + y + settingHeight
    }
}