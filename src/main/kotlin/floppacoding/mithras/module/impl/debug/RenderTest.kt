package floppacoding.mithras.module.impl.debug

import floppacoding.aurora.core.Aurora
import floppacoding.aurora.core.font.AuroraFontRenderer
import floppacoding.aurora.mc_modern.AuroraMC
import floppacoding.mithras.events.RenderWorldOverlayEvent
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import meteordevelopment.orbit.EventHandler

object RenderTest : Module("RenderTest", Category.MISC) {

    @EventHandler
    fun onRenderWorld(event: RenderWorldOverlayEvent) {
        Aurora.beginFrame()
        Aurora.rect(0f,0f,100f,100f,-1)
        AuroraFontRenderer.fontAtlas(AuroraMC.defaultFont,0f, 0f)
        Aurora.endFrame()
    }
}