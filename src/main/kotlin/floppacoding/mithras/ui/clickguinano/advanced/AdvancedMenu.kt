package floppacoding.mithras.ui.clickguinano.advanced

import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.impl.render.MainSettings
import floppacoding.mithras.module.settings.impl.*
import floppacoding.mithras.ui.clickguinano.ClickGUINano
import floppacoding.mithras.ui.clickguinano.advanced.elements.AdvancedElement
import floppacoding.mithras.ui.clickguinano.advanced.elements.menu.*
import floppacoding.mithras.ui.clickguinano.util.ColorUtil
import floppacoding.mithras.ui.clickguinano.util.ColorUtil.TEXT_COLOR
import floppacoding.mithras.ui.nanovg.NVGR
import net.minecraft.util.math.MathHelper
import java.awt.Color

/**
 * Provides an advanced menu screen for click gui modules.
 *
 * @author Aton
 */
class AdvancedMenu(val module: Module, val clickGui: ClickGUINano) {
    private val elements: MutableList<AdvancedElement<*>> = mutableListOf()
    var x = 10f
        set(value) {
            MainSettings.advancedRelX.value = value / mc.window.width.toDouble() * clickGui.scale
            field = (mc.window.width * MainSettings.advancedRelX.value).toFloat() / clickGui.scale
        }
    var y = 10f
        set(value) {
            MainSettings.advancedRelY.value = value / mc.window.height.toDouble() * clickGui.scale
            field = (mc.window.height * MainSettings.advancedRelY.value).toFloat() / clickGui.scale
        }
    private var width = 10f
    private var height = 10f

    // For repositioning the screen.
    private var dragging = false
    private var x2 = 0f
    private var y2 = 0f

    // For scrolling
    private var length = 0f
    private val scrollAmount = 15f
    private var scrollOffs = 0f
        set(value) {
            field = MathHelper.clamp(value,0f, length)
        }

    private val indent = 5f

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
    fun drawScreen(mouseX: Float, mouseY: Float, partialTicks: Float) {
        if (dragging) {
            x = x2 + mouseX
            y = y2 + mouseY
        }
        updatePosition()

        val temp = ColorUtil.clickGUIColor
        val color = Color(temp.red, temp.green, temp.blue, 200).rgb

        // Set up Transform
        NVGR.push()
        NVGR.translate(x, y)

        /** Rendering the background box */
        NVGR.rect(0f, 0f, width, height, ColorUtil.elementColor)

        // Render a title bar containing the name of the module
        NVGR.rect(0f, 0f, width, 15f, color)
        NVGR.text(module.name, width/2f, 1f+ 15f / 2f, TEXT_COLOR, textAlign = NVGR.TextAlign.CENTER_MIDDLE)

        // Set up the Scissor Box
        NVGR.scissor(0f, 15f, width, height-(15f+ indent))

        /**
         * Current render position.
         */
        var dy = 20 - scrollOffs

        /** Render the module description text */
        NVGR.text(module.description, indent, dy, TEXT_COLOR, splitWidth = width -2*indent)
        dy += NVGR.textBounds(module.description, width-2*indent).height() + 10f
        // Render the settings.
        for (element in elements) {
            element.setPosition(indent, dy)
            element.width = this.width - 2* indent
            element.drawScreen(mouseX, mouseY, partialTicks)
            dy += element.height
        }
        length = dy + scrollOffs

        // Resetting the scissor test
        NVGR.endScissor()
        NVGR.pop()
    }

    fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int): Boolean {
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

    fun mouseReleased(mouseX: Float, mouseY: Float, state: Int) {
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
    fun scroll(amount: Int, mouseX: Float, mouseY: Float): Boolean {
        if (!isMouseInBox(mouseX, mouseY)) return false

        val diff = -amount * scrollAmount
        scrollOffs = (scrollOffs + diff).coerceAtMost(length - height + 20).coerceAtLeast(0f)
        return true

    }

    /**
     * Updates the position of the element.
     */
    private fun updatePosition() {
        x = (mc.window.width  * MainSettings.advancedRelX.value   / clickGui.scale).toFloat()
        y = (mc.window.height * MainSettings.advancedRelY.value   / clickGui.scale).toFloat()
        width =  (mc.window.width *MainSettings.ADVANCED_GUI_RELATIVE_WIDTH  / clickGui.scale).toFloat()
        height = (mc.window.height*MainSettings.ADVANCED_GUI_RELATIVE_HEIGHT / clickGui.scale).toFloat()
    }

    /**
     * Detects whether the mouse is within this gui.
     * @return true when the mouse is over the box of the Gui.
     */
    private fun isMouseInBox(mouseX: Float, mouseY: Float): Boolean{
        return mouseX >= x && mouseX < x + width && mouseY  >= y && mouseY < y + height
    }

    /**
     * Detects whether the mouse is on the top Bar of the Gui.
     */
    private fun isMouseOnTopBar(mouseX: Float, mouseY: Float): Boolean{
        return mouseX >= x && mouseX < x + width && mouseY  >= y && mouseY < y + 15
    }
}