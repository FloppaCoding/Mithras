package floppacoding.mithras.ui.clickgui.elements.menu

import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.impl.DummySetting
import floppacoding.mithras.ui.clickgui.elements.Element
import floppacoding.mithras.ui.clickgui.elements.ElementType
import floppacoding.mithras.ui.clickgui.elements.ModuleButton
import floppacoding.mithras.ui.clickgui.util.FontUtil
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

/**
 * Provides a key bind element.
 *
 * @author Aton
 */
class ElementKeyBind(parent: ModuleButton, val mod: Module) :
    Element<DummySetting>(parent, DummySetting("Key Bind"), ElementType.KEY_BIND) {

    private val keyBlackList = intArrayOf()


    override fun renderElement(context: DrawContext, mouseX: Int, mouseY: Int, partialTicks: Float): Int {
        val keyName = if (mod.keyCode > 0)
            GLFW.glfwGetKeyName(mod.keyCode, GLFW.glfwGetKeyScancode(mod.keyCode)) ?: "Err"
        else if (mod.keyCode < -10)
            InputUtil.Type.MOUSE.createFromCode(mod.keyCode + 100).localizedText.string
//            Mouse.getButtonName(mod.keyCode + 100)
        else
            ".."
        val displayValue = "[$keyName]"

        FontUtil.drawString(context, displayName, 1, 2)
        FontUtil.drawString(context, displayValue, width - FontUtil.getStringWidth(displayValue), 2)

        return super.renderElement(context, mouseX, mouseY, partialTicks)
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
            mod.keyCode = -100 + mouseButton
            listening = false
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    /**
     * Register key strokes. Used to set the key bind.
     */
    override fun keyTyped(typedChar: String, keyCode: Int): Boolean {
        if (listening) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                mod.keyCode = GLFW.GLFW_KEY_UNKNOWN
                listening = false
            } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                listening = false
            } else if (!keyBlackList.contains(keyCode)) {
                mod.keyCode = keyCode
                listening = false
            }
            return true
        }
        return super.keyTyped(typedChar, keyCode)
    }

    /**
     * Checks whether this element is hovered
     */
    private fun isCheckHovered(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute && mouseY <= yAbsolute + height
    }
}