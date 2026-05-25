package floppacoding.aurora.mc_modern

import com.mojang.blaze3d.systems.RenderSystem
import floppacoding.aurora.core.FrameBuffer
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.gl.GlBackend
import net.minecraft.client.texture.GlTexture

@Suppress("UNUSED_PARAMETER")
class FrameBufferMC(val buffer: Framebuffer) : FrameBuffer() {
    override val fbo: Int
    override var width: Int
        get() = buffer.textureWidth
        set(value) {}
    override var height: Int
        get() = buffer.textureHeight
        set(value) {}

    init {
        val device = RenderSystem.getDevice() as GlBackend
        fbo = (buffer.colorAttachment as GlTexture).getOrCreateFramebuffer((device as GlBackend).bufferManager, buffer.depthAttachment)
    }
}