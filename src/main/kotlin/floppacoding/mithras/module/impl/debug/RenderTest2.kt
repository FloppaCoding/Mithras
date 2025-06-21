package floppacoding.mithras.module.impl.debug

import floppacoding.aurora.core.Aurora
import floppacoding.mithras.events.RenderWorldOverlayEvent
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.utils.render.ImageManager
import meteordevelopment.orbit.EventHandler

object RenderTest2 : Module("RenderTest2", Category.MISC) {

    @EventHandler
    fun onRenderWorld(event: RenderWorldOverlayEvent) {
        Aurora.beginFrame()
        Aurora.image(ImageManager.ICON, 40f, 40f, 200f, 200f)
        Aurora.endFrame()
    }

}