package floppacoding.mithras.ui.clickgui.advanced.elements.menu

import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.impl.DummySetting
import floppacoding.mithras.ui.clickgui.advanced.AdvancedMenu
import floppacoding.mithras.ui.clickgui.advanced.elements.AdvancedElement
import floppacoding.mithras.ui.clickgui.advanced.elements.AdvancedElementType
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import floppacoding.aurora.core.TextAlign
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
    override fun renderElement(mouseX: Float, mouseY: Float, partialTicks: Float): Float {
        val displayName = "Key Bind"
        val keyName = module.keyBind.localizedText.string

        val displayValue = "[$keyName]"

        // Rendering the text and the keybind.
        renderer.text(displayName, 1f, 2f, ColorUtil.TEXT_COLOR)
        renderer.text(displayValue, settingWidth-1f, 2f, ColorUtil.TEXT_COLOR, textAlign = TextAlign.RIGHT_TOP)
        return this.settingHeight
    }

    /**
     * Handles mouse clicks for this element and returns true if an action was performed.
     * Used to interact with the element and to register mouse binds.
     */
    override fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int): Boolean {
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
    override fun keyPressed(keyCode: Int, scanCode: Int): Boolean {
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
        return super.keyPressed(keyCode, scanCode)
    }

    /**
     * Checks whether this element is hovered
     */
    private fun isCheckHovered(mouseX: Float, mouseY: Float): Boolean {
        return mouseX >= parent.x + x && mouseX <= parent.x + x + settingWidth && mouseY >= parent.y + y  && mouseY <= parent.y + y + settingHeight
    }
}