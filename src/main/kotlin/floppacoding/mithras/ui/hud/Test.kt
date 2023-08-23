package floppacoding.mithras.ui.hud

import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.ui.nanovg.NVGImageManager
import floppacoding.mithras.ui.nanovg.NVGR
import floppacoding.mithras.ui.nanovg.NVGScreen
import org.lwjgl.nanovg.NanoVG
import java.awt.Color

object Test : NVGScreen("Hello") {

    override fun render(mouseX: Float, mouseY: Float, delta: Float) {
        NVGR.roundedRect(100f, 500f, 200f, 200f, 8f, -1)
        NVGR.image(NVGImageManager.ICON, 100f, 500f, 200f, 200f)
        NVGR.chromaBorder(100f,500f,200f,200f, 5f, 0f)

        val text = "Floppa is best"
        val textWdith = NVGR.textWidth(text, 16f)
        NVGR.text(text, 100f, 100f, 0x654657a7, 16f)
        NVGR.rect(100f + textWdith, 50f, 100f, 100f, 0x65700491)

        NVGR.roundedRect(780f, 150f, 440f, 700f,8f, Color(28, 30, 32, 240).rgb)
        NVGR.textField("Enter  credit card number here!",800f, 200f, 400f, Color(255, 255, 255, 64).rgb, 20f)

        val skinResource = mc.networkHandler?.getPlayerListEntry(mc.player!!.uuid)?.skinTexture
        if (skinResource != null) {
            val skinImage = NVGImageManager.createImage(skinResource, NanoVG.NVG_IMAGE_NEAREST)

            NVGR.border(10f, 10f, 70f, 70f, 3.0f, 2f, Color(0, 0, 0, 255).rgb)
            NVGR.image(skinImage, 10f, 10f, 70f, 70f, 2f, 8f, 8f, 8f, 8f)
        }
    }

    override val displayPerformance: Boolean = true
}