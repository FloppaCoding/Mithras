package floppacoding.mithras.ui.clickgui.elements.menu

import floppacoding.mithras.module.settings.impl.ActionSetting
import floppacoding.mithras.ui.clickgui.elements.Element
import floppacoding.mithras.ui.clickgui.elements.ElementType
import floppacoding.mithras.ui.clickgui.elements.ModuleButton
import floppacoding.mithras.ui.clickgui.util.FontUtil
import net.minecraft.client.gui.DrawContext

/**
 * Provides the Menu Button for action settings.
 *
 * @author Aton
 */
class ElementAction(parent: ModuleButton, setting: ActionSetting) :
    Element<ActionSetting>(parent, setting, ElementType.ACTION)  {

    override fun renderElement(context: DrawContext, mouseX: Int, mouseY: Int, partialTicks: Float): Int {
        FontUtil.drawString(context, displayName, 1, 2)
        return super.renderElement(context, mouseX, mouseY, partialTicks)
    }

    /**
     * Handles mouse clicks for this element and returns true if an action was performed.
     * Used to activate the elements action.
     */
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0 && isButtonHovered(mouseX, mouseY) ) {
            (setting as? ActionSetting)?.doAction()
            return true
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    /**
     * Checks whether this element is hovered
     */
    private fun isButtonHovered(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute  && mouseY <= yAbsolute + height
    }
}