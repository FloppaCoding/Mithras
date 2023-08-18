package floppacoding.mithras.ui.clickguinano.elements.menu

import floppacoding.mithras.module.settings.impl.StringSetting
import floppacoding.mithras.ui.clickguinano.elements.Element
import floppacoding.mithras.ui.clickguinano.elements.ElementType
import floppacoding.mithras.ui.clickguinano.elements.ModuleButton
import floppacoding.mithras.ui.clickguinano.util.ColorUtil
import floppacoding.mithras.ui.nanovg.NVGR
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
        val totalWidth = NVGR.textWidth(displayValue + "00" + displayName)


        /** Rendering the text */
        if (totalWidth <= width) {
            NVGR.text(displayName, 1f, 2f, ColorUtil.TEXT_COLOR)
            NVGR.text(displayValue, width-1f, 2f, ColorUtil.TEXT_COLOR, textAlign = NVGR.TextAlign.TOP_RIGHT)
        }else {
            if (isTextHovered(mouseX, mouseY) || listening) {
                NVGR.text(displayValue, width / 2f, 2f, ColorUtil.TEXT_COLOR, textAlign = NVGR.TextAlign.CENTER_TOP)
            } else {
                NVGR.text(displayName, width/2f, 2f, ColorUtil.TEXT_COLOR, textAlign = NVGR.TextAlign.CENTER_TOP)
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
    override fun keyTyped(keyCode: Int, scanCode: Int): Boolean {
        if (listening) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_ENTER) {
                listening = false
            } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                setting.text = setting.text.dropLast(1)
            }else if (!keyBlackList.contains(keyCode)) {
                val typedChar = GLFW.glfwGetKeyName(keyCode, scanCode) ?: ""
                setting.text += typedChar
            }
            return true
        }
        return super.keyTyped(keyCode, scanCode)
    }

    /**
     * Checks whether the mouse is hovering the text field
     */
    private fun isTextHovered(mouseX: Float, mouseY: Float): Boolean {
        return mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute  && mouseY <= yAbsolute + height
    }

    private val keyBlackList = intArrayOf(
        GLFW.GLFW_KEY_ESCAPE,
        GLFW.GLFW_KEY_ENTER,
        GLFW.GLFW_KEY_TAB,
        GLFW.GLFW_KEY_BACKSPACE,
        GLFW.GLFW_KEY_INSERT,
        GLFW.GLFW_KEY_DELETE,
        GLFW.GLFW_KEY_RIGHT,
        GLFW.GLFW_KEY_LEFT,
        GLFW.GLFW_KEY_DOWN,
        GLFW.GLFW_KEY_UP,
        GLFW.GLFW_KEY_PAGE_UP,
        GLFW.GLFW_KEY_PAGE_DOWN,
        GLFW.GLFW_KEY_HOME,
        GLFW.GLFW_KEY_END,
        GLFW.GLFW_KEY_CAPS_LOCK,
        GLFW.GLFW_KEY_SCROLL_LOCK,
        GLFW.GLFW_KEY_NUM_LOCK,
        GLFW.GLFW_KEY_PRINT_SCREEN,
        GLFW.GLFW_KEY_PAUSE,
        GLFW.GLFW_KEY_F1,
        GLFW.GLFW_KEY_F2,
        GLFW.GLFW_KEY_F3,
        GLFW.GLFW_KEY_F4,
        GLFW.GLFW_KEY_F5,
        GLFW.GLFW_KEY_F6,
        GLFW.GLFW_KEY_F7,
        GLFW.GLFW_KEY_F8,
        GLFW.GLFW_KEY_F9,
        GLFW.GLFW_KEY_F10,
        GLFW.GLFW_KEY_F11,
        GLFW.GLFW_KEY_F12,
        GLFW.GLFW_KEY_F13,
        GLFW.GLFW_KEY_F14,
        GLFW.GLFW_KEY_F15,
        GLFW.GLFW_KEY_F16,
        GLFW.GLFW_KEY_F17,
        GLFW.GLFW_KEY_F18,
        GLFW.GLFW_KEY_F19,
        GLFW.GLFW_KEY_F20,
        GLFW.GLFW_KEY_F21,
        GLFW.GLFW_KEY_F22,
        GLFW.GLFW_KEY_F23,
        GLFW.GLFW_KEY_F24,
        GLFW.GLFW_KEY_F25,
        GLFW.GLFW_KEY_KP_0,
        GLFW.GLFW_KEY_KP_1,
        GLFW.GLFW_KEY_KP_2,
        GLFW.GLFW_KEY_KP_3,
        GLFW.GLFW_KEY_KP_4,
        GLFW.GLFW_KEY_KP_5,
        GLFW.GLFW_KEY_KP_6,
        GLFW.GLFW_KEY_KP_7,
        GLFW.GLFW_KEY_KP_8,
        GLFW.GLFW_KEY_KP_9,
        GLFW.GLFW_KEY_KP_DECIMAL,
        GLFW.GLFW_KEY_KP_DIVIDE,
        GLFW.GLFW_KEY_KP_MULTIPLY,
        GLFW.GLFW_KEY_KP_SUBTRACT,
        GLFW.GLFW_KEY_KP_ADD,
        GLFW.GLFW_KEY_KP_ENTER,
        GLFW.GLFW_KEY_KP_EQUAL,
        GLFW.GLFW_KEY_LEFT_SHIFT,
        GLFW.GLFW_KEY_LEFT_CONTROL,
        GLFW.GLFW_KEY_LEFT_ALT,
        GLFW.GLFW_KEY_LEFT_SUPER,
        GLFW.GLFW_KEY_RIGHT_SHIFT,
        GLFW.GLFW_KEY_RIGHT_CONTROL,
        GLFW.GLFW_KEY_RIGHT_ALT,
        GLFW.GLFW_KEY_RIGHT_SUPER,
        GLFW.GLFW_KEY_MENU,
        GLFW.GLFW_KEY_LAST
    )
}