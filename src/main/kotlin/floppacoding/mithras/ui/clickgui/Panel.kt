package floppacoding.mithras.ui.clickgui

import floppacoding.mithras.module.Category
import floppacoding.mithras.module.ModuleManager
import floppacoding.mithras.module.impl.render.GUIDesign
import floppacoding.mithras.module.impl.render.MainSettings
import floppacoding.mithras.ui.clickgui.elements.ModuleButton
import floppacoding.mithras.ui.clickgui.elements.menu.ElementKeyBind
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import floppacoding.mithras.ui.clickgui.util.FontUtil.capitalizeOnlyFirst
import floppacoding.mithras.utils.render.Renderer2D
import floppacoding.mithras.utils.render.TextAlign

/**
 * Provides a category panel for the click gui.
 *
 * @author Aton
 */
class Panel(
    var category: Category,
    var clickgui: ClickGUI
) {
    private val title: String = category.name.capitalizeOnlyFirst()

    val renderer: Renderer2D by clickgui::renderer

    var dragging = false
    val visible = true // Currently unused, but can be used in future for hiding categories
    val moduleButtons: ArrayList<ModuleButton> = ArrayList()

    val width = MainSettings.panelWidth.value.toFloat()
    val height = MainSettings.panelHeight.value.toFloat()
    /** Absolute position of the panel on the screen. */
    var x = MainSettings.panelX[category]!!.value.toFloat()
    /** Absolute position of the panel on the screen. */
    var y = MainSettings.panelY[category]!!.value.toFloat()
    var extended: Boolean = MainSettings.panelExtended[category]!!.enabled

    private var scrollOffset = 0f

    /** The length of the extended panel */
    private var length = 0f
    /** Used as temporary reference for dragging the panel. */
    private var x2 = 0f
    /** Used as temporary reference for dragging the panel. */
    private var y2 = 0f

    init {
        for (module in ModuleManager.modules) {
            if (module.category != this.category) continue
            moduleButtons.add(ModuleButton(module, this))
        }
    }

    /**
	 * Renders the panel and dispatches the rendering of its [moduleButtons].
     * @see ModuleButton.drawScreen
	 */
    fun drawScreen(mouseX: Float, mouseY: Float, partialTicks: Float) {
        if (!visible) return
        if (dragging) {
            x = x2 + mouseX
            y = y2 + mouseY
        }

        // Set up Transform
        renderer.push()
        renderer.translate(x, y)

        // Set up the Scissor Box
        renderer.scissor(1f, height, width + 2f, 4000f)

        /** Render the module buttons and the Settings elements */
        var startY = height
        if (extended && moduleButtons.isNotEmpty()) {
            startY -= scrollOffset
            for (moduleButton in moduleButtons) {
                // Render the module Button
                moduleButton.y = startY

                startY += moduleButton.drawScreen(mouseX, mouseY, partialTicks)
            }
            length = startY+5f
        }

        // Resetting the scissor
        renderer.endScissor()

        // Render the Panel
        renderer.rect(0f, 0f, width, height,  ColorUtil.DROPDOWN_COLOR)
        renderer.rect(0f, startY, width, 5f,  ColorUtil.DROPDOWN_COLOR)

        // Render decor
        if (MainSettings.design.isSelected(GUIDesign.NEW)) {
            renderer.rect(0f, 0f,  2f, height, ColorUtil.outlineColor)
            renderer.rect(0f, startY,  2f, 5f, ColorUtil.outlineColor)
            renderer.text(title, 4f, height/2f, ColorUtil.TEXT_COLOR, textAlign = TextAlign.LEFT_MIDDLE)
        } else if (MainSettings.design.isSelected(GUIDesign.JELLYLIKE)) {
            renderer.rect(4f, 2f,  1f, height-4f, ColorUtil.JELLY_PANEL_COLOR)
            renderer.rect(width - 4f, 2f,  -1f, height - 4f, ColorUtil.JELLY_PANEL_COLOR)
            renderer.text(title, width/2f, height/2f, ColorUtil.TEXT_COLOR, textAlign = TextAlign.CENTER_MIDDLE)
        }

        renderer.pop()
    }

    /**
	 * Handles clicks on the panel and disptaches the click to its [moduleButtons].
     * @see ModuleButton.mouseClicked
	 */
    fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int): Boolean {
        if (!visible) {
            return false
        }
        if (isHovered(mouseX, mouseY)) {
            if (mouseButton == 0) {
                x2 = x - mouseX
                y2 = y - mouseY
                dragging = true
                return true
            } else if (mouseButton == 1 ) {
                extended = !extended
                return true
            }
        }else if (isMouseOverExtended(mouseX, mouseY) || moduleButtons.any { it.menuElements.any { element -> element is ElementKeyBind && element.listening} }) {
            for (moduleButton in moduleButtons.reversed()) {
                if (moduleButton.mouseClicked(mouseX, mouseY, mouseButton)) {
                    return true
                }
            }
        }
        return false
    }

    /**
	 * Handles mouse release for the panel and dispatches it to its [moduleButtons].
     *
     * Also takes care of updating the state of the panel to [MainSettings].
     *
     * @see ModuleButton.mouseReleased
	 */
    fun mouseReleased(mouseX: Float, mouseY: Float, state: Int) {
        if (!visible) {
            return
        }
        if (state == 0) {
            dragging = false
        }

        // saving changes on mouse release instead of in the individual positions to have it all in one place
        MainSettings.panelX[category]!!.value = x.toDouble()
        MainSettings.panelY[category]!!.value = y.toDouble()
        MainSettings.panelExtended[category]!!.enabled = extended

        if (extended) {
            for (moduleButton in moduleButtons.reversed()) {
                moduleButton.mouseReleased(mouseX, mouseY, state)
            }
        }
    }

    /**
     * Dispatches key press to its [moduleButtons].
     * @return true if any of the modules used the input.
     * @see ModuleButton.keyPressed
     */
    fun keyPressed(keyCode: Int, scanCode: Int): Boolean{
        if (extended && visible) {
            for (moduleButton in moduleButtons.reversed()) {
                if (moduleButton.keyPressed(keyCode, scanCode)) return true
            }
        }
        return false
    }

    /**
     * Dispatches char typed to its [moduleButtons].
     * @return true if any of the modules used the input.
     * @see ModuleButton.charTyped
     */
    fun charTyped(chr: Char, modifiers: Int): Boolean{
        if (extended && visible) {
            for (moduleButton in moduleButtons.reversed()) {
                if (moduleButton.charTyped(chr, modifiers)) return true
            }
        }
        return false
    }

    /**
     * Scrolls the panel extension by the given number of elements.
     * If the panel is not extended does nothing.
     *
     * @param amount The amount to scroll
     */
    fun scroll(amount: Int, mouseX: Float, mouseY: Float): Boolean {
        if (!visible) return false
        if (isMouseOverExtended(mouseX, mouseY)) {
            val diff = (-amount * SCROLL_DISTANCE).coerceAtMost(length - height - 16f)

            val realDiff = (scrollOffset + diff).coerceAtLeast(0f) - scrollOffset

            length -= realDiff
            scrollOffset += realDiff
            return true
        }
        return false
    }

    /**
	 * Returns true when the mouse is hovering the top Panel Button.
	 */
    private fun isHovered(mouseX: Float, mouseY: Float): Boolean {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height
    }

    /**
     * Returns true when the Panel is extended and the mouse is over the Panel or its extended part.
     */
    private fun isMouseOverExtended(mouseX: Float, mouseY: Float): Boolean {
        if (!extended) return false
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + length
    }

    companion object {
        private const val SCROLL_DISTANCE = 11f
    }
}