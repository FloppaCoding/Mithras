package floppacoding.aurora.core

import org.lwjgl.opengl.GL46.GL_FRAMEBUFFER
import org.lwjgl.opengl.GL46.glBindFramebuffer

open class FrameBuffer {
    open val fbo: Int = 0
    open var width: Int = 0
    open var height: Int = 0

    open fun use() {
        glBindFramebuffer(GL_FRAMEBUFFER, fbo)
    }
}

class ResizableFrameBufferReference(
    override val fbo: Int,
    initialWidth: Int,
    initialHeight: Int
) : FrameBuffer() {
    init {
        this.width = initialWidth
        this.height = initialHeight
    }
}

@Suppress("UNUSED_PARAMETER")
class FrameBufferReference(
    override val fbo: Int,
    private val widthGetter: () -> Int,
    private val heightGetter: () -> Int
) : FrameBuffer() {
    override var width: Int
        get() = widthGetter()
        set(value) {}

    override var height: Int
        get() = heightGetter()
        set(value) {}
}