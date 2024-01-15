package floppacoding.mithras.ui.hud

import floppacoding.mithras.ui.GuiScreen
import floppacoding.mithras.ui.core.GuiTools.addElements
import floppacoding.mithras.ui.core.elements.Slider
import floppacoding.mithras.ui.core.elements.ToggleButton
import floppacoding.mithras.utils.ChatUtils
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
        addElements(slider, toggleButton)
    }


    override fun render(mouseX: Float, mouseY: Float, delta: Float) {


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

    override val displayPerformance: Boolean = true
}