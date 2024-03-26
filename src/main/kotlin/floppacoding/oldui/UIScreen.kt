package floppacoding.oldui

import floppacoding.mithras.Mithras.mc
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

// UI Implementation for Minecraft screen ui
class UIScreen(val ui: UI, val scale: Float = 1f) : Screen(Text.literal("")) {

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        ui.render(scale)
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
        ui.onMouseMoved(mc.mouse.x.toFloat(), mc.mouse.y.toFloat())
    }

}