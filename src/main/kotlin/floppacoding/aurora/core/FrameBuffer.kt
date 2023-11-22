package floppacoding.aurora.core

open class FrameBuffer {
    open val fbo: Int = 0
    open var width: Int = 0
    open var height: Int = 0
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