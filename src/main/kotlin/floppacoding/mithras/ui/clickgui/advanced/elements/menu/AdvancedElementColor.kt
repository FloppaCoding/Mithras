package floppacoding.mithras.ui.clickgui.advanced.elements.menu

import floppacoding.mithras.Mithras.RESOURCE_DOMAIN
import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.impl.ColorSetting
import floppacoding.mithras.ui.clickgui.advanced.AdvancedMenu
import floppacoding.mithras.ui.clickgui.advanced.elements.AdvancedElement
import floppacoding.mithras.ui.clickgui.advanced.elements.AdvancedElementType
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import floppacoding.mithras.ui.clickgui.util.FontUtil
import net.minecraft.client.gui.DrawContext
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import org.lwjgl.glfw.GLFW
import java.awt.Color
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

    private val hueScale = Identifier(RESOURCE_DOMAIN, "gui/huescale.png")

    /**
     * Renders the element
     */
    override fun renderElement(context: DrawContext, mouseX: Int, mouseY: Int, partialTicks: Float) : Int{
        val temp = ColorUtil.clickGUIColor
        val color = Color(temp.red, temp.green, temp.blue, 150).rgb
        val color2 = Color(temp.red, temp.green, temp.blue,  230).rgb

        val colorValue = setting.value.rgb

        //<editor-fold desc="Render the box and text">
        FontUtil.drawString(context, setting.name, 1, 2, -0x1)

        // Render the color preview
        context.fill(
            settingWidth - 26,
            2,
            settingWidth - 1,
            11,
            colorValue
        )

        // Render the tab indicating the drop-down
        context.fill(0, 13, settingWidth, 15, 0x77000000)
        context.fill(
            (settingWidth * 0.4).toInt(),
            12,
            (settingWidth * 0.6).toInt(),
            15,
            color
        )
        //</editor-fold>

        // Render the extended
        var ay = 15
        if (comboextended) {
            val startY = 15
            context.fill(0, startY, settingWidth, settingHeight, -0x55ededee)
            val increment = 15

            // Render the color sliders
            for (currentColor in setting.colors()) {

                // If hue selected, render the hue bar.
                if (currentColor == ColorSetting.ColorComponent.HUE) {
                    hueScale.let {
                        context.matrices.push()
                        context.drawTexture(hueScale, 0, ay, 0f, 0f, settingWidth, 11, settingWidth, 11)
                        context.matrices.pop()
                    }
                }

                val dispVal = "" + (setting.getNumber(currentColor) * 100.0).roundToInt() / 100.0
                FontUtil.drawString(context, currentColor.getName(), 1, ay + 2, -0x1)
                FontUtil.drawString(context, dispVal, settingWidth - FontUtil.getStringWidth(dispVal), ay + 2, -0x1)

                val maxVal = currentColor.maxValue()
                val percentage = setting.getNumber(currentColor)  / maxVal
                context.fill(0, (ay + 12), settingWidth, ay + 14, -0xefeff0)
                context.fill(0, (ay + 12), (percentage * settingWidth).toInt(), ay + 14, color)
                if (percentage > 0 && percentage < 1) context.fill(
                    (percentage * settingWidth - 2).toInt(), ay + 12, ((percentage * settingWidth).toInt().coerceAtMost(settingWidth)), ay + 14, color2
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
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
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
    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        dragging = null
    }

    /**
     * Check for arrow keys to move the slider by one increment.
     */
    override fun keyTyped(keyCode: Int, scanCode: Int): Boolean {
        if (!comboextended) return false
        val scaledresolution = mc.window
        val i1: Int = scaledresolution.scaledWidth
        val j1: Int = scaledresolution.scaledHeight
        val k1: Int = mc.mouse.x.toInt() * i1 / mc.window.width
        val l1: Int = j1 - mc.mouse.y.toInt() * j1 / mc.window.height - 1
        val scale = 2.0 / mc.options.guiScale.value
        val scaledMouseX = (k1 / scale).toInt()
        val scaledMouseY = (l1 / scale).toInt()

        var ay = y + 15
        val increment = 15
        for (currentColor in setting.colors()) {

            if (scaledMouseX >= parent.x + x && scaledMouseX <= parent.x + x + settingWidth && scaledMouseY >= parent.y + ay && scaledMouseY <= parent.y + ay + increment) {
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
        return super.keyTyped(keyCode, scanCode)
    }


    /**
     * Checks whether the mouse is hovering the selector
     */
    private fun isButtonHovered(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= parent.x + x && mouseX <= parent.x + x + settingWidth && mouseY >= parent.y + y && mouseY <= parent.y + y + 15
    }
}