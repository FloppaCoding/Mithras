package floppacoding.mithras.ui.clickgui.elements.menu

import floppacoding.aurora.core.TextAlign
import floppacoding.mithras.module.settings.impl.StringSetting
import floppacoding.mithras.ui.clickgui.elements.Element
import floppacoding.mithras.ui.clickgui.elements.ElementType
import floppacoding.mithras.ui.clickgui.elements.ModuleButton
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import org.lwjgl.glfw.GLFW

/**
 * Provides a text field element.
 *
 * @author Aton
 */
class ElementTextField(parent: ModuleButton, setting: StringSetting) :
    Element<StringSetting>(parent, setting, ElementType.TEXT_FIELD) {

    override fun renderElement(mouseX: Float, mouseY: Float, partialTicks: Float): Float {
        val displayValue = setting.text
        val totalWidth = renderer.textWidth(displayValue + "00" + displayName)


        /** Rendering the text */
        if (totalWidth <= width) {
            renderer.text(displayName, 1f, 2f, ColorUtil.TEXT_COLOR)
            renderer.text(displayValue, width-1f, 2f, ColorUtil.TEXT_COLOR, textAlign = TextAlign.RIGHT_TOP)
        }else {
            if (isTextHovered(mouseX, mouseY) || listening) {
                renderer.text(displayValue, width / 2f, 2f, ColorUtil.TEXT_COLOR, textAlign = TextAlign.CENTER_TOP)
            } else {
                renderer.text(displayName, width/2f, 2f, ColorUtil.TEXT_COLOR, textAlign = TextAlign.CENTER_TOP)
            }
        }

        return super.renderElement(mouseX, mouseY, partialTicks)
    }

    /**
     * Handles interaction with this element.
     * Returns true if interacted with the element to cancel further interactions.
     */
    override fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int): Boolean {
        if (mouseButton == 0 && isTextHovered(mouseX, mouseY)) {
            listening = true
            return true
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    /**
     * Register key strokes.
     */
    override fun keyPressed(keyCode: Int, scanCode: Int): Boolean {
        if (listening) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_ENTER) {
                listening = false
                return true
            } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                setting.text = setting.text.dropLast(1)
                return true
            }
        }
        return super.keyPressed(keyCode, scanCode)
    }

    override fun charTyped(chr: Char, modifiers: Int): Boolean {
        if (listening) {
            if (isValidChar(chr)) {
                setting.text += chr.toString()
                return true
            }
        }
        return super.charTyped(chr, modifiers)
    }

    private fun isValidChar(chr: Char): Boolean {
        return chr.code != 167 && chr >= ' ' && chr.code != 127
    }

    /**
     * Checks whether the mouse is hovering the text field
     */
    private fun isTextHovered(mouseX: Float, mouseY: Float): Boolean {
        return mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute  && mouseY <= yAbsolute + height
    }
}