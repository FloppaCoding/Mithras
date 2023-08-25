package floppacoding.mithras.ui.clickgui.advanced.elements.menu

import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.impl.SelectorOptions
import floppacoding.mithras.module.settings.impl.SelectorSetting
import floppacoding.mithras.ui.clickgui.advanced.AdvancedMenu
import floppacoding.mithras.ui.clickgui.advanced.elements.AdvancedElement
import floppacoding.mithras.ui.clickgui.advanced.elements.AdvancedElementType
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import floppacoding.mithras.ui.clickgui.util.FontUtil
import net.minecraft.client.gui.DrawContext
import java.awt.Color
import java.util.*

/**
 * Provides a selector element for the advanced gui.
 *
 * @author Aton
 */
class AdvancedElementSelector<T>(
    parent: AdvancedMenu, module: Module, setting: SelectorSetting<T>,
) : AdvancedElement<SelectorSetting<T>>(parent, module, setting, AdvancedElementType.SELECTOR)
        where T : SelectorOptions, T: Enum<T> {


    /**
	 * Renders the element
	 */
    override fun renderElement(context: DrawContext, mouseX: Int, mouseY: Int, partialTicks: Float) : Int {
        val temp = ColorUtil.clickGUIColor
        val color = Color(temp.red, temp.green, temp.blue, 150).rgb
        val displayValue = setting.selected

        /** Render the box and text */

        if (FontUtil.getStringWidth(displayValue + "00" + setting.name) <= settingWidth) {
            FontUtil.drawString(context, setting.name, 1, 2, -0x1)
            FontUtil.drawString(context, displayValue, settingWidth - FontUtil.getStringWidth(displayValue), 2, -0x1)
        } else {
            if (isButtonHovered(mouseX, mouseY)) {
                FontUtil.drawCenteredStringWithShadow(context, displayValue,  settingWidth / 2.0, 2.0, -0x1)
            } else {
                FontUtil.drawCenteredString(context, setting.name, settingWidth / 2.0, 2.0, -0x1)
            }
        }

        context.fill(0, 13, settingWidth, 15, 0x77000000)
        context.fill(
            (settingWidth * 0.4).toInt(),
            12,
            (settingWidth * 0.6).toInt(),
            15,
            color
            )

        var ay = 15
        if (comboextended) {
            val clr2 = temp.rgb

            val increment = FontUtil.fontHeight + 2
            for (option in setting.options) {

                val optionName = option.displayName
                context.fill(0, ay, settingWidth, ay + increment, -0x55ededee)
                val elementtitle =
                    optionName.substring(0, 1).uppercase(Locale.getDefault()) + optionName.substring(1, optionName.length)
                FontUtil.drawCenteredString(context, elementtitle, settingWidth / 2.0, ay + 2.0, -0x1)

                /** Highlights the element if it is selected */
                if (setting.isSelected(option)) {
                    context.fill(x, ay, 2, ay + increment, color)
                }
                /** Highlights the element when it is hovered */
                if (mouseX >= parent.x + x && mouseX <= parent.x + x + settingWidth && mouseY >= parent.y + y +  ay && mouseY < parent.y + y + ay + increment) {
                    context.fill(settingWidth - 1, ay, settingWidth, ay + increment, clr2)
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
                setting.index += 1
                return true
            }

            if (!comboextended) return false
            var ay = y + 15
            val increment = FontUtil.fontHeight + 2
            for (option in setting.options) {
                if (mouseX >= parent.x + x && mouseX <= parent.x + x + settingWidth && mouseY >= parent.y + ay && mouseY <= parent.y + ay + increment) {
                    setting.value = option
                    return true
                }
                ay += increment
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
     * Checks whether the mouse is hovering the selector
     */
    private fun isButtonHovered(mouseX: Int, mouseY: Int): Boolean {
        return (mouseX >= parent.x + x && mouseX <= parent.x + x + settingWidth && mouseY >= parent.y + y && mouseY <= parent.y + y + 15)
    }
}