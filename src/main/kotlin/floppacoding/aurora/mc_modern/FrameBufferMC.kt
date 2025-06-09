package floppacoding.aurora.mc_modern

import floppacoding.aurora.core.FrameBuffer
import net.minecraft.client.gl.Framebuffer

class FrameBufferMC(val buffer: Framebuffer) : FrameBuffer() {
    override val fbo: Int = 0 // TODO this may be very wrong. Maybe use GlStateManager.writeFbo
    override var width: Int
        get() = buffer.textureWidth
        set(value) {}
    override var height: Int
        get() = buffer.textureHeight
        set(value) {}
}