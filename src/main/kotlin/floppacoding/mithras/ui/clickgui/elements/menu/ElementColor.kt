package floppacoding.mithras.ui.clickgui.elements.menu

import floppacoding.mithras.Mithras
import floppacoding.mithras.module.settings.impl.ColorSetting
import floppacoding.mithras.ui.clickgui.elements.Element
import floppacoding.mithras.ui.clickgui.elements.ElementType
import floppacoding.mithras.ui.clickgui.elements.ModuleButton
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import floppacoding.mithras.ui.clickgui.util.FontUtil
import net.minecraft.client.gui.DrawContext
import net.minecraft.util.Identifier
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

    override fun renderElement(context: DrawContext, mouseX: Int, mouseY: Int, partialTicks: Float): Int {
        val colorValue = setting.value.rgb

        FontUtil.drawString(context, displayName, 1, 2)

        /** Render the color preview */
        context.fill(width - 26, 2, width - 1, 11, colorValue)

        /** Render the tab indicating the drop-down */
        context.fill(0,  13, width, 15, ColorUtil.TAB_BACKGROUND_COLOR)
        context.fill((width * 0.4).toInt(), 12, (width * 0.6).toInt(), 15, ColorUtil.tabColor)

        /** Render the extended */
        if (extended) {
            context.fill(0, DEFAULT_HEIGHT,  width, height, ColorUtil.DROPDOWN_COLOR)
            var currentDrawY = DEFAULT_HEIGHT
            val increment = DEFAULT_HEIGHT

            /** Render the color sliders */
            for (currentColor in setting.colors()) {
                val isColorDragged = dragging == currentColor.ordinal
                /** For hue render the hue bar. */
                if (currentColor == ColorSetting.ColorComponent.HUE) {
                    context.matrices.push()
                    context.drawTexture(HUE_SCALE, 0, currentDrawY, 0f, 0f, width, 11, width, height)
                    context.matrices.pop()
                }

                val dispVal = "" + (setting.getNumber(currentColor) * 100.0).roundToInt() / 100.0
                FontUtil.drawString(context, currentColor.getName(), 1, currentDrawY + 2)
                FontUtil.drawString(context, dispVal, width - FontUtil.getStringWidth(dispVal), currentDrawY + 2)

                val maxVal = currentColor.maxValue()
                val percentage = setting.getNumber(currentColor)  / maxVal
                context.fill(0, currentDrawY + 12, width, currentDrawY + 13, -0xefeff0)
                context.fill(0, currentDrawY + 12, (percentage * width).toInt(), currentDrawY + 13, ColorUtil.sliderColor(isColorDragged))
                if (percentage > 0 && percentage < 1) context.fill(
                    (percentage * width - 1).toInt(),
                    (currentDrawY + 12), (percentage * width).toInt().coerceAtMost(width), currentDrawY + 13, ColorUtil.sliderKnobColor(isColorDragged)
                )

                /** Calculate and set new value when dragging */
                if (isColorDragged) {
                    val newVal = MathHelper.clamp(((mouseX - xAbsolute) / width.toDouble()), 0.0, 1.0) * maxVal
                    setting.setNumber(currentColor, newVal)
                }

                currentDrawY += increment
            }

        }


        return super.renderElement(context, mouseX, mouseY, partialTicks)
    }

    /**
     * Handles interaction with this element.
     * Returns true if interacted with the element to cancel further interactions.
     */
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
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
    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        dragging = null
    }

    /**
     * Check for arrow keys to move the slider by one increment.
     */
    override fun keyTyped(keyCode: Int, scanCode: Int): Boolean {
        if (!extended) return false
        val scaledMouseX = clickgui.getScaledMouseX()
        val scaledMouseY = clickgui.getScaledMouseY()

        var ay = DEFAULT_HEIGHT
        val increment = DEFAULT_HEIGHT
        for (currentColor in setting.colors()) {
            if (scaledMouseX >= xAbsolute && scaledMouseX <= xAbsolute + width && scaledMouseY >= yAbsolute + ay && scaledMouseY <= yAbsolute + ay + increment) {
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
        return super.keyTyped(keyCode, scanCode)
    }


    /**
     * Checks whether the mouse is hovering the selector
     */
    private fun isButtonHovered(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute && mouseY <= yAbsolute + 15
    }

    companion object {
        private val HUE_SCALE = Identifier(Mithras.RESOURCE_DOMAIN, "gui/huescale.png")
    }
}