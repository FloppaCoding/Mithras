package floppacoding.mithras.ui.clickgui.elements

import floppacoding.mithras.module.Module
import floppacoding.mithras.module.impl.keybinds.KeyBind
import floppacoding.mithras.module.impl.render.GUIDesign
import floppacoding.mithras.module.impl.render.MainSettings
import floppacoding.mithras.module.settings.impl.*
import floppacoding.mithras.ui.clickgui.Panel
import floppacoding.mithras.ui.clickgui.advanced.AdvancedMenu
import floppacoding.mithras.ui.clickgui.elements.menu.*
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import floppacoding.mithras.utils.render.Renderer2D
import floppacoding.mithras.utils.render.TextAlign

/**
 * Provides the toggle button for modules in the click gui.
 *
 * @author Aton
 */
class ModuleButton(val module: Module, val panel: Panel) {
    val menuElements: ArrayList<Element<*>> = ArrayList()
    val renderer: Renderer2D = panel.renderer
    /** Relative position of this button in respect to [panel]. */
    var x = 0f
    /** Relative position of this button in respect to [panel]. */
    var y = 0f
    val width = panel.width
    val height = (renderer.defaultFontHeight + 2f)
    var extended = false
    /** Absolute position of the panel on the screen. */
    val xAbsolute: Float
        get() = x + panel.x
    /** Absolute position of the panel on the screen. */
    val yAbsolute: Float
        get() = y + panel.y

    init {
        /** Register the corresponding gui element for all non-hidden settings in the module */
        updateElements()
        menuElements.add(ElementKeyBind(this, module))
    }

    /**
     * Updates the [menuElements].
     *
     * This is used to initially populate the elements and to update the list based on the visibility condition of the settings.
     * @see floppacoding.mithras.module.settings.Setting.shouldBeVisible
     */
    fun updateElements() {
        var position = -1 // This looks weird, but it starts at -1 because it gets incremented before being used.
        for (setting in module.settings) {
            /** Don't show hidden settings */
            if (setting.visibility.visibleInClickGui && setting.shouldBeVisible) run addElement@{
                position++
                if (menuElements.any { it.setting === setting }) return@addElement
                val newElement = when (setting) {
                    is BooleanSetting ->    ElementCheckBox(this, setting)
                    is NumberSetting ->     ElementSlider(this, setting)
                    is SelectorSetting ->   ElementSelector(this, setting)
                    is StringSetting ->     ElementTextField(this, setting)
                    is ColorSetting ->      ElementColor(this, setting)
                    is ActionSetting ->     ElementAction(this, setting)
                    else -> return@addElement
                }
                menuElements.add(position, newElement)
            }else {
                menuElements.removeIf {
                    it.setting === setting
                }
            }
        }
    }

    /**
	 * Render the Button.
     * Dispatches rendering of its [menuElements].
     * @return The height of the button.
	 */
    fun drawScreen(mouseX: Float, mouseY: Float, partialTicks: Float) : Float {

        renderer.push()
        renderer.translate(x, y)

        renderer.rect(0f, 0f, width, height + 1f, ColorUtil.MODULE_BUTTON_COLOR)
        if (MainSettings.design.isSelected(GUIDesign.NEW)) {
            renderer.rect(0f, 0f, 2f, height + 1f, ColorUtil.outlineColor)
        }

        /** Draw the highlight when the module is enabled. */
        if (module.enabled) {
            renderer.rect(0f, 0f, width, height + 1f, ColorUtil.outlineColor)
        }

        /** Change color on hover */
        if (isButtonHovered(mouseX, mouseY)) {
            if (module.enabled)
                renderer.rect(0f, 0f, width, height+1f, ColorUtil.MODULE_HOVER_ENABLED)
            else
                renderer.rect(0f, 0f, width, height+1f, ColorUtil.hoverColor)
        }

        /** Rendering the name in the middle */
        val displayName = if (module is KeyBind){
            module.bindName.text
        } else {
            module.name
        }
        renderer.text(displayName, width / 2f, 1f + height / 2f, ColorUtil.TEXT_COLOR, textAlign = TextAlign.CENTER_MIDDLE)

        /** Render the settings elements */
        var offs = height + 1
        if (extended && menuElements.isNotEmpty()) {
            for (menuElement in menuElements) {
                menuElement.y = offs
                menuElement.update()

                offs += menuElement.drawScreen(mouseX, mouseY, partialTicks)
            }
        }

        renderer.pop()

        return offs
    }

    /**
	 * Handles mouse clicks for this element and dispatches them to its [menuElements].
     * @return true if an action was performed.
     * @see Element.mouseClicked
	 */
    fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int): Boolean {
        if (isButtonHovered(mouseX, mouseY)) {
            /** Toggle the mod on left click, expand its settings on right click and show an info screen on middle click */
            when (mouseButton) {
                0 -> {
                    module.toggle()
                    return true
                }
                1 -> {
                    /** toggle extended
                     * Disable listening for all members*/
                    if (menuElements.size > 0) {
                        extended = !extended
                        if (!extended) {
                            menuElements.forEach {
                                it.listening = false
                            }
                        }
                    }
                    return true
                }
                2 -> {
                    panel.clickgui.advancedMenu = AdvancedMenu(module, panel.clickgui)
                    return true
                }
            }
        }else if (isMouseUnderButton(mouseX, mouseY) || menuElements.any { element -> element is ElementKeyBind && element.listening}) {
            for (menuElement in menuElements.reversed()) {
                if (menuElement.mouseClicked(mouseX, mouseY, mouseButton)) {
                    updateElements()
                    return true
                }
            }
        }
        return false
    }

    /**
     * Dispatches mouse released actions to its [menuElements]
     * @see Element.mouseReleased
     */
    fun mouseReleased(mouseX: Float, mouseY: Float, state: Int) {
        if (extended) {
            for (menuElement in menuElements.reversed()) {
                menuElement.mouseReleased(mouseX, mouseY, state)
            }
        }
    }

    /**
     * Dispatches key press to its [menuElements].
     * @return true if any of the elements used the input.
     * @see Element.keyPressed
     */
    fun keyPressed(keyCode: Int, scanCode: Int): Boolean {
        if (extended) {
            for (menuElement in menuElements.reversed()) {
                if (menuElement.keyPressed(keyCode, scanCode)) return true
            }
        }
        return false
    }

    /**
     * Dispatches typed characters to its [menuElements].
     * @return true if any of the elements used the input.
     * @see Element.charTyped
     */
    fun charTyped(chr: Char, modifiers: Int): Boolean {
        if (extended) {
            for (menuElement in menuElements.reversed()) {
                if (menuElement.charTyped(chr, modifiers)) return true
            }
        }
        return false
    }

    /**
     * Returns true when the mouse is hovering over the module button.
     */
    private fun isButtonHovered(mouseX: Float, mouseY: Float): Boolean {
        return mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute && mouseY <= yAbsolute + height
    }

    /**
     * Returns true when the Settings are extended and the mouse below the Module Button.
     */
    private fun isMouseUnderButton(mouseX: Float, mouseY: Float): Boolean {
        if (!extended) return false
        return mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY > yAbsolute + height
    }
}