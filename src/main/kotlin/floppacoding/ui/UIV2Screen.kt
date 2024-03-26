package floppacoding.ui

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class UIV2Screen(val uiV2: UIV2) : Screen(Text.literal("screen")) {

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        uiV2.render()
    }
}