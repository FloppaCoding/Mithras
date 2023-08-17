package floppacoding.mithras.ui.hud

import floppacoding.mithras.ui.nanovg.NVGImageManager
import floppacoding.mithras.ui.nanovg.NVGR
import floppacoding.mithras.ui.nanovg.NVGScreen

object Test : NVGScreen("Hello") {

    override fun render(mouseX: Double, mouseY: Double, delta: Float) {
        NVGR.roundedRect(100f, 500f, 200f, 200f, 8f, -1)

        val text = "Floppa is best"
        val textWdith = NVGR.textWidth(text, 16f)
        NVGR.text(text, 100f, 100f, 16f, 0x654657a7)
        NVGR.rect(100f + textWdith, 50f, 100f, 100f, 0x65700491)


        val imgSize = 120f


        NVGR.image(NVGImageManager.ICON, windowWidth-150f, windowHeight - 150f, imgSize, imgSize)

    }

    override val displayPerformance: Boolean = true
}