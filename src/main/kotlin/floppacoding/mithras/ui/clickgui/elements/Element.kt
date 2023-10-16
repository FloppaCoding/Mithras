package floppacoding.mithras.ui.clickgui.elements

import floppacoding.mithras.module.impl.render.GUIDesign
import floppacoding.mithras.module.impl.render.MainSettings
import floppacoding.mithras.module.settings.Setting
import floppacoding.mithras.module.settings.impl.ColorSetting
import floppacoding.mithras.module.settings.impl.SelectorSetting
import floppacoding.mithras.ui.clickgui.ClickGUI
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import floppacoding.mithras.ui.nanovg.NVGR

/**
 * Parent class to the settings elements in the click gui.
 *
 * @author Aton
 */
abstract class Element<S: Setting<*>>(
    val parent: ModuleButton,
//    val module: Module,
    val setting: S,
    val type: ElementType
) {
    val clickgui: ClickGUI = parent.panel.clickgui
    /** Relative position of this element in respect to [parent]. */
    var x = 2f
    /** Relative position of this element in respect to [parent]. */
    var y = 0f
    val width = parent.width - 2f - x
    /** Height of the complete element included optional dropdown. */
    var height: Float
    var displayName: String = setting.name
    var extended = false
    var listening = false

    /** Absolute position of the panel on the screen. */
    val xAbsolute: Float
        get() = x + parent.x + parent.panel.x
    /** Absolute position of the panel on the screen. */
    val yAbsolute: Float
        get() = y + parent.y + parent.panel.y

    init {
        height = when (type) {
            ElementType.TEXT_FIELD -> 12f
            ElementType.KEY_BIND -> 11f
            ElementType.ACTION -> 11f
            else -> DEFAULT_HEIGHT
        }
    }

    /**
     * Updates the height of the Element based on [extended].
     */
    fun update() {
        displayName = setting.name
        when (type) {
            ElementType.SELECTOR -> {
                height = if (extended)
                    ((setting as SelectorSetting<*>).options.size * (NVGR.DEFAULT_FONT_HEIGHT + 2) + DEFAULT_HEIGHT)
                else
                    DEFAULT_HEIGHT
            }
            ElementType.COLOR -> {
                height = if (extended)
                    if((setting as ColorSetting).allowAlpha)
                        DEFAULT_HEIGHT * 5
                    else
                        DEFAULT_HEIGHT * 4
                else
                    DEFAULT_HEIGHT
            }
            else -> {}
        }
    }

    /**
     * Sets up the rendering of the element and dispatches rendering of the individual implementations.
     * @return the height of the element.
     * @see renderElement
     */
    fun drawScreen(mouseX: Float, mouseY: Float, partialTicks: Float) : Float {
        NVGR.push()
        NVGR.translate(x, y)

        val color = if (listening) {
            ColorUtil.clickGUIColor.rgb
        }else {
            ColorUtil.elementColor
        }

        /** Rendering the box */
        NVGR.rect(0f, 0f, width, height, color)
        /** The decor */
        if (MainSettings.design.isSelected(GUIDesign.NEW)) {
            NVGR.rect(width, 0f, 2f, height, ColorUtil.outlineColor)
        }

        // Render the element.
        val elementLength = renderElement(mouseX, mouseY, partialTicks)

        NVGR.pop()
        return elementLength
    }

    /**
     * To be overridden in the implementations.
     * @return the height of the element.
     */
    protected open fun renderElement(mouseX: Float, mouseY: Float, partialTicks: Float) : Float { return height }

    /**
     * Handles mouse clicks on the Element.
     * To be overridden in the implementations.
     * @return whether an action was performed.
     */
    open fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int): Boolean {
        return isHovered(mouseX, mouseY)
    }

    open fun mouseReleased(mouseX: Float, mouseY: Float, state: Int) {}

    /**
     * Overridden in the elements to enable key detection.
     * @return true when an action was taken.
     */
    open fun keyPressed(keyCode: Int, scanCode: Int): Boolean { return false }

    /**
     * Overridden in the elements to enable key detection.
     * @return true when an action was taken.
     */
    open fun charTyped(chr: Char, modifiers: Int): Boolean { return false }

    private fun isHovered(mouseX: Float, mouseY: Float): Boolean {
        return mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute && mouseY <= yAbsolute + height
    }

    companion object {
        const val DEFAULT_HEIGHT = 15f
    }
}