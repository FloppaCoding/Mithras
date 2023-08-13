package floppacoding.mithras.ui.clickgui.elements.menu

import floppacoding.mithras.module.settings.impl.BooleanSetting
import floppacoding.mithras.ui.clickgui.elements.Element
import floppacoding.mithras.ui.clickgui.elements.ElementType
import floppacoding.mithras.ui.clickgui.elements.ModuleButton
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import floppacoding.mithras.ui.clickgui.util.FontUtil
import net.minecraft.client.gui.DrawContext

/**
 * Provides a checkbox element.
 *
 * @author  Aton
 */
class ElementCheckBox(parent: ModuleButton, setting: BooleanSetting) :
    Element<BooleanSetting>(parent, setting, ElementType.CHECK_BOX) {

    override fun renderElement(context: DrawContext, mouseX: Int, mouseY: Int, partialTicks: Float): Int {
        val buttonColor = if (setting.enabled)
            ColorUtil.clickGUIColor.rgb
        else ColorUtil.buttonColor

        /** Rendering the name and the checkbox */
        FontUtil.drawString(context, displayName, 1, 3)
        context.fill(width -13, 2, width - 1, 13, buttonColor)
        if (isCheckHovered(mouseX, mouseY))
            context.fill(width -13, 2, width - 1, 13, ColorUtil.boxHoverColor)

        return super.renderElement(context, mouseX, mouseY, partialTicks)
    }

    /**
     * Handles mouse clicks for this element and returns true if an action was performed
	 */
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0 && isCheckHovered(mouseX, mouseY)) {
            setting.toggle()
            return true
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    /**
	 * Checks whether this element is hovered
	 */
    private fun isCheckHovered(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= xAbsolute + width - 13 && mouseX <= xAbsolute + width - 1 && mouseY >= yAbsolute + 2 && mouseY <= yAbsolute + height - 2
    }
}