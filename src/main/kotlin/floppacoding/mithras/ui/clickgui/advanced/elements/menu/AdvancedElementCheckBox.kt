package floppacoding.mithras.ui.clickgui.advanced.elements.menu

import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.impl.BooleanSetting
import floppacoding.mithras.ui.clickgui.advanced.AdvancedMenu
import floppacoding.mithras.ui.clickgui.advanced.elements.AdvancedElement
import floppacoding.mithras.ui.clickgui.advanced.elements.AdvancedElementType
import floppacoding.mithras.ui.clickgui.util.ColorUtil

/**
 * Provides a checkbox element for the advanced gui.
 *
 * @author Aton
 */
class AdvancedElementCheckBox(
    parent: AdvancedMenu, module: Module, setting: BooleanSetting,
) : AdvancedElement<BooleanSetting>(parent, module, setting, AdvancedElementType.CHECK_BOX) {


    /**
     * Render the element
     */
    override fun renderElement(mouseX: Float, mouseY: Float, partialTicks: Float) : Float{
        val buttonColor = if (setting.enabled)
            ColorUtil.clickGUIColor.rgb
        else ColorUtil.BUTTON_COLOR

        /** Rendering the name and the checkbox */
        renderer.text(setting.name, 1f, 2f, ColorUtil.TEXT_COLOR)
        renderer.rect(settingWidth-13f, 2f ,11f, 11f, buttonColor)

        if (isCheckHovered(mouseX, mouseY)) renderer.rect(
            settingWidth - 13,  2f, 11f,
            11f, ColorUtil.BOX_HOVER_COLOR
        )
        return this.settingHeight
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
        return mouseX >= parent.x + x + settingWidth - 13 && mouseX <= parent.x + x + settingWidth - 1 && mouseY >= parent.y + y + 2 && mouseY <= parent.y + y + settingHeight - 2
    }
}