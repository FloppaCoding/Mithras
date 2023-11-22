package floppacoding.mithras.ui.clickgui.advanced.elements.menu

import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.impl.ColorSetting
import floppacoding.mithras.ui.clickgui.advanced.AdvancedMenu
import floppacoding.mithras.ui.clickgui.advanced.elements.AdvancedElement
import floppacoding.mithras.ui.clickgui.advanced.elements.AdvancedElementType
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import floppacoding.mithras.ui.clickgui.util.ColorUtil.TEXT_COLOR
import floppacoding.aurora.core.images.ImageManager
import floppacoding.aurora.core.TextAlign
import net.minecraft.util.math.MathHelper
import org.lwjgl.glfw.GLFW
import kotlin.math.roundToInt

/**
 * Provides a color selector element for the advanced gui.
 *
 * @author Aton
 */
class AdvancedElementColor(
    parent: AdvancedMenu, module: Module, setting: ColorSetting,
) : AdvancedElement<ColorSetting>(parent, module, setting, AdvancedElementType.COLOR) {
    private var dragging: Int? = null

    /**
     * Renders the element
     */
    override fun renderElement(mouseX: Float, mouseY: Float, partialTicks: Float) : Float{
        val colorValue = setting.value.rgb

        // Render the box and text
        renderer.text(setting.name, 1f, 2f, TEXT_COLOR)

        // Render the color preview
        renderer.rect(settingWidth-26f,2f, 25f, 9f, colorValue)

        // Render the tab indicating the drop-down
        renderer.rect(0f, 13f, settingWidth, 2f, ColorUtil.TAB_BACKGROUND_COLOR)
        renderer.rect(settingWidth*0.4f, 12f, settingWidth*0.2f, 3f, ColorUtil.tabColor)


        // Render the extended
        var ay = 15f
        if (comboextended) {
            val startY = 15f
            renderer.rect(0f, startY, settingWidth, settingHeight- startY, ColorUtil.DROPDOWN_COLOR)
            val increment = 15f

            // Render the color sliders
            for (currentColor in setting.colors()) {
                val isColorDragged = dragging == currentColor.ordinal
                // If hue selected, render the hue bar.
                if (currentColor == ColorSetting.ColorComponent.HUE) {
                    renderer.image(ImageManager.HUE_SCALE, 0f, ay, settingWidth, 11f)
                }

                val dispVal = "" + (setting.getNumber(currentColor) * 100.0).roundToInt() / 100.0
                renderer.text(currentColor.getName(), 1f, ay + 2f, TEXT_COLOR)
                renderer.text(dispVal, settingWidth -1f, ay+ 2f, TEXT_COLOR, textAlign = TextAlign.RIGHT_TOP)

                val maxVal = currentColor.maxValue()
                val percentage = (setting.getNumber(currentColor)  / maxVal).toFloat()
                renderer.rect(0f, ay+12f, settingWidth, 1f, ColorUtil.SLIDER_BACKGROUND_COLOR)
                renderer.rect(0f, ay + 12f, percentage * settingWidth, 1f, ColorUtil.sliderColor(isColorDragged))
                if (percentage > 0 && percentage < 1) renderer.rect(
                    percentage * settingWidth - 1f,
                    ay + 12f,
                    1f, 1f, ColorUtil.sliderKnobColor(isColorDragged)
                )

                /** Calculate and set new value when dragging */
                if (dragging == currentColor.ordinal) {
                    val newVal = MathHelper.clamp((mouseX - parent.x - x) / settingWidth.toDouble(), 0.0, 1.0) * maxVal
                    setting.setNumber(currentColor, newVal)
                }

                ay += increment
            }

        }
        return ay
    }

    /**
     * Handles interaction with this element.
     * Returns true if interacted with the element to cancel further interactions.
     */
    override fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int): Boolean {
        if (mouseButton == 0) {
            if (isButtonHovered(mouseX, mouseY)) {
                // for now also extend on left click
                comboextended = !comboextended
                return true
            }

            if (!comboextended) return false
            var ay = y + 15
            val increment = 15
            for (currentColor in setting.colors()) {
                if (mouseX >= parent.x + x && mouseX <= parent.x + x + settingWidth && mouseY >= parent.y + ay && mouseY <= parent.y + ay + increment) {
                    dragging = currentColor.ordinal
                    return true
                }
                ay += 15
            }
        } else if( mouseButton == 1) {
            if (isButtonHovered(mouseX, mouseY)) {
                comboextended = !comboextended
                return true
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    /**
     * Stops slider action on mouse release
     */
    override fun mouseReleased(mouseX: Float, mouseY: Float, button: Int) {
        dragging = null
    }

    /**
     * Check for arrow keys to move the slider by one increment.
     */
    override fun keyPressed(keyCode: Int, scanCode: Int): Boolean {
        if (!comboextended) return false
        val  mouseX = clickgui.getMouseX()
        val  mouseY = clickgui.getMouseY()

        var ay = y + 15
        val increment = 15
        for (currentColor in setting.colors()) {

            if (mouseX >= parent.x + x && mouseX <= parent.x + x + settingWidth && mouseY >= parent.y + ay && mouseY <= parent.y + ay + increment) {
                if (keyCode == GLFW.GLFW_KEY_RIGHT){
                    setting.setNumber(currentColor, setting.getNumber(currentColor)+currentColor.maxValue()/255.0)
                }
                if (keyCode == GLFW.GLFW_KEY_LEFT){
                    setting.setNumber(currentColor, setting.getNumber(currentColor)-currentColor.maxValue()/255.0)
                }
                return true
            }

            ay += 15
        }
        return super.keyPressed(keyCode, scanCode)
    }


    /**
     * Checks whether the mouse is hovering the selector
     */
    private fun isButtonHovered(mouseX: Float, mouseY: Float): Boolean {
        return mouseX >= parent.x + x && mouseX <= parent.x + x + settingWidth && mouseY >= parent.y + y && mouseY <= parent.y + y + 15
    }
}