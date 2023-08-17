package floppacoding.mithras.ui.clickgui.advanced

import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.impl.render.MainSettings
import floppacoding.mithras.module.settings.impl.*
import floppacoding.mithras.ui.clickgui.ClickGUI
import floppacoding.mithras.ui.clickgui.advanced.elements.AdvancedElement
import floppacoding.mithras.ui.clickgui.advanced.elements.menu.*
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import floppacoding.mithras.ui.clickgui.util.ColorUtil.textcolor
import floppacoding.mithras.ui.clickgui.util.FontUtil
import floppacoding.mithras.utils.render.HUDRenderUtils
import net.minecraft.client.gui.DrawContext
import net.minecraft.util.math.MathHelper
import java.awt.Color

/**
 * Provides an advanced menu screen for click gui modules.
 *
 * @author Aton
 */
class AdvancedMenu(val module: Module) {
    private val elements: MutableList<AdvancedElement<*>> = mutableListOf()

    // Position parameters, for simplicity all the logic is handled in the getters and setters, so that the values dont have to be updated once every render
    private val s
        get() = mc.window
    var x = 10
        set(value) {
            MainSettings.advancedRelX.value = value / s.width.toDouble() * ClickGUI.CLICK_GUI_SCALE
            field = (s.width * MainSettings.advancedRelX.value / ClickGUI.CLICK_GUI_SCALE).toInt()
        }
    var y = 10
        set(value) {
            MainSettings.advancedRelY.value = value / s.height.toDouble() * ClickGUI.CLICK_GUI_SCALE
            field = (s.height * MainSettings.advancedRelY.value / ClickGUI.CLICK_GUI_SCALE).toInt()
        }
    private var width = 10
    private var height = 10

    // For repositioning the screen.
    private var dragging = false
    private var x2 = 0
    private var y2 = 0

    // For scrolling
    private var length = 0
    private val scrollAmmount = 15
    private var scrollOffs = 0
        set(value) {
            field = MathHelper.clamp(value,0, length)
        }

    private val indent = 5

    init {
        for (setting in module.settings) {
            if (!setting.visibility.visibleInAdvanced) continue
            when (setting) {
                is BooleanSetting   -> elements.add(AdvancedElementCheckBox (this, module, setting))
                is NumberSetting    -> elements.add(AdvancedElementSlider   (this, module, setting))
                is SelectorSetting  -> elements.add(AdvancedElementSelector (this, module, setting))
                is StringSetting    -> elements.add(AdvancedElementTextField(this, module, setting))
                is ColorSetting     -> elements.add(AdvancedElementColor    (this, module, setting))
                is ActionSetting    -> elements.add(AdvancedElementAction   (this, module, setting))
            }
        }
        elements.add(AdvancedElementKeyBind(this, module))
    }

    /**
     * Returns true if any of the elements is listening for key inputs.
     * In that case the esc key should not exit the menu.
     */
    fun isListening() : Boolean{
        return elements.any { it.listening }
    }

    /**
     * Render the menu
     */
    fun drawScreen(context: DrawContext, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (dragging) {
            x = x2 + mouseX
            y = y2 + mouseY
        }
        updatePosition()

        val temp = ColorUtil.clickGUIColor
        val color = Color(temp.red, temp.green, temp.blue, 200).rgb

        // Set up Transform
        context.matrices.push()
        context.matrices.translate(x.toFloat(), y.toFloat(), 0f)

        /** Rendering the background box */
        context.fill(0, 0,  width, height, ColorUtil.elementColor)

        // Render a title bar containing the name of the module
        context.fill(0, 0, width, 15, color)
        FontUtil.drawTotalCenteredStringWithShadow(context, module.name, width / 2.0,  1 + 15 / 2.0, textcolor)

        // Set up the Scissor Box
//        val scale = mc.window.height /  mc.window.scaledHeight
        HUDRenderUtils.setUpScissor(
            (mc.window.width * MainSettings.advancedRelX.value).toInt(),
            (mc.window.height * MainSettings.advancedRelY.value + 15 * ClickGUI.CLICK_GUI_SCALE).toInt(),
            (mc.window.width * MainSettings.ADVANCED_GUI_RELATIVE_WIDTH).toInt(),
            (mc.window.height * MainSettings.ADVANCED_GUI_RELATIVE_HEIGHT - (15+indent) * ClickGUI.CLICK_GUI_SCALE).toInt(),
            1.0
        )
//        GL11.glScissor(
//            (mc.window.width * MainSettings.advancedRelX.value).toInt(),
//            (mc.window.height * (1- MainSettings.advancedRelHeight -MainSettings.advancedRelY.value) + indent * scale).toInt(),
//            (mc.window.width * MainSettings.advancedRelWidth).toInt(),
//            (mc.window.height * MainSettings.advancedRelHeight - 15 * scale - indent * scale).toInt()
//        )
//        GL11.glEnable(GL11.GL_SCISSOR_TEST)

        /**
         * Current render position.
         */
        var dy = 20 - scrollOffs

        /** Render the module description text */
        FontUtil.drawSplitString(context, module.description, indent, dy, width - 2 * indent , textcolor)
        dy += FontUtil.getSplitHeight(module.description, width-2*indent) + 10
        //Render the settings.
        for (element in elements) {
            element.setPosition(indent, dy)
            element.width = this.width - 2* indent
            element.drawScreen(context, mouseX, mouseY, partialTicks)
            dy += element.height
        }
        length = dy + scrollOffs

        // Resetting the scissor
        HUDRenderUtils.endScissor()
        context.matrices.pop()
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0 && isMouseOnTopBar(mouseX, mouseY)) {
            x2 = x - mouseX
            y2 = y - mouseY
            dragging = true
            return true
        }
        if (isMouseInBox(mouseX, mouseY) || elements.any { element -> element is AdvancedElementKeyBind && element.listening} ) {
            for (element in elements.reversed()) {
                if(element.mouseClicked(mouseX, mouseY, mouseButton)) return true
            }
        }
        return isMouseInBox(mouseX, mouseY)
    }

    fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (state == 0) {
            dragging = false
        }
        for (element in elements.reversed()) {
            element.mouseReleased(mouseX, mouseY, state)
        }
    }

    fun keyTyped(keyCode: Int, scanCode: Int): Boolean {
        for (element in elements.reversed()) {
            if(element.keyTyped(keyCode, scanCode)) return true
        }
        return false
    }

    /**
     * Scrolls the settings by the given number of lines.
     *
     * @param amount The amount to scroll
     */
    fun scroll(amount: Int, mouseX: Int, mouseY: Int): Boolean {
        if (!isMouseInBox(mouseX, mouseY)) return false

        val diff = -amount * scrollAmmount
        scrollOffs = (scrollOffs + diff).coerceAtMost(length - height + 20).coerceAtLeast(0)
        return true

    }

    /**
     * Updates the position of the element.
     */
    private fun updatePosition() {
        val s = mc.window
        x = (s.width  * MainSettings.advancedRelX.value   / ClickGUI.CLICK_GUI_SCALE).toInt()
        y = (s.height * MainSettings.advancedRelY.value   / ClickGUI.CLICK_GUI_SCALE).toInt()
        width =  (s.width *MainSettings.ADVANCED_GUI_RELATIVE_WIDTH  / ClickGUI.CLICK_GUI_SCALE).toInt()
        height = (s.height*MainSettings.ADVANCED_GUI_RELATIVE_HEIGHT / ClickGUI.CLICK_GUI_SCALE).toInt()
    }

    /**
     * Detects whether the mouse is within this gui.
     * @return true when the mouse is over the box of the Gui.
     */
    private fun isMouseInBox(mouseX: Int, mouseY: Int): Boolean{
        return mouseX >= x && mouseX < x + width && mouseY  >= y && mouseY < y + height
    }

    /**
     * Detects whether the mouse is on the top Bar of the Gui.
     */
    private fun isMouseOnTopBar(mouseX: Int, mouseY: Int): Boolean{
        return mouseX >= x && mouseX < x + width && mouseY  >= y && mouseY < y + 15
    }
}