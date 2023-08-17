package floppacoding.mithras.ui.clickgui.advanced.elements

import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.Setting
import floppacoding.mithras.ui.clickgui.advanced.AdvancedMenu
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import floppacoding.mithras.ui.clickgui.util.FontUtil
import net.minecraft.client.gui.DrawContext
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
    var x = 0
    var y = 0
    /** Width of the entire element consisting of the setting and description. */
    var width = 150
    /** Height of the entire element consisting of the setting and description. Essentially the height of the higher one of the two */
    var height = 15
    /** Width of the Setting without the description text. */
    var settingWidth = 116
    /** Height of the Setting without the description text. */
    var settingHeight = 15

    var comboextended = false

    var listening = false

    fun drawScreen(context: DrawContext, mouseX: Int, mouseY: Int, partialTicks: Float) {
        context.matrices.push()
        context.matrices.translate(x.toFloat(), y.toFloat(), 0f)

        //Rendering the box behind the element.
        val temp = ColorUtil.clickGUIColor
        val color = if (listening) {
            Color(temp.red, temp.green, temp.blue, 200).rgb
        }else {
            Color(ColorUtil.elementColor, true).darker().rgb
        }
        context.fill(0, 0, width, height, Color(ColorUtil.bgColor, true).brighter().rgb)
        context.fill(0, 0, settingWidth, settingHeight, color)

        // Render the element.
        val l1 = renderElement(context, mouseX, mouseY, partialTicks)

        // Render the descriton right of the Setting
        val l2 = renderDescription(context)
        this.settingHeight = l1
        this.height = l1.coerceAtLeast(l2)

        context.matrices.pop()
    }

    open fun renderElement(context: DrawContext, mouseX: Int, mouseY: Int, partialTicks: Float) : Int{ return settingHeight }

    open fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        return false
    }

    open fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {}

    /**
     * Overridden in the elements to enable key detection. Returns true when an action was taken.
     */
    open fun keyTyped(keyCode: Int, scanCode: Int): Boolean { return false }

    fun renderDescription(context: DrawContext) : Int{
        var descriptionHeight = 0
        setting.description?.let {
            FontUtil.drawSplitString(
                context,
                it, settingWidth + 10,
                2, width - settingWidth - 10, ColorUtil.TEXT_COLOR
            )
            descriptionHeight = FontUtil.getSplitHeight(it, width - settingWidth - 10)
        }
        return descriptionHeight + 4
    }

    fun setDimensions(x: Int, y: Int, width: Int, height: Int){
        this.x = x
        this.y = y
        this.width = width
        this.height = height
    }

    fun setPosition(x: Int, y: Int){
        this.x = x
        this.y = y
    }
}