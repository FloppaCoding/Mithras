package floppacoding.mithras.ui.clickgui.elements.menu

import floppacoding.mithras.module.settings.impl.SelectorOptions
import floppacoding.mithras.module.settings.impl.SelectorSetting
import floppacoding.mithras.ui.clickgui.elements.Element
import floppacoding.mithras.ui.clickgui.elements.ElementType
import floppacoding.mithras.ui.clickgui.elements.ModuleButton
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import floppacoding.mithras.utils.render.TextAlign
import java.util.*

/**
 * Provides a selector element.
 *
 * @author Aton
 */
class ElementSelector<T>(parent: ModuleButton, setting: SelectorSetting<T>) :
    Element<SelectorSetting<T>>(parent, setting, ElementType.SELECTOR)
        where T : SelectorOptions, T : Enum<T> {


    override fun renderElement(mouseX: Float, mouseY: Float, partialTicks: Float): Float {
        val displayValue = (setting as SelectorSetting<*>).selected
        val textWidth = renderer.textWidth(displayValue + "00" + displayName)

        // Render the text.
        if (textWidth <= width) {
            renderer.text(displayName, 1f, 2f, ColorUtil.TEXT_COLOR)
            renderer.text(displayValue, width-1f, 2f, ColorUtil.TEXT_COLOR, textAlign = TextAlign.TOP_RIGHT)
        } else {
            if (isButtonHovered(mouseX, mouseY)) {
                renderer.text(displayValue, width / 2f, 2f, ColorUtil.TEXT_COLOR, textAlign = TextAlign.CENTER_TOP)
            } else {
                renderer.text(displayValue, width / 2f, 2f, ColorUtil.TEXT_COLOR, textAlign = TextAlign.CENTER_TOP)
            }
        }

        // Render the tab indicating the drop-down
        renderer.rect(0f, 13f, width, 2f, ColorUtil.TAB_BACKGROUND_COLOR)
        renderer.rect(width*0.4f, 12f, width*0.2f, 3f, ColorUtil.tabColor)

        // Render the dropdown
        if (extended) {
            var ay = DEFAULT_HEIGHT
            val increment = renderer.defaultFontHeight + 2f
            for (option in setting.options) {
                renderer.rect(0f, ay, width, increment, ColorUtil.DROPDOWN_COLOR)
                val optionName = option.displayName
                val elementtitle =
                    optionName.substring(0, 1).uppercase(Locale.getDefault()) + optionName.substring(1, optionName.length)
                renderer.text(elementtitle, width/2f, ay + 2f, ColorUtil.TEXT_COLOR, textAlign = TextAlign.CENTER_TOP)

                /** Highlight the element if it is selected */
                if (setting.isSelected(option)) {
                    renderer.rect(0f, ay, 2f, increment, ColorUtil.clickGUIColor.rgb)
                }
                /** Highlight the element when it is hovered */
                if (mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute + ay && mouseY < yAbsolute + ay + increment) {
                    renderer.rect(width-1f, ay, 1f, increment, ColorUtil.clickGUIColor.rgb)
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
            val increment = renderer.defaultFontHeight + 2
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