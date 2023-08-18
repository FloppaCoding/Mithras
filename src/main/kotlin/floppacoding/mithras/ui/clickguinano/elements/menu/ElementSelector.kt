package floppacoding.mithras.ui.clickguinano.elements.menu

import floppacoding.mithras.module.settings.impl.Options
import floppacoding.mithras.module.settings.impl.SelectorSetting
import floppacoding.mithras.ui.clickguinano.elements.Element
import floppacoding.mithras.ui.clickguinano.elements.ElementType
import floppacoding.mithras.ui.clickguinano.elements.ModuleButton
import floppacoding.mithras.ui.clickguinano.util.ColorUtil
import floppacoding.mithras.ui.nanovg.NVGR
import java.util.*

/**
 * Provides a selector element.
 *
 * @author Aton
 */
class ElementSelector<T>(parent: ModuleButton, setting: SelectorSetting<T>) :
    Element<SelectorSetting<T>>(parent, setting, ElementType.SELECTOR)
        where T : Options, T : Enum<T> {


    override fun renderElement(mouseX: Float, mouseY: Float, partialTicks: Float): Float {
        val displayValue = (setting as SelectorSetting<*>).selected
        val textWidth = NVGR.textWidth(displayValue + "00" + displayName)

        // Render the text.
        if (textWidth <= width) {
            NVGR.text(displayName, 1f, 2f, ColorUtil.TEXT_COLOR)
            NVGR.text(displayValue, width-1f, 2f, ColorUtil.TEXT_COLOR, textAlign = NVGR.TextAlign.TOP_RIGHT)
        } else {
            if (isButtonHovered(mouseX, mouseY)) {
                NVGR.text(displayValue, width / 2f, 2f, ColorUtil.TEXT_COLOR, textAlign = NVGR.TextAlign.CENTER_TOP)
            } else {
                NVGR.text(displayValue, width / 2f, 2f, ColorUtil.TEXT_COLOR, textAlign = NVGR.TextAlign.CENTER_TOP)
            }
        }

        // Render the tab indicating the drop-down
        NVGR.rect(0f, 13f, width, 2f, ColorUtil.TAB_BACKGROUND_COLOR)
        NVGR.rect(width*0.4f, 12f, width*0.2f, 3f, ColorUtil.tabColor)

        // Render the dropdown
        if (extended) {
            var ay = DEFAULT_HEIGHT
            val increment = NVGR.DEFAULT_FONT_HEIGHT + 2f
            for (option in setting.options) {
                NVGR.rect(0f, ay, width, increment, ColorUtil.DROPDOWN_COLOR)
                val optionName = option.displayName
                val elementtitle =
                    optionName.substring(0, 1).uppercase(Locale.getDefault()) + optionName.substring(1, optionName.length)
                NVGR.text(elementtitle, width/2f, ay + 2f, ColorUtil.TEXT_COLOR, textAlign = NVGR.TextAlign.CENTER_TOP)

                /** Highlight the element if it is selected */
                if (setting.isSelected(option)) {
                    NVGR.rect(0f, ay, 2f, increment, ColorUtil.clickGUIColor.rgb)
                }
                /** Highlight the element when it is hovered */
                if (mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute + ay && mouseY < yAbsolute + ay + increment) {
                    NVGR.rect(width-1f, ay, 1f, increment, ColorUtil.clickGUIColor.rgb)
                }
                ay += increment
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
                setting.index += 1
                return true
            }

            if (!extended) return false
            var ay = DEFAULT_HEIGHT
            val increment = NVGR.DEFAULT_FONT_HEIGHT + 2
            for (option in setting.options) {
                if (mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute + ay && mouseY <= yAbsolute + ay + increment) {
                    setting.value = option
                    return true
                }
                ay += increment
            }
        } else if (mouseButton == 1) {
            if (isButtonHovered(mouseX, mouseY)) {
                extended = !extended
                return true
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    /**
     * Checks whether the mouse is hovering the selector
     */
    private fun isButtonHovered(mouseX: Float, mouseY: Float): Boolean {
        return (mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute && mouseY <= yAbsolute + DEFAULT_HEIGHT)
    }
}