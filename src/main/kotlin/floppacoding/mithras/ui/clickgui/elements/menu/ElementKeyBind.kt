package floppacoding.mithras.ui.clickgui.elements.menu

import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.impl.DummySetting
import floppacoding.mithras.ui.clickgui.elements.Element
import floppacoding.mithras.ui.clickgui.elements.ElementType
import floppacoding.mithras.ui.clickgui.elements.ModuleButton
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import floppacoding.mithras.utils.render.TextAlign
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


    override fun renderElement(mouseX: Float, mouseY: Float, partialTicks: Float): Float {
        val keyName = mod.keyBind.localizedText.string
        val displayValue = "[$keyName]"

        renderer.text(displayName, 1f, 2f, ColorUtil.TEXT_COLOR)
        renderer.text(displayValue, width-1f, 2f, ColorUtil.TEXT_COLOR, textAlign = TextAlign.RIGHT_TOP)

        return super.renderElement(mouseX, mouseY, partialTicks)
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
            mod.keyBind = InputUtil.Type.MOUSE.createFromCode(mouseButton)
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
                mod.keyBind = InputUtil.UNKNOWN_KEY
                listening = false
            } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                listening = false
            } else if (!keyBlackList.contains(keyCode)) {
                mod.keyBind = InputUtil.fromKeyCode(keyCode, scanCode)
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
        return mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute && mouseY <= yAbsolute + height
    }
}