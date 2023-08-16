package floppacoding.mithras.ui.hud

import floppacoding.mithras.ui.nanovg.NVGR
import floppacoding.mithras.ui.nanovg.NVGScreen

object Test : NVGScreen("Hello") {

    override fun render(mouseX: Double, mouseY: Double, delta: Float) {
//        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)
        NVGR.beginFrame()
        NVGR.roundedRect(100f, 100f, 200f, 200f, 8f, -1)
        NVGR.endFrame()
      //  GL11.glPopAttrib()
    }

    override val displayPerformance: Boolean = true
}