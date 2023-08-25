package floppacoding.mithras.ui.clickguinano.advanced.elements.menu

import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.impl.SelectorOptions
import floppacoding.mithras.module.settings.impl.SelectorSetting
import floppacoding.mithras.ui.clickguinano.advanced.AdvancedMenu
import floppacoding.mithras.ui.clickguinano.advanced.elements.AdvancedElement
import floppacoding.mithras.ui.clickguinano.advanced.elements.AdvancedElementType
import floppacoding.mithras.ui.clickguinano.util.ColorUtil
import floppacoding.mithras.ui.nanovg.NVGR
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
        val textWidth = NVGR.textWidth(displayValue + "00" + setting.name)

        /** Render the box and text */
        if (textWidth <= settingWidth) {
            NVGR.text(setting.name, 1f, 2f, ColorUtil.TEXT_COLOR)
            NVGR.text(displayValue, settingWidth-1f, 2f, ColorUtil.TEXT_COLOR, textAlign = NVGR.TextAlign.TOP_RIGHT)
        } else {
            if (isButtonHovered(mouseX, mouseY)) {
                NVGR.text(displayValue, settingWidth / 2f, 2f, ColorUtil.TEXT_COLOR, textAlign = NVGR.TextAlign.CENTER_TOP)
            } else {
                NVGR.text(displayValue, settingWidth / 2f, 2f, ColorUtil.TEXT_COLOR, textAlign = NVGR.TextAlign.CENTER_TOP)
            }
        }

        // Render the tab indicating the drop-down
        NVGR.rect(0f, 13f, settingWidth, 2f, ColorUtil.TAB_BACKGROUND_COLOR)
        NVGR.rect(settingWidth*0.4f, 12f, settingWidth*0.2f, 3f, ColorUtil.tabColor)

        // Render the drop-down
        var ay = 15f
        if (comboextended) {
            val increment = NVGR.DEFAULT_FONT_HEIGHT + 2
            for (option in setting.options) {

                val optionName = option.displayName
                NVGR.rect(0f, ay, settingWidth, increment, ColorUtil.DROPDOWN_COLOR)
                val elementtitle =
                    optionName.substring(0, 1).uppercase(Locale.getDefault()) + optionName.substring(1, optionName.length)
                NVGR.text(elementtitle, settingWidth/2f, ay + 2f, ColorUtil.TEXT_COLOR, textAlign = NVGR.TextAlign.CENTER_TOP)

                /** Highlights the element if it is selected */
                if (setting.isSelected(option)) {
                    NVGR.rect(0f, ay, 2f, increment, ColorUtil.clickGUIColor.rgb)
                }
                /** Highlights the element when it is hovered */
                if (mouseX >= parent.x + x && mouseX <= parent.x + x + settingWidth && mouseY >= parent.y + y +  ay && mouseY < parent.y + y + ay + increment) {
                    NVGR.rect(settingWidth-1f, ay, 1f, increment, ColorUtil.clickGUIColor.rgb)
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
            val increment = NVGR.DEFAULT_FONT_HEIGHT + 2
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