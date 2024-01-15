package floppacoding.mithras.ui.core.elements

import floppacoding.aurora.core.Renderer2D
import floppacoding.mithras.Mithras

abstract class GuiElement {
    // TODO centralize this with the one in GuiScreen
    protected val renderer: Renderer2D
        get() = Mithras.renderer2D

    protected var _x = 0f
    protected var _y = 0f
    protected var _width = 0f
    protected var _height = 0f

    open var x: Float
        get() = _x
        set(value) { _x = value }
    open var y: Float
        get() = _y
        set(value) { _y = value }
    open var width: Float
        get() = _width
        set(value) { _width = value }
    open var height: Float
        get() = _height
        set(value) { _height = value }
    // TODO: Decide where to handle translation and optionally scissoring
    abstract fun render(mouseX: Float, mouseY: Float, delta: Float)

    // TODO remove this maybe?
//    open fun mouseMoved(mouseX: Float, mouseY: Float) {}

    open fun mouseClicked(mouseX: Float, mouseY: Float, button: Int): Boolean { return false }

    open fun mouseReleased(mouseX: Float, mouseY: Float, button: Int): Boolean { return false }

    // TODO maybe remove this?
//    open fun mouseDragged(mouseX: Float, mouseY: Float, button: Int, deltaX: Float, deltaY: Float): Boolean { return false }

    open fun mouseScrolled(mouseX: Float, mouseY: Float, amount: Float): Boolean { return false }

    open fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean { return false }

    open fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean { return false }

    open fun charTyped(chr: Char, modifiers: Int): Boolean { return false }

    open fun isMouseOver(mouseX: Float, mouseY: Float): Boolean {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height
    }

    //TODO add delegation to useful screen methods? or maybe better fresh implementation? Or put it in separate class?
    // hasControlDown() public static boolean hasShiftDown() hasAltDown()  isCut(int code)  isPaste(int code)  isCopy(int code)  isSelectAll(int code)

    companion object {
        // TODO centralize somewhere
        const val FONT_SIZE = 9f
        val fontColor = -1
        val font = Mithras.renderer2D.defaultFont
    }
}
