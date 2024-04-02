package floppacoding.ui

import floppacoding.mithras.Mithras
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class UIScreen(val ui: UI) : Screen(Text.literal("screen")) {

    override fun init() {
        ui.initialize()
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        ui.render()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        ui.onMouseClick(button)
        return true
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        ui.onRelease(button)
        return true
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        ui.onMouseMoved(Mithras.mc.mouse.x.toFloat(), Mithras.mc.mouse.y.toFloat())
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (ui.onKeyTyped(keyCode)) {
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return super.keyReleased(keyCode, scanCode, modifiers)
    }

    override fun shouldPause(): Boolean = false
}