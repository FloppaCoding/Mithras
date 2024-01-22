package floppacoding.mithras.ui.core.elements

import floppacoding.aurora.core.CapStyle
import floppacoding.aurora.core.TextAlign
import floppacoding.mithras.utils.Extensions.withAlpha
import floppacoding.mithras.utils.render.ImageManager
import org.lwjgl.glfw.GLFW
import java.awt.Color

// TODO SB and alpha slider / dropdown
class ColorPicker : GuiElement() {
    var hasAlpha = true
    var hue = 0f
    var saturation = 1f
    var brightness = 1f
    var alpha = 1f
    var label: CharSequence = ""


    var onChangeCallback: ((color: Color) -> Unit)? = null
    var onFinishedCallback: ((color: Color) -> Unit)? = null

    var dragging: DragElement? = null
    var extended = false


    init {
        height = NORMAL_HEIGHT
    }

    fun setColor(color: Color) {
        val newHSB = Color.RGBtoHSB(color.red, color.green, color.blue, null)
        brightness = newHSB[2]
        if (newHSB[2] > 0) {
            saturation = newHSB[1]
            if (newHSB[1] > 0){
                hue = newHSB[0]
            }
        }
        if (hasAlpha) {
            alpha = color.alpha / 255f
        }
    }

    override fun close() {
        if (dragging != null) {
            dragging = null
            val color = Color.getHSBColor(hue, saturation, brightness).withAlpha((alpha * 255).toInt())
            onFinishedCallback?.invoke(color)
        }
        extended = false
        super.close()
    }

    override fun render(mouseX: Float, mouseY: Float, delta: Float) {
        val bobberX = width*hue
        val color = Color.getHSBColor(hue, saturation, brightness).withAlpha((alpha * 255).toInt())

        renderer.push()
        renderer.translate(x, y)

        // TODO prevent label and value overlap?!
        renderer.textLine(label, 0f, 0f, fontColor, FONT_SIZE, font, TextAlign.LEFT_TOP)
        renderer.roundedRect(width - PREVIEW_WIDTH, 0f, PREVIEW_WIDTH, PREVIEW_HEIGHT, PREVIEW_CORNER_RADIUS, color.rgb)

        renderer.line(
            width - PREVIEW_WIDTH - DROP_DOWN_MARGIN - DROP_DOWN_WIDTH + 0.5f * DROP_DOWN_LINE_WIDTH,
            0.5f * DROP_DOWN_LINE_WIDTH + DROP_DOWN_V_MARGIN,
            width - PREVIEW_WIDTH - DROP_DOWN_MARGIN - 0.5f * DROP_DOWN_WIDTH,
            DROP_DOWN_HEIGHT - 0.5f * DROP_DOWN_LINE_WIDTH,
            DROP_DOWN_LINE_WIDTH,
            -1,
            CapStyle.ROUND
        )
        renderer.line(
            width - PREVIEW_WIDTH - DROP_DOWN_MARGIN  - 0.5f * DROP_DOWN_LINE_WIDTH,
            0.5f * DROP_DOWN_LINE_WIDTH + DROP_DOWN_V_MARGIN,
            width - PREVIEW_WIDTH - DROP_DOWN_MARGIN - 0.5f * DROP_DOWN_WIDTH,
            DROP_DOWN_HEIGHT - 0.5f * DROP_DOWN_LINE_WIDTH,
            DROP_DOWN_LINE_WIDTH,
            -1,
            CapStyle.ROUND
        )



        renderer.translate(0f, OFFSET)
        // The slider progress
        renderer.roundedImage(ImageManager.HUE_SCALE, 0f, (SLIDER_HEIGHT - SLIDER_BAR_HEIGHT) /2, width, SLIDER_BAR_HEIGHT, SLIDER_CORNER_RADIUS)
        // The knob indicating the progress

        val knobColor = Color.getHSBColor(hue, 1f, 1f)
        renderer.circle(bobberX, SLIDER_HEIGHT /2, KNOB_RADIUS, knobColor.rgb)
        renderer.border(bobberX - KNOB_RADIUS, SLIDER_HEIGHT /2 - KNOB_RADIUS, 2* KNOB_RADIUS, 2* KNOB_RADIUS, 0.5f, KNOB_RADIUS, WHITE)

        if (extended) {

            renderer.translate(0f, SLIDER_HEIGHT + SB_FILED_V_MARGIN)

            renderer.roundedRect(0f, 0f, width, SB_FIELD_HEIGHT, SB_FIELD_CORNER_RADIUS, -1)
                .setFourColorFade(
                    Color.getHSBColor(hue, 0f, 1f).rgb,
                    Color.getHSBColor(hue, 0f, 0f).rgb,
                    Color.getHSBColor(hue, 1f, 0f).rgb,
                    Color.getHSBColor(hue, 1f, 1f).rgb
                )

            val indicatorX = width * saturation
            val indicatorY = SB_FIELD_HEIGHT * (1 - brightness)

            // TODO implement circle border.
            renderer.border(indicatorX - SB_INDICATOR_RADIUS, indicatorY - SB_INDICATOR_RADIUS, 2* SB_INDICATOR_RADIUS, 2* SB_INDICATOR_RADIUS, 0.5f, SB_INDICATOR_RADIUS, WHITE)

            if (hasAlpha) {
                renderer.translate(0f, SB_FIELD_OFFSET)
                renderer.roundedRect(0f, (SLIDER_HEIGHT - SLIDER_BAR_HEIGHT) /2, width, SLIDER_BAR_HEIGHT, SLIDER_CORNER_RADIUS, -1).setHorizontalFade(
                    color.withAlpha(0).rgb,
                    color.withAlpha(255).rgb
                )
                // The knob indicating the progress
                val alphaX = width*alpha
                renderer.circle(alphaX, SLIDER_HEIGHT /2, KNOB_RADIUS, color.rgb)
                renderer.border(alphaX - KNOB_RADIUS, SLIDER_HEIGHT /2 - KNOB_RADIUS, 2* KNOB_RADIUS, 2* KNOB_RADIUS, 0.5f, KNOB_RADIUS, WHITE)
            }
        }

        renderer.pop()

        when (dragging) {
            DragElement.HUE -> hue = ((mouseX - x) / width).coerceIn(0f ,1f)
            DragElement.SB -> {
                saturation = ((mouseX - x) / width).coerceIn(0f ,1f)
                brightness = (((y + OFFSET + SLIDER_HEIGHT + SB_FILED_V_MARGIN + SB_FIELD_HEIGHT) - mouseY) / SB_FIELD_HEIGHT).coerceIn(0f ,1f)
            }
            DragElement.ALPHA -> alpha = ((mouseX - x) / width).coerceIn(0f ,1f)
            null -> {}
        }
        if (dragging != null) onChangeCallback?.invoke(color)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if(button == GLFW.GLFW_MOUSE_BUTTON_1) {
            when {
                isMouseOverDropDown(mouseX, mouseY) -> {
                    extended = !extended
                    return true
                }
                isMouseOverHueSlider(mouseX, mouseY) -> dragging = DragElement.HUE
                isMouseOverSBField(mouseX, mouseY) -> dragging = DragElement.SB
                isMouseOverAlphaSlider(mouseX, mouseY) -> dragging = DragElement.ALPHA
            }
            if (dragging != null) return true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (dragging != null && button == GLFW.GLFW_MOUSE_BUTTON_1) {
            dragging = null
            val color = Color.getHSBColor(hue, saturation, brightness).withAlpha((alpha * 255).toInt())
            onFinishedCallback?.invoke(color)
            return true
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    private fun isMouseOverHueSlider(mouseX: Float, mouseY: Float): Boolean {
        // TODO fix this
        return mouseX >= x && mouseX < x + width && mouseY >= y + OFFSET && mouseY < y + OFFSET + SLIDER_HEIGHT
    }

    private fun isMouseOverSBField(mouseX: Float, mouseY: Float): Boolean {
        // TODO fix this
        return mouseX >= x && mouseX < x + width && mouseY >= y + OFFSET + SLIDER_HEIGHT + SB_FILED_V_MARGIN && mouseY < y + OFFSET + SLIDER_HEIGHT + SB_FILED_V_MARGIN + SB_FIELD_HEIGHT
    }

    private fun isMouseOverAlphaSlider(mouseX: Float, mouseY: Float): Boolean {
        // TODO fix this
        return mouseX >= x && mouseX < x + width && mouseY >= y + OFFSET + SLIDER_HEIGHT + SB_FIELD_OFFSET && mouseY < y + OFFSET + SLIDER_HEIGHT + SB_FIELD_OFFSET + SLIDER_HEIGHT
    }

    private fun isMouseOverDropDown(mouseX: Float, mouseY: Float): Boolean {
        // TODO fix this
        return mouseX >= x + width - PREVIEW_WIDTH - DROP_DOWN_MARGIN - DROP_DOWN_WIDTH && mouseX < x + width && mouseY >= y && mouseY < y + OFFSET
    }

    enum class DragElement {
        HUE, SB, ALPHA;
    }

    //TODO combine this with slider?
    companion object {
        private const val PREVIEW_WIDTH = 10f
        private const val PREVIEW_HEIGHT = FONT_SIZE
        private const val PREVIEW_CORNER_RADIUS = 2f

        private const val DROP_DOWN_WIDTH = 7f
        private const val DROP_DOWN_LINE_WIDTH = 1f
        private const val DROP_DOWN_HEIGHT = FONT_SIZE * 0.6f
        private const val DROP_DOWN_V_MARGIN = FONT_SIZE * 0.2f
        private const val DROP_DOWN_MARGIN = 2f

        private const val SLIDER_BAR_HEIGHT = 3f
        private const val SLIDER_CORNER_RADIUS = SLIDER_BAR_HEIGHT / 2
        private const val KNOB_RADIUS = 3f

        private const val SLIDER_HEIGHT = 5f
        private const val NORMAL_HEIGHT = 15f
        private const val OFFSET = NORMAL_HEIGHT - SLIDER_HEIGHT
        private const val SB_FIELD_OFFSET = 55f
        private const val SB_FIELD_HEIGHT = 50f
        private const val SB_FILED_V_MARGIN = (SB_FIELD_OFFSET - SB_FIELD_HEIGHT) * 0.5f
        private const val SB_FIELD_CORNER_RADIUS = 3f
        private const val SB_INDICATOR_RADIUS = KNOB_RADIUS

        private const val BLACK: Int = -0x1000000 // = 0xff_00_00_00u.toInt()
        private const val WHITE: Int = -1         // = 0xff_ff_ff_ffu.toInt
    }
}