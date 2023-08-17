package floppacoding.mithras.ui.clickgui.advanced.elements.menu

import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.impl.DummySetting
import floppacoding.mithras.ui.clickgui.advanced.AdvancedMenu
import floppacoding.mithras.ui.clickgui.advanced.elements.AdvancedElement
import floppacoding.mithras.ui.clickgui.advanced.elements.AdvancedElementType
import floppacoding.mithras.ui.clickgui.util.FontUtil
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

/**
 * Provides a key bind element.
 *
 * @author Aton
 */
class AdvancedElementKeyBind(parent: AdvancedMenu, module: Module) :
    AdvancedElement<DummySetting>(parent, module, DummySetting("KeyBind"), AdvancedElementType.KEY_BIND) {

    private val keyBlackList = intArrayOf()

    /**
     * Render the element
     */
    override fun renderElement(context: DrawContext, mouseX: Int, mouseY: Int, partialTicks: Float): Int {
        val displayName = "Key Bind"
        val keyName = module.keyBind.localizedText.string

        val displayValue = "[$keyName]"

        // Rendering the text and the keybind.
        FontUtil.drawString(context, displayName, 1, 2, -0x1)
        FontUtil.drawString(context, displayValue, this.settingWidth - FontUtil.getStringWidth(displayValue), 2, -0x1)
        return this.settingHeight
    }

    /**
     * Handles mouse clicks for this element and returns true if an action was performed.
     * Used to interact with the element and to register mouse binds.
     */
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0 && isCheckHovered(mouseX, mouseY)) {
            listening = !listening
            return true
        } else if (listening) {
            module.keyBind = InputUtil.Type.MOUSE.createFromCode(mouseButton)
            listening = false
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    /**
     * Register keystrokes. Used to set the key bind.
     */
    override fun keyTyped(keyCode: Int, scanCode: Int): Boolean {
        if (listening) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                module.keyBind = InputUtil.UNKNOWN_KEY
                listening = false
            } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                listening = false
            } else if (!keyBlackList.contains(keyCode)) {
                module.keyBind = InputUtil.fromKeyCode(keyCode, scanCode)
                listening = false
            }
            return true
        }
        return super.keyTyped(keyCode, scanCode)
    }

    /**
     * Checks whether this element is hovered
     */
    private fun isCheckHovered(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= parent.x + x && mouseX <= parent.x + x + settingWidth && mouseY >= parent.y + y  && mouseY <= parent.y + y + settingHeight
    }
}