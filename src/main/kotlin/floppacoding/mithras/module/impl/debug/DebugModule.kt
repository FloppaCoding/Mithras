package floppacoding.mithras.module.impl.debug

import floppacoding.mithras.events.RenderWorldOverlayEvent
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.utils.render.WorldRenderer
import meteordevelopment.orbit.EventHandler
import net.minecraft.util.math.BlockPos
import java.awt.Color

object DebugModule : Module(
    "Debug Module",
    category = Category.MISC,
) {
    @EventHandler
    fun onRender(event: RenderWorldOverlayEvent) {
        WorldRenderer.drawBoxOutline(event.context, 114f, -51f, -51f, 115f, -50f, -50f, Color(0,255,0, 50), 2f, phase = true)

        WorldRenderer.drawFilledBox(event.context, 114f, -49f, -51f, 115f, -48f, -50f, Color(255,0,0, 50), phase = true)

        WorldRenderer.drawOutlinedFilledBox(event.context, 113.5f, -47.5f, -51.5f, 115f, -46f, -50f, Color(255,0,255, 50), Color(0,255,0,150), 4f)

        WorldRenderer.drawLine(event.context, 113f, -40f, -50f, 115f, -48f, -55f, Color(75,255,250, 200), lineWidth = 7f)

        val pos = BlockPos(114, -45, -55)
        WorldRenderer.drawBlockBoundingBox(event.context, pos, outlineColor = null, fillColor = Color(250, 50, 170, 100), lineWidth = 4f)
    }
}