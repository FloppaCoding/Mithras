package floppacoding.mithras.module.impl.debug

import floppacoding.mithras.events.RenderWorldOverlayEvent
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.utils.render.Renderer3D
import meteordevelopment.orbit.EventHandler
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import org.joml.Vector3f
import java.awt.Color

object DebugModule : Module(
    "Debug Module",
    category = Category.MISC,
) {
    @EventHandler
    fun onRender(event: RenderWorldOverlayEvent) {
        Renderer3D.drawBoxOutline(event.context, 114f, -51f, -51f, 115f, -50f, -50f, Color(0,255,0, 50), 20f, phase = true)

        Renderer3D.drawFilledBox(event.context, 114f, -49f, -51f, 115f, -48f, -50f, Color(255,0,0, 50), phase = true)

        Renderer3D.drawOutlinedFilledBox(event.context, 113.5f, -47.5f, -51.5f, 115f, -46f, -50f, Color(255,0,255, 50), Color(0,255,0,150), 4f)

        Renderer3D.drawLine(event.context, 113f, -40f, -50f, 115f, -48f, -55f, Color(75,255,250, 200), lineWidth = 7f)



        Renderer3D.drawCircle(event.context, 110f,-40f, -40f, 2f, Vector3f(0f,1f,0f),null, Color(200,200,200,100), lineWidth = 5f, segments = 400)
        Renderer3D.drawCircle(event.context, 110f,-40f, -45f, 2f, Vector3f(1f,0f,0f),Color(50,0,70,200), lineWidth = 5f)
        Renderer3D.drawCircle(event.context, 110f,-40f, -50f, 2f, Vector3f(0f,0f,1f),Color(0,70,200,100), lineWidth = 5f)
        Renderer3D.drawCircle(event.context, 110f,-40f, -55f, 2f, Vector3f(7f,1f,-3f),Color(255,255,70,100), Color(200,200,200,100), lineWidth = 5f, segments = 10)


        Renderer3D.drawEllipse(event.context, 120f,-40f, -40f, 2f, 1f, 0f, Vector3f(0f,1f,0f),Color(0,255,70,200), lineWidth = 2f)
        Renderer3D.drawEllipse(event.context, 120f,-40f, -45f, 2f, 1f, 0f, Vector3f(1f,0f,0f),Color(50,0,70,200), lineWidth = 2f)
        Renderer3D.drawEllipse(event.context, 120f,-40f, -50f, 2f, 1f, 0f, Vector3f(0f,0f,1f),Color(0,70,200,200), lineWidth = 2f)
        Renderer3D.drawEllipse(event.context, 120f,-40f, -55f, 2f, 1f, 0f, Vector3f(7f,1f,-3f),Color(255,255,70,200), lineWidth = 2f)

        Renderer3D.drawEllipse(event.context, 120f,-35f, -40f, 2f, 1f, 3.1f/4, Vector3f(0f,1f,0f),Color(0,255,70,200), lineWidth = 5f)
        Renderer3D.drawEllipse(event.context, 120f,-35f, -45f, 2f, 1f, 3.1f/4, Vector3f(1f,0f,0f),Color(50,0,70,200), lineWidth = 5f)
        Renderer3D.drawEllipse(event.context, 120f,-35f, -50f, 2f, 1f, 3.1f/4, Vector3f(0f,0f,1f),Color(0,70,200,200), lineWidth = 5f)
        Renderer3D.drawEllipse(event.context, 120f,-35f, -55f, 2f, 1f, 3.1f/4, Vector3f(7f,1f,-3f),Color(255,255,70,200), lineWidth = 5f)

        val pos = BlockPos(114, -45, -55)
        Renderer3D.drawBlockBoundingBox(event.context, pos, outlineColor = null, fillColor = Color(250, 50, 170, 100), lineWidth = 4f)

        Renderer3D.drawBox(event.context, Box(134.0, -58.0, -34.0, 135.0, -57.0, -33.0), fillColor = Color(100,255,70))
    }
}