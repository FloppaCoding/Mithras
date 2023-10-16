package floppacoding.mithras.ui.clickgui.advanced.elements.menu

import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.impl.ActionSetting
import floppacoding.mithras.ui.clickgui.advanced.AdvancedMenu
import floppacoding.mithras.ui.clickgui.advanced.elements.AdvancedElement
import floppacoding.mithras.ui.clickgui.advanced.elements.AdvancedElementType
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import floppacoding.mithras.ui.nanovg.NVGR

/**
 * Provides the Button for action settings in the advanced gui.
 *
 * @author Aton
 */
class AdvancedElementAction(parent: AdvancedMenu, module: Module, setting: ActionSetting) :
    AdvancedElement<ActionSetting>(parent, module, setting, AdvancedElementType.ACTION) {

    /**
     * Render the element
     */
    override fun renderElement(mouseX: Float, mouseY: Float, partialTicks: Float) : Float{
        NVGR.text(setting.name, 1f, 2f, ColorUtil.TEXT_COLOR)
        return this.settingHeight
    }

    /**
     * Handles mouse clicks for this element and returns true if an action was performed.
     * Used to activate the elements action.
     */
    override fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int): Boolean {
        if (mouseButton == 0 && isButtonHovered(mouseX, mouseY)) {
            setting.doAction()
            return true
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    /**
     * Checks whether this element is hovered
     */
    private fun isButtonHovered(mouseX: Float, mouseY: Float): Boolean {
        return mouseX >= parent.x + x && mouseX <= parent.x + x + settingWidth && mouseY >= parent.y + y  && mouseY <= parent.y + y + settingHeight
    }
}