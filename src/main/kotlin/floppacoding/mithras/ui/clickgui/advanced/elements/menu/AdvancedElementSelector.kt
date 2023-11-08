package floppacoding.mithras.ui.clickgui.advanced.elements.menu

import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.impl.SelectorOptions
import floppacoding.mithras.module.settings.impl.SelectorSetting
import floppacoding.mithras.ui.clickgui.advanced.AdvancedMenu
import floppacoding.mithras.ui.clickgui.advanced.elements.AdvancedElement
import floppacoding.mithras.ui.clickgui.advanced.elements.AdvancedElementType
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import floppacoding.mithras.utils.render.TextAlign
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
    override fun renderElement(mouseX: Float, mouseY: Float, partialTicks: Float) : Float {
        val displayValue = setting.selected
        val textWidth = renderer.textWidth(displayValue + "00" + setting.name)

        /** Render the box and text */
        if (textWidth <= settingWidth) {
            renderer.text(setting.name, 1f, 2f, ColorUtil.TEXT_COLOR)
            renderer.text(displayValue, settingWidth-1f, 2f, ColorUtil.TEXT_COLOR, textAlign = TextAlign.RIGHT_TOP)
        } else {
            if (isButtonHovered(mouseX, mouseY)) {
                renderer.text(displayValue, settingWidth / 2f, 2f, ColorUtil.TEXT_COLOR, textAlign = TextAlign.CENTER_TOP)
            } else {
                renderer.text(displayValue, settingWidth / 2f, 2f, ColorUtil.TEXT_COLOR, textAlign = TextAlign.CENTER_TOP)
            }
        }

        // Render the tab indicating the drop-down
        renderer.rect(0f, 13f, settingWidth, 2f, ColorUtil.TAB_BACKGROUND_COLOR)
        renderer.rect(settingWidth*0.4f, 12f, settingWidth*0.2f, 3f, ColorUtil.tabColor)

        // Render the drop-down
        var ay = 15f
        if (comboextended) {
            val increment = renderer.defaultFontHeight + 2
            for (option in setting.options) {

                val optionName = option.displayName
                renderer.rect(0f, ay, settingWidth, increment, ColorUtil.DROPDOWN_COLOR)
                val elementtitle =
                    optionName.substring(0, 1).uppercase(Locale.getDefault()) + optionName.substring(1, optionName.length)
                renderer.text(elementtitle, settingWidth/2f, ay + 2f, ColorUtil.TEXT_COLOR, textAlign = TextAlign.CENTER_TOP)

                /** Highlights the element if it is selected */
                if (setting.isSelected(option)) {
                    renderer.rect(0f, ay, 2f, increment, ColorUtil.clickGUIColor.rgb)
                }
                /** Highlights the element when it is hovered */
                if (mouseX >= parent.x + x && mouseX <= parent.x + x + settingWidth && mouseY >= parent.y + y +  ay && mouseY < parent.y + y + ay + increment) {
                    renderer.rect(settingWidth-1f, ay, 1f, increment, ColorUtil.clickGUIColor.rgb)
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
                setting.index += 1
                return true
            }

            if (!comboextended) return false
            var ay = y + 15f
            val increment = renderer.defaultFontHeight + 2
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
    private fun isButtonHovered(mouseX: Float, mouseY: Float): Boolean {
        return (mouseX >= parent.x + x && mouseX <= parent.x + x + settingWidth && mouseY >= parent.y + y && mouseY <= parent.y + y + 15)
    }
}