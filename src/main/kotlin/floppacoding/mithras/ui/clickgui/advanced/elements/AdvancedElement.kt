package floppacoding.mithras.ui.clickgui.advanced.elements

import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.Setting
import floppacoding.mithras.ui.clickgui.ClickGUI
import floppacoding.mithras.ui.clickgui.advanced.AdvancedMenu
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import floppacoding.mithras.ui.nanovg.NVGR
import java.awt.Color

/**
 * Class for all setting elements in the advanced menu to inherit from
 *
 * @author Aton
 */
abstract class AdvancedElement<S: Setting<*>>(
    val parent: AdvancedMenu,
    val module: Module,
    val setting: S,
    val type: AdvancedElementType,
) {
    val clickgui: ClickGUI = parent.clickGui
    var x = 0f
    var y = 0f
    /** Width of the entire element consisting of the setting and description. */
    var width = 150f
    /** Height of the entire element consisting of the setting and description. Essentially the height of the higher one of the two */
    var height = 15f
    /** Width of the Setting without the description text. */
    var settingWidth = 116f
    /** Height of the Setting without the description text. */
    var settingHeight = 15f

    var comboextended = false

    var listening = false

    fun drawScreen(mouseX: Float, mouseY: Float, partialTicks: Float) {
        NVGR.push()
        NVGR.translate(x, y)

        //Rendering the box behind the element.
        val temp = ColorUtil.clickGUIColor
        val color = if (listening) {
            Color(temp.red, temp.green, temp.blue, 200).rgb
        }else {
            Color(ColorUtil.elementColor, true).darker().rgb
        }
        NVGR.rect(0f, 0f, width, height, Color(ColorUtil.bgColor, true).brighter().rgb)
        NVGR.rect(0f, 0f, settingWidth, settingHeight, color)

        // Render the element.
        val l1 = renderElement(mouseX, mouseY, partialTicks)

        // Render the descriton right of the Setting
        val l2 = renderDescription()
        this.settingHeight = l1
        this.height = l1.coerceAtLeast(l2)

        NVGR.pop()
    }

    open fun renderElement(mouseX: Float, mouseY: Float, partialTicks: Float) : Float{ return settingHeight }

    open fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int): Boolean {
        return false
    }

    open fun mouseReleased(mouseX: Float, mouseY: Float, button: Int) {}

    /**
     * Overridden in the elements to enable key detection. Returns true when an action was taken.
     */
    open fun keyPressed(keyCode: Int, scanCode: Int): Boolean { return false }

    /**
     * Overridden in the elements to enable key detection. Returns true when an action was taken.
     */
    open fun charTyped(chr: Char, modifiers: Int): Boolean { return false }

    private fun renderDescription() : Float{
        var descriptionHeight = 0f
        setting.description?.let {
            NVGR.text(it, settingWidth + 10f, 2f, ColorUtil.TEXT_COLOR, splitWidth = width- settingWidth -10f)
            descriptionHeight = NVGR.textBounds(it, width - settingWidth - 10f).height()
        }
        return descriptionHeight + 4f
    }

    fun setDimensions(x: Float, y: Float, width: Float, height: Float){
        this.x = x
        this.y = y
        this.width = width
        this.height = height
    }

    fun setPosition(x: Float, y: Float){
        this.x = x
        this.y = y
    }
}