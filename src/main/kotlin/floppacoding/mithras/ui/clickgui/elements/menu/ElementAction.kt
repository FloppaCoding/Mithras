package floppacoding.mithras.ui.clickgui.elements.menu

import floppacoding.mithras.module.settings.impl.ActionSetting
import floppacoding.mithras.ui.clickgui.elements.Element
import floppacoding.mithras.ui.clickgui.elements.ElementType
import floppacoding.mithras.ui.clickgui.elements.ModuleButton
import floppacoding.mithras.ui.clickgui.util.ColorUtil

/**
 * Provides the Menu Button for action settings.
 *
 * @author Aton
 */
class ElementAction(parent: ModuleButton, setting: ActionSetting) :
    Element<ActionSetting>(parent, setting, ElementType.ACTION)  {

    override fun renderElement(mouseX: Float, mouseY: Float, partialTicks: Float): Float {
        renderer.text(displayName, 1f, 2f, ColorUtil.TEXT_COLOR)
        return super.renderElement(mouseX, mouseY, partialTicks)
    }

    /**
     * Handles mouse clicks for this element and returns true if an action was performed.
     * Used to activate the elements action.
     */
    override fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int): Boolean {
        if (mouseButton == 0 && isButtonHovered(mouseX, mouseY) ) {
            (setting as? ActionSetting)?.doAction()
            return true
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    /**
     * Checks whether this element is hovered
     */
    private fun isButtonHovered(mouseX: Float, mouseY: Float): Boolean {
        return mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute  && mouseY <= yAbsolute + height
    }
}