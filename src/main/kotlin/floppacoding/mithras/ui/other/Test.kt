package floppacoding.mithras.ui.other

import floppacoding.mithras.ui.GuiScreen
import floppacoding.mithras.ui.core.GuiTools.addElements
import floppacoding.mithras.ui.core.elements.ColorPicker
import floppacoding.mithras.ui.core.elements.Slider
import floppacoding.mithras.ui.core.elements.ToggleButton
import floppacoding.mithras.utils.ChatUtils
import org.joml.Vector2f
import java.awt.Color

object Test : GuiScreen("Hello", 2f) {

    init {
        val slider = Slider().apply {
            x = 100f
            y = 20f
            width = 100f
            label = "Slider"
            onFinishedCallback = { value: Float, progress: Float ->
                ChatUtils.chatMessage("Progress: $progress")
            }
        }
        val toggleButton = ToggleButton().apply {
            x = 100f
            y = 40f
            width = 100f
            label = "Toggle"
            onToggleCallback = { value: Boolean ->
                ChatUtils.chatMessage("Toggled: $value")
            }
        }
        val colorPicker = ColorPicker().apply {
            x = 100f
            y = 60f
            width = 100f
            label = "Color Picker"
        }
        addElements(slider, toggleButton, colorPicker)
    }


    override fun render(mouseX: Float, mouseY: Float, delta: Float) {
        renderer.push()

        renderer.circle(100f, 250f, 50f, -1).setVerticalFade(Color(255,0,0).rgb, Color(0,255, 0, 50).rgb)

        renderer.ellipse(100f, 400f, Vector2f(50f, 50f),20f, -1).setHorizontalFade(Color(255,0,0).rgb, Color(0,255, 0, 50).rgb)

        renderer.roundedRect(400f, 100f, 100f, 200f, 20f, -1).setFourColorFade(Color(255,0,0).rgb, Color(0,255, 0, 50).rgb, Color(0,0, 255, 170).rgb, Color(255, 255, 0, 200).rgb)

        renderer.pop()

    }

    fun slider(x: Float, y: Float, width: Float, percentage: Float, color: Int) {
        val sliderHeight = 2f
        val sliderCornerRadius = sliderHeight / 2
        val sliderBobberRadius = 2f
        val bobberX = x + width*percentage
        val backgroundColor = Color(0,0,0,50)
        // The slider progress
        renderer.roundedRect(x, y-sliderHeight/2, width*percentage, sliderHeight, sliderCornerRadius, backgroundColor.rgb)
        renderer.roundedRect(x, y-sliderHeight/2, width*percentage, sliderHeight, sliderCornerRadius, color)
        // The bobber indicating the progress
        renderer.circle(bobberX, y, sliderBobberRadius, color)
    }

    override var displayPerformance: Boolean = true
}