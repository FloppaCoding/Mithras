package floppacoding.mithras.ui.core.elements

import floppacoding.aurora.core.CapStyle
import floppacoding.aurora.core.TextAlign

class ActionButton : GuiElement() {
    var label: CharSequence = ""

    var onClickCallback: (() -> Unit)? = null

    init {
        height = 10f
    }

    override fun render(mouseX: Float, mouseY: Float, delta: Float) {
        renderer.push()
        renderer.translate(x, y)
        renderer.textLine(label, 0f, 0f, fontColor, FONT_SIZE, font, textAlign = TextAlign.LEFT_TOP)

        renderer.line(
            width - ARROW_WIDTH + 0.5f * ARROW_LINE_WIDTH,
            2f,
            width - 0.5f * ARROW_LINE_WIDTH,
            5f,
            ARROW_LINE_WIDTH,
            -1,
            CapStyle.ROUND
        )
        renderer.line(
            width - ARROW_WIDTH + 0.5f * ARROW_LINE_WIDTH,
            8f,
            width - 0.5f * ARROW_LINE_WIDTH,
            5f,
            ARROW_LINE_WIDTH,
            -1,
            CapStyle.ROUND
        )

        renderer.pop()
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (isMouseOverButton(mouseX, mouseY)) {
            onClickCallback?.invoke()
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    private fun isMouseOverButton(mouseX: Float, mouseY: Float): Boolean {
        return isMouseOver(mouseX, mouseY)
    }

    companion object {

        private const val ARROW_WIDTH = 6f
        private const val ARROW_LINE_WIDTH = 1f

    }
}