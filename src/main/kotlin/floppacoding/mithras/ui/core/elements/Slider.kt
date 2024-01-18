package floppacoding.mithras.ui.core.elements

import floppacoding.aurora.core.TextAlign
import floppacoding.mithras.ui.hud.Test
import org.joml.Math
import org.lwjgl.glfw.GLFW
import java.awt.Color

class Slider : GuiElement() {
    var color: Int = -1
    var backgroundColor: Int = Color(0,0,0,50).rgb

    var showValue: Boolean = true
        set(newValue) {
            field = newValue
            height = if (field || label != null) {
                NORMAL_HEIGHT
            }else {
                SLIDER_HEIGHT
            }
        }
    var label: CharSequence? = null
        set(newValue) {
            field = newValue
            height = if (showValue || field != null) {
                NORMAL_HEIGHT
            }else {
                SLIDER_HEIGHT
            }
        }
    var min: Float = 0f
    var max: Float = 1f
    var progress: Float = 0f
    var value: Float
        get() = Math.lerp(min, max, progress)
        set(value) { progress  = ((value - min) / (max - min)).coerceIn(0f, 1f) }


    var onChangeCallback: ((value: Float, progress: Float) -> Unit)? = null
    var onFinishedCallback: ((value: Float, progress: Float) -> Unit)? = null

    var dragging = false

    init {
        height = NORMAL_HEIGHT
    }

    override fun close() {
        if (dragging) {
            dragging = false
            onFinishedCallback?.invoke(value,progress)
        }
        super.close()
    }

    override fun render(mouseX: Float, mouseY: Float, delta: Float) {
        val bobberX = width*progress


        renderer.push()
        renderer.translate(x, y)
        if (showValue) {
            // TODO better formatting?
            renderer.textLine("%.2f".format(value), width, 0f, fontColor, FONT_SIZE, font, TextAlign.RIGHT_TOP)
        }
        label?.let {
            // TODO prevent label and value overlap?!
            renderer.textLine(it, 0f, 0f, fontColor, FONT_SIZE, font, TextAlign.LEFT_TOP)
        }


        if (showValue || label != null)
            renderer.translate(0f, OFFSET)
        // The slider progress
        Test.renderer.roundedRect(0f, (SLIDER_HEIGHT - SLIDER_BAR_HEIGHT) /2, width, SLIDER_BAR_HEIGHT, SLIDER_CORNER_RADIUS, backgroundColor)
        Test.renderer.roundedRect(0f, (SLIDER_HEIGHT - SLIDER_BAR_HEIGHT) /2, width*progress, SLIDER_BAR_HEIGHT, SLIDER_CORNER_RADIUS, color)
        // The knob indicating the progress
        Test.renderer.circle(bobberX, SLIDER_HEIGHT /2, KNOB_RADIUS, color)

        renderer.pop()

        if (dragging) {
            progress = ((mouseX - x) / width).coerceIn(0f ,1f)
            onChangeCallback?.invoke(value, progress)
        }
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if(button == GLFW.GLFW_MOUSE_BUTTON_1 && isMouseOverSlider(mouseX, mouseY)) {
            dragging = true
            return true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (dragging && button == GLFW.GLFW_MOUSE_BUTTON_1) {
            dragging = false
            onFinishedCallback?.invoke(value,progress)
            return true
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    private fun isMouseOverSlider(mouseX: Float, mouseY: Float): Boolean {
        return mouseX >= x && mouseX < x + width && mouseY >= y + OFFSET && mouseY < y + height
    }

    companion object {
        private const val SLIDER_BAR_HEIGHT = 2f
        private const val SLIDER_CORNER_RADIUS = SLIDER_BAR_HEIGHT / 2
        private const val KNOB_RADIUS = 2f

        private const val SLIDER_HEIGHT = 5f
        private const val NORMAL_HEIGHT = 15f
        private const val OFFSET = NORMAL_HEIGHT - SLIDER_HEIGHT
    }
}