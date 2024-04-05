package floppacoding.ui

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class UIScreen(val ui: UI) : Screen(Text.literal("UI Screen")) {

    private val mc = MinecraftClient.getInstance()

    private var previousWidth: Int = 0
    private var previousHeight: Int = 0

    override fun init() {
        ui.initialize()
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        // doing this for resizing is much better since it guarantees it updates
        val w = mc.framebuffer.viewportWidth
        val h = mc.framebuffer.viewportHeight
        if (w != previousWidth || h != previousHeight) {
            ui.resize(w, h)
            previousWidth = w
            previousHeight = h
        }
        ui.render()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        ui.eventManager?.onMouseClick(button)
        return true
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        ui.eventManager?.onMouseRelease(button)
        return true
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        ui.eventManager?.onMouseMove(mc.mouse.x.toFloat(), mc.mouse.y.toFloat())
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
        ui.eventManager?.onMouseScroll(amount.toFloat())
        return true
    }

    override fun charTyped(chr: Char, modifiers: Int): Boolean {
        return ui.eventManager?.onKeyType(chr) == true
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (ui.eventManager?.onKeycodePressed(keyCode) == true) { // this is here so pressing esc works
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (ui.eventManager?.onKeyReleased(keyCode) == true) {
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

//    override fun resize(client: MinecraftClient, width: Int, height: Int) {
//        ui.resize(client.framebuffer.viewportWidth, client.framebuffer.viewportHeight)
//        super.resize(client, width, height)
//    }

    override fun shouldPause(): Boolean = false
}