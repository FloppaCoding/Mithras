package floppacoding.mithras.utils.render

import net.minecraft.client.gl.Framebuffer
import org.lwjgl.opengl.GL45.*

/**
 * # A Framebuffer for rendering antialiased sub frames.
 *
 * This framebuffer is designed to be used for offscreen rendering of antialiased 2D elements.
 * It does not store any depth information.
 *
 * The intended use is to first copy the current image from the main buffer (without antialiasing) to this buffer with
 * [useAndCopyFrom]. This also sets this Framebuffer as the current render target.
 * When rendering to this buffer geometry is multisampled with the specified [samples].
 * Once you are done rendering the antialiased scene you have to switch back to the main buffer with [copyBackTo].
 *
 * **NOTE:** When you release an instance of this buffer to the garbage collector you have to manually free the
 * underlying memory with [delete]. The garbage collector will not do that.
 *
 *
 * @author Aton
 */
class MSAAFrameBuffer(private val samples: Int, initialWidth: Int, initialHeight: Int) {

    private val fbo: Int = glGenFramebuffers()
    private var colorBuffer: Int = glGenRenderbuffers()

    private var bufferWidth = initialWidth
    private var bufferHeight = initialHeight

    init {
        glBindFramebuffer(GL_FRAMEBUFFER, fbo)
        glBindRenderbuffer(GL_RENDERBUFFER, colorBuffer)
        glRenderbufferStorageMultisample(GL_RENDERBUFFER, samples, GL_RGBA8, initialWidth, initialHeight)
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, colorBuffer)
        checkStatus()
        glBindRenderbuffer(GL_RENDERBUFFER, 0)
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    /**
     * Trows an exception if the Buffer is not valid.
     */
    private fun checkStatus() {
        val fboStatus = glCheckFramebufferStatus(GL_FRAMEBUFFER)
        if (fboStatus != GL_FRAMEBUFFER_COMPLETE) {
            when (fboStatus) {
                GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT -> {
                    throw RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT")
                }
                GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT -> {
                    throw RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT")
                }
                GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER -> {
                    throw RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER")
                }
                GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER -> {
                    throw RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER")
                }
                GL_FRAMEBUFFER_UNSUPPORTED -> {
                    throw RuntimeException("GL_FRAMEBUFFER_UNSUPPORTED")
                }
                GL_OUT_OF_MEMORY -> {
                    throw RuntimeException("GL_OUT_OF_MEMORY")
                }
                else -> {
                    throw RuntimeException("glCheckFramebufferStatus returned unknown status:$fboStatus")
                }
            }
        }
    }

    /**
     * If the desired size does not match the currently set size, delete the render buffer and create a new one with the
     * desired dimension.
     */
    private fun resize(width: Int, height: Int) {
        if (bufferWidth == width && bufferHeight == height) return
        bufferHeight = height; bufferWidth = width
        glBindFramebuffer(GL_FRAMEBUFFER, fbo)
        glDeleteRenderbuffers(colorBuffer)
        colorBuffer = glGenRenderbuffers()
        glBindRenderbuffer(GL_RENDERBUFFER, colorBuffer)
        glRenderbufferStorageMultisample(GL_RENDERBUFFER, samples, GL_RGBA8, width, height)
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, colorBuffer)
        checkStatus()
        glBindRenderbuffer(GL_RENDERBUFFER, 0)
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    /**
     * Frees the memory of the underlying buffers.
     * Must be invoked before this object is released to the garbage collector.
     */
    fun delete() {
        glDeleteRenderbuffers(colorBuffer)
        glDeleteFramebuffers(fbo)
    }

    /**
     * Starts using this Framebuffer as render target.
     * Copies the frame from [source] to this buffer.
     * If required the buffer is resized to match [source].
     */
    fun useAndCopyFrom(source: Framebuffer) {
        val width = source.textureWidth; val height = source.textureHeight
        resize(width, height)
        glBlitNamedFramebuffer(source.fbo, fbo, 0, 0, width, height, 0, 0, width, height, GL_COLOR_BUFFER_BIT, GL_LINEAR)
        glBindFramebuffer(GL_FRAMEBUFFER, fbo)
    }

    /**
     * Switches back to [target] as the render target.
     * Copies the frame from this buffer to [target].
     */
    fun copyBackTo(target: Framebuffer) {
        val width = bufferWidth; val height = bufferHeight
        glBlitNamedFramebuffer(fbo, target.fbo, 0, 0, width, height, 0, 0, width, height, GL_COLOR_BUFFER_BIT, GL_LINEAR)
        glBindFramebuffer(GL_FRAMEBUFFER, target.fbo)
    }
}