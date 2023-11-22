package floppacoding.mithras.ui.clickgui.elements.menu

import floppacoding.mithras.module.settings.impl.ColorSetting
import floppacoding.mithras.ui.clickgui.elements.Element
import floppacoding.mithras.ui.clickgui.elements.ElementType
import floppacoding.mithras.ui.clickgui.elements.ModuleButton
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import floppacoding.aurora.core.images.ImageManager
import floppacoding.aurora.core.TextAlign
import net.minecraft.util.math.MathHelper
import org.lwjgl.glfw.GLFW
import kotlin.math.roundToInt

/**
 * Provides a color selector element.
 *
 * @author Aton
 */
class ElementColor(parent: ModuleButton, setting: ColorSetting) :
    Element<ColorSetting>(parent, setting, ElementType.COLOR) {
    var dragging: Int? = null

    override fun renderElement(mouseX: Float, mouseY: Float, partialTicks: Float): Float {
        val colorValue = setting.value.rgb

        renderer.text(displayName, 1f, 2f, ColorUtil.TEXT_COLOR)

        /** Render the color preview */
        renderer.rect(width - 26f, 2f, 25f, 10f, colorValue)

        /** Render the tab indicating the drop-down */
        renderer.rect(0f, 13f, width, 2f, ColorUtil.TAB_BACKGROUND_COLOR)
        renderer.rect(width*0.4f, 12f, width*0.2f, 3f, ColorUtil.tabColor)

        /** Render the extended */
        if (extended) {
            renderer.rect(0f, DEFAULT_HEIGHT, width, height- DEFAULT_HEIGHT, ColorUtil.DROPDOWN_COLOR)
            var currentDrawY = DEFAULT_HEIGHT
            val increment = DEFAULT_HEIGHT

            /** Render the color sliders */
            for (currentColor in setting.colors()) {
                val isColorDragged = dragging == currentColor.ordinal
                /** For hue render the hue bar. */
                if (currentColor == ColorSetting.ColorComponent.HUE) {
                    renderer.image(ImageManager.HUE_SCALE, 0f, currentDrawY, width, 11f)
                }

                val dispVal = "" + (setting.getNumber(currentColor) * 100.0).roundToInt() / 100.0
                renderer.text(currentColor.getName(), 1f, currentDrawY +2f, ColorUtil.TEXT_COLOR)
                renderer.text(dispVal, width - 1f, currentDrawY +2f, ColorUtil.TEXT_COLOR, textAlign = TextAlign.RIGHT_TOP)

                val maxVal = currentColor.maxValue()
                val percentage = (setting.getNumber(currentColor)  / maxVal).toFloat()
                renderer.rect(0f, currentDrawY+12f, width, 1f, ColorUtil.SLIDER_BACKGROUND_COLOR)
                renderer.rect(0f, currentDrawY + 12f, percentage * width, 1f, ColorUtil.sliderColor(isColorDragged))
                if (percentage > 0 && percentage < 1) renderer.rect(
                    percentage * width - 1f,
                    currentDrawY + 12f,
                    1f, 1f, ColorUtil.sliderKnobColor(isColorDragged)
                )

                /** Calculate and set new value when dragging */
                if (isColorDragged) {
                    val newVal = MathHelper.clamp(((mouseX - xAbsolute) / width.toDouble()), 0.0, 1.0) * maxVal
                    setting.setNumber(currentColor, newVal)
                }

                currentDrawY += increment
            }

        }


        return super.renderElement(mouseX, mouseY, partialTicks)
    }

    /**
     * Handles interaction with this element.
     * Returns true if interacted with the element to cancel further interactions.
     */
    override fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int): Boolean {
        if (mouseButton == 0) {
            if (isButtonHovered(mouseX, mouseY)) {
                // for now also extend on left click
                extended = !extended
                return true
            }

            if (!extended) return false
            var ay = DEFAULT_HEIGHT
            val increment = DEFAULT_HEIGHT
            for (currentColor in setting.colors()) {
                if (mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute + ay && mouseY <= yAbsolute + ay + increment) {
                    dragging = currentColor.ordinal
                    return true
                }
                ay += increment
            }
        } else if( mouseButton == 1) {
            if (isButtonHovered(mouseX, mouseY)) {
                extended = !extended
                return true
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    /**
     * Stops slider action on mouse release
     */
    override fun mouseReleased(mouseX: Float, mouseY: Float, state: Int) {
        dragging = null
    }

    /**
     * Check for arrow keys to move the slider by one increment.
     */
    override fun keyPressed(keyCode: Int, scanCode: Int): Boolean {
        if (!extended) return false
        val mouseX = clickgui.getMouseX()
        val mouseY = clickgui.getMouseY()

        var ay = DEFAULT_HEIGHT
        val increment = DEFAULT_HEIGHT
        for (currentColor in setting.colors()) {
            if (mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute + ay && mouseY <= yAbsolute + ay + increment) {
                if (keyCode == GLFW.GLFW_KEY_RIGHT){
                    setting.setNumber(currentColor, setting.getNumber(currentColor)+currentColor.maxValue()/255.0)
                }
                if (keyCode == GLFW.GLFW_KEY_LEFT){
                    setting.setNumber(currentColor, setting.getNumber(currentColor)-currentColor.maxValue()/255.0)
                }
                return true
            }
            ay += increment
        }
        return super.keyPressed(keyCode, scanCode)
    }


    /**
     * Checks whether the mouse is hovering the selector
     */
    private fun isButtonHovered(mouseX: Float, mouseY: Float): Boolean {
        return mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute && mouseY <= yAbsolute + 15
    }
}