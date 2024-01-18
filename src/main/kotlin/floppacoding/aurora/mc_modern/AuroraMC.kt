package floppacoding.aurora.mc_modern

import floppacoding.aurora.core.Aurora
import floppacoding.aurora.core.AuroraRenderer
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext

object AuroraMC : Renderer2DMC, AuroraRenderer by Aurora {
    private val mc = MinecraftClient.getInstance()

    init {
        Aurora.setMainBuffer(FrameBufferMC(mc.framebuffer))
    }

    override fun beginFrame(context: DrawContext) {
        Aurora.beginFrame()
        setTransform(context)
    }

    override fun setTransform(context: DrawContext) {
        Aurora.matrices.loadIdentity()
        Aurora.matrices.scale(mc.window.scaleFactor.toFloat(), mc.window.scaleFactor.toFloat())
        val pm = context.matrices.peek().positionMatrix
        Aurora.matrices.peek().set(pm.m00(), pm.m01(), pm.m10(), pm.m11(), pm.m30(), pm.m31())
    }

    override fun setDimensions(width: Int, height: Int) {
        super.setDimensions(width, height)
    }
}