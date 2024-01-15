package floppacoding.mithras.ui.core.elements

import floppacoding.aurora.core.TextAlign
import java.awt.Color

class ToggleButton : GuiElement() {
    var label: CharSequence = ""
    var enabledColor: Int = Color(0,150,250,200).rgb
    var disabledColor: Int = Color(50, 50 ,50, 200).rgb
    var knobColor: Int = Color(255, 255, 255, 255).rgb

    var enabled = false

    var onToggleCallback: ((newState: Boolean) -> Unit)? = null

    init {
        height = SLIDER_HEIGHT
    }

    override fun render(mouseX: Float, mouseY: Float, delta: Float) {
        renderer.push()
        renderer.translate(x, y)
        renderer.textLine(label, 0f, 0f, fontColor, FONT_SIZE, font, textAlign = TextAlign.LEFT_TOP)

        val color = if (enabled) enabledColor else disabledColor
        renderer.roundedRect(width - SLIDER_WIDTH, 0f, SLIDER_WIDTH, SLIDER_HEIGHT, SLIDER_RADIUS, color)
        val offset = if (enabled) 0f else DISABLED_OFFSET
        renderer.circle(width- SLIDER_RADIUS - offset, SLIDER_RADIUS, KNOB_RADIUS, knobColor)

        renderer.pop()
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (isMouseOverButton(mouseX, mouseY)) {
            enabled = !enabled
            onToggleCallback?.invoke(enabled)
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    private fun isMouseOverButton(mouseX: Float, mouseY: Float): Boolean {
        return mouseX < x + width && mouseX > x + width - SLIDER_WIDTH && mouseY > y && mouseY < y + SLIDER_HEIGHT
    }

    companion object {
        private const val SLIDER_HEIGHT = 10f // TODO make this depend on font size?
        private const val SLIDER_RADIUS = SLIDER_HEIGHT * 0.5f
        private const val KNOB_RADIUS = 0.4f * SLIDER_HEIGHT
        private const val SLIDER_WIDTH = 2f * SLIDER_HEIGHT
        private const val DISABLED_OFFSET = SLIDER_WIDTH - SLIDER_HEIGHT
    }
}