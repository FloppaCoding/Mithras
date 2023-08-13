package floppacoding.mithras.ui.clickgui.elements.menu

import floppacoding.mithras.module.settings.impl.Options
import floppacoding.mithras.module.settings.impl.SelectorSetting
import floppacoding.mithras.ui.clickgui.elements.Element
import floppacoding.mithras.ui.clickgui.elements.ElementType
import floppacoding.mithras.ui.clickgui.elements.ModuleButton
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import floppacoding.mithras.ui.clickgui.util.FontUtil
import net.minecraft.client.gui.DrawContext
import java.util.*

/**
 * Provides a selector element.
 *
 * @author Aton
 */
class ElementSelector<T>(parent: ModuleButton, setting: SelectorSetting<T>) :
    Element<SelectorSetting<T>>(parent, setting, ElementType.SELECTOR)
        where T : Options, T : Enum<T> {


    override fun renderElement(context: DrawContext, mouseX: Int, mouseY: Int, partialTicks: Float): Int {
        val displayValue = (setting as SelectorSetting<*>).selected

        // Render the text.
        if (FontUtil.getStringWidth(displayValue + "00" + displayName) <= width) {
            FontUtil.drawString(context, displayName, 1, 2)
            FontUtil.drawString(context, displayValue, width - FontUtil.getStringWidth(displayValue), 2)
        } else {
            if (isButtonHovered(mouseX, mouseY)) {
                FontUtil.drawCenteredStringWithShadow(context, displayValue, width / 2.0, 2.0)
            } else {
                FontUtil.drawCenteredString(context, displayName, width / 2.0, 2.0)
            }
        }

        // Render the tab indicating the drop-down
        context.fill(0, 13, width, 15, ColorUtil.tabColorBg)
        context.fill((width * 0.4).toInt(), 12, (width * 0.6).toInt(), 15, ColorUtil.tabColor)

        // Render the dropdown
        if (extended) {
            var ay = DEFAULT_HEIGHT
            val increment = FontUtil.fontHeight + 2
            for (option in setting.options) {
                context.fill(0, ay, width, ay + increment, ColorUtil.dropDownColor)
                val optionName = option.displayName
                val elementtitle =
                    optionName.substring(0, 1).uppercase(Locale.getDefault()) + optionName.substring(1, optionName.length)
                FontUtil.drawCenteredString(context, elementtitle, width / 2.0, ay + 2.0)

                /** Highlight the element if it is selected */
                if (setting.isSelected(option)) {
                    context.fill(0, ay, 2, ay + increment, ColorUtil.clickGUIColor.rgb)
                }
                /** Highlight the element when it is hovered */
                if (mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute + ay && mouseY < yAbsolute + ay + increment) {
                    context.fill(width - 1, ay, width, ay + increment, ColorUtil.clickGUIColor.rgb)
                }
                ay += increment
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
                setting.index += 1
                return true
            }

            if (!extended) return false
            var ay = DEFAULT_HEIGHT
            val increment = FontUtil.fontHeight + 2
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
    private fun isButtonHovered(mouseX: Int, mouseY: Int): Boolean {
        return (mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute && mouseY <= yAbsolute + DEFAULT_HEIGHT)
    }
}