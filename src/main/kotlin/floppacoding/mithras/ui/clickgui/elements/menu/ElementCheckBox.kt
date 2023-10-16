package floppacoding.mithras.ui.clickgui.elements.menu

import floppacoding.mithras.module.settings.impl.BooleanSetting
import floppacoding.mithras.ui.clickgui.elements.Element
import floppacoding.mithras.ui.clickgui.elements.ElementType
import floppacoding.mithras.ui.clickgui.elements.ModuleButton
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import floppacoding.mithras.ui.nanovg.NVGR

/**
 * Provides a checkbox element.
 *
 * @author  Aton
 */
class ElementCheckBox(parent: ModuleButton, setting: BooleanSetting) :
    Element<BooleanSetting>(parent, setting, ElementType.CHECK_BOX) {

    override fun renderElement(mouseX: Float, mouseY: Float, partialTicks: Float): Float {
        val buttonColor = if (setting.enabled)
            ColorUtil.clickGUIColor.rgb
        else ColorUtil.BUTTON_COLOR

        /** Rendering the name and the checkbox */
        NVGR.text(displayName, 1f, 3f, ColorUtil.TEXT_COLOR)
        NVGR.rect(width-13f, 2f,11f, 11f, buttonColor)
        if (isCheckHovered(mouseX, mouseY))
            NVGR.rect(width-13f, 2f,11f, 11f, ColorUtil.BOX_HOVER_COLOR)

        return super.renderElement(mouseX, mouseY, partialTicks)
    }

    /**
     * Handles mouse clicks for this element and returns true if an action was performed
	 */
    override fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int): Boolean {
        if (mouseButton == 0 && isCheckHovered(mouseX, mouseY)) {
            setting.toggle()
            return true
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    /**
	 * Checks whether this element is hovered
	 */
    private fun isCheckHovered(mouseX: Float, mouseY: Float): Boolean {
        return mouseX >= xAbsolute + width - 13 && mouseX <= xAbsolute + width - 1 && mouseY >= yAbsolute + 2 && mouseY <= yAbsolute + height - 2
    }
}