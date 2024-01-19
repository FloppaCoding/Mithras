package floppacoding.mithras.ui.core.elements

import floppacoding.aurora.core.TextAlign
import floppacoding.mithras.utils.render.ImageManager
import org.lwjgl.glfw.GLFW
import java.awt.Color

// TODO SB and alpha slider / dropdown
class ColorPicker : GuiElement() {
    var hasAlpha = true
    var hue = 0f
    var saturation = 0f
    var brightness = 0f
    var alpha = 1f
    var label: CharSequence = ""

    var backgroundColor: Int = Color(0,0,0,50).rgb

    var onChangeCallback: ((color: Color) -> Unit)? = null
    var onFinishedCallback: ((color: Color) -> Unit)? = null

    var dragging = false
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
    }

    override fun close() {
        if (dragging) {
            dragging = false
            val color = Color.getHSBColor(hue, saturation, brightness)
            onFinishedCallback?.invoke(color)
        }
        extended = false
        super.close()
    }

    override fun render(mouseX: Float, mouseY: Float, delta: Float) {
        val bobberX = width*hue


        renderer.push()
        renderer.translate(x, y)

        // TODO prevent label and value overlap?!
        renderer.textLine(label, 0f, 0f, fontColor, FONT_SIZE, font, TextAlign.LEFT_TOP)



        renderer.translate(0f, OFFSET)
        // The slider progress
        renderer.roundedImage(ImageManager.HUE_SCALE, 0f, (SLIDER_HEIGHT - SLIDER_BAR_HEIGHT) /2, width, SLIDER_BAR_HEIGHT, SLIDER_CORNER_RADIUS)
        // The knob indicating the progress
        val color = Color.getHSBColor(hue, saturation, brightness)
        renderer.circle(bobberX, SLIDER_HEIGHT /2, KNOB_RADIUS, color.rgb)

        if (extended) {

            renderer.translate(0f, SLIDER_HEIGHT)

            val topY = (SB_FIELD_OFFSET - SB_FIELD_HEIGHT) * 0.5f
            renderer.roundedRect(0f, topY, width, SB_FIELD_HEIGHT, SB_FIELD_CORNER_RADIUS, -1)



        }

        renderer.pop()

        if (dragging) {
            hue = ((mouseX - x) / width).coerceIn(0f ,1f)
            onChangeCallback?.invoke(color)
        }
    }





    override fun mouseClicked(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if(button == GLFW.GLFW_MOUSE_BUTTON_1 && isMouseOverHueSlider(mouseX, mouseY)) {
            dragging = true
            return true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (dragging && button == GLFW.GLFW_MOUSE_BUTTON_1) {
            dragging = false
            val color = Color.getHSBColor(hue, saturation, brightness)
            onFinishedCallback?.invoke(color)
            return true
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    private fun isMouseOverHueSlider(mouseX: Float, mouseY: Float): Boolean {
        // TODO fix this
        return mouseX >= x && mouseX < x + width && mouseY >= y + OFFSET && mouseY < y + height
    }

    //TODO combine this with slider?
    companion object {
        private const val SLIDER_BAR_HEIGHT = 2f
        private const val SLIDER_CORNER_RADIUS = SLIDER_BAR_HEIGHT / 2
        private const val KNOB_RADIUS = 2f

        private const val SLIDER_HEIGHT = 5f
        private const val NORMAL_HEIGHT = 15f
        private const val OFFSET = NORMAL_HEIGHT - SLIDER_HEIGHT
        private const val SB_FIELD_OFFSET = 30f
        private const val SB_FIELD_HEIGHT = 25f
        private const val SB_FIELD_CORNER_RADIUS = 3f
    }
}