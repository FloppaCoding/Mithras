package floppacoding.mithras.ui.clickgui

import floppacoding.mithras.module.Category
import floppacoding.mithras.module.ModuleManager
import floppacoding.mithras.module.impl.render.ClickGui
import floppacoding.mithras.ui.clickgui.elements.ModuleButton
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import floppacoding.mithras.ui.clickgui.util.FontUtil
import floppacoding.mithras.ui.clickgui.util.FontUtil.capitalizeOnlyFirst
import floppacoding.mithras.utils.render.HUDRenderUtils
import net.minecraft.client.gui.DrawContext

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

    var dragging = false
    val visible = true // Currently unused, but can be used in future for hiding categories
    val moduleButtons: ArrayList<ModuleButton> = ArrayList()

    val width = ClickGui.panelWidth.value.toInt()
    val height = ClickGui.panelHeight.value.toInt()
    /** Absolute position of the panel on the screen. */
    var x = ClickGui.panelX[category]!!.value.toInt()
    /** Absolute position of the panel on the screen. */
    var y = ClickGui.panelY[category]!!.value.toInt()
    var extended: Boolean = ClickGui.panelExtended[category]!!.enabled

    private var scrollOffset = 0

    /** The length of the extended panel */
    private var length = 0
    /** Used as temporary reference for dragging the panel. */
    private var x2 = 0
    /** Used as temporary reference for dragging the panel. */
    private var y2 = 0

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
    fun drawScreen(context: DrawContext, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!visible) return
        if (dragging) {
            x = x2 + mouseX
            y = y2 + mouseY
        }

        // Set up Transform
        context.matrices.push()
        context.matrices.translate(x.toFloat(), y.toFloat(), 0f)

        // Set up the Scissor Box
        HUDRenderUtils.setUpScissor(x - 1, y, width + 2, 4000, ClickGUI.CLICK_GUI_SCALE)

        /** Render the module buttons and the Settings elements */
        var startY = height
        if (extended && moduleButtons.isNotEmpty()) {
            startY -= scrollOffset
            for (moduleButton in moduleButtons) {
                // Render the module Button
                moduleButton.y = startY

                startY += moduleButton.drawScreen(context, mouseX, mouseY, partialTicks)
            }
            length = startY+5
        }

        // Resetting the scissor
        HUDRenderUtils.endScissor()

        // Render the Panel
        context.fill(0, 0, width, height, ColorUtil.dropDownColor)
        context.fill(0, startY, width, startY + 5, ColorUtil.dropDownColor)

        // Render decor
        if (ClickGui.design.isSelected("New")) {
            context.fill(0, 0,  2, height, ColorUtil.outlineColor)
            context.fill(0, startY,  2, startY+5, ColorUtil.outlineColor)
            FontUtil.drawStringWithShadow(context, title, 4.0, height / 2.0 - FontUtil.fontHeight / 2.0)
        } else if (ClickGui.design.isSelected("JellyLike")) {
            context.fill(4, 2, 5, height - 2, ColorUtil.jellyPanelColor)
            context.fill(width - 4, 2, width - 5, height - 2, ColorUtil.jellyPanelColor)
            FontUtil.drawTotalCenteredStringWithShadow(context, title, width / 2.0, height / 2.0)
        }

        context.matrices.pop()
    }

    /**
	 * Handles clicks on the panel and disptaches the click to its [moduleButtons].
     * @see ModuleButton.mouseClicked
	 */
    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
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
        }else if (isMouseOverExtended(mouseX, mouseY)) {
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
     * Also takes care of updating the state of the panel to [ClickGui].
     *
     * @see ModuleButton.mouseReleased
	 */
    fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (!visible) {
            return
        }
        if (state == 0) {
            dragging = false
        }

        // saving changes on mouse release instead of in the individual positions to have it all in one place
        ClickGui.panelX[category]!!.value = x.toDouble()
        ClickGui.panelY[category]!!.value = y.toDouble()
        ClickGui.panelExtended[category]!!.enabled = extended

        if (extended) {
            for (moduleButton in moduleButtons.reversed()) {
                moduleButton.mouseReleased(mouseX, mouseY, state)
            }
        }
    }

    /**
     * Dispatches key press to its [moduleButtons].
     * @return true if any of the modules used the input.
     * @see ModuleButton.keyTyped
     */
    fun keyTyped(typedChar: String, keyCode: Int): Boolean{
        if (extended && visible) {
            for (moduleButton in moduleButtons.reversed()) {
                if (moduleButton.keyTyped(typedChar, keyCode)) return true
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
    fun scroll(amount: Int, mouseX: Int, mouseY: Int): Boolean {
        if (!visible) return false
        if (isMouseOverExtended(mouseX, mouseY)) {
            val diff = (-amount * SCROLL_DISTANCE).coerceAtMost(length - height - 16)

            val realDiff = (scrollOffset + diff).coerceAtLeast(0) - scrollOffset

            length -= realDiff
            scrollOffset += realDiff
            return true
        }
        return false
    }

    /**
	 * Returns true when the mouse is hovering the top Panel Button.
	 */
    private fun isHovered(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height
    }

    /**
     * Returns true when the Panel is extended and the mouse is over the Panel or its extended part.
     */
    private fun isMouseOverExtended(mouseX: Int, mouseY: Int): Boolean {
        if (!extended) return false
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + length
    }

    companion object {
        private const val SCROLL_DISTANCE = 11
    }
}