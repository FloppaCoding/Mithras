package floppacoding.mithras.ui.clickgui.advanced.elements.menu

import floppacoding.aurora.core.TextAlign
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.impl.StringSetting
import floppacoding.mithras.ui.clickgui.advanced.AdvancedMenu
import floppacoding.mithras.ui.clickgui.advanced.elements.AdvancedElement
import floppacoding.mithras.ui.clickgui.advanced.elements.AdvancedElementType
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import org.lwjgl.glfw.GLFW

/**
 * Provides a text field element for the advanced gui.
 *
 * @author Aton
 */
class AdvancedElementTextField(
    parent: AdvancedMenu, module: Module, setting: StringSetting,
) : AdvancedElement<StringSetting>(parent, module, setting, AdvancedElementType.TEXT_FIELD) {

    /**
     * Rendering the element
     */
    override fun renderElement(mouseX: Float, mouseY: Float, partialTicks: Float) : Float{
        val displayValue = setting.text
        val totalWidth = renderer.textWidth(displayValue + "00" + setting.name)


        /** Rendering the text */
        if (totalWidth <= settingWidth) {
            renderer.text(setting.name, 1f, 2f, ColorUtil.TEXT_COLOR)
            renderer.text(displayValue, settingWidth-1f, 2f, ColorUtil.TEXT_COLOR, textAlign = TextAlign.RIGHT_TOP)
        }else {
            if (isTextHovered(mouseX, mouseY) || listening) {
                renderer.text(displayValue, settingWidth / 2f, 2f, ColorUtil.TEXT_COLOR, textAlign = TextAlign.CENTER_TOP)
            } else {
                renderer.text(setting.name, settingWidth/2f, 2f, ColorUtil.TEXT_COLOR, textAlign = TextAlign.CENTER_TOP)
            }
        }

        return this.settingHeight
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
        return mouseX >= parent.x + x && mouseX <= parent.x + x + settingWidth && mouseY >= parent.y + y  && mouseY <= parent.y + y + settingHeight
    }

}