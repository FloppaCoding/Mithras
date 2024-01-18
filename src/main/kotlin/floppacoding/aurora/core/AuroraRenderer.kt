package floppacoding.aurora.core

/**
 * Interface for implementation dependant methods.
 *
 * @author Aton
 */
interface AuroraRenderer : Renderer2D {

    val matrices: MatrixStack2D
    val vaoBuilder: VAOBuilder2D

    /**
     * Circles are approximated through polygons in this library. This method sets the maximum allowed
     * deviation of such a polygon from a true circle in pixels. The default value is 0.33.
     */
    fun setMaxDeviation(deviation: Float)

    /**
     * Determines whether to use Multi-Sample-Antialiasing (MSAA).
     * MSAA is enabled by default.
     */
    fun useMSAA(use: Boolean)

    /**
     * Sets the number of samples used for Multi-Sample-Antialiasing (MSAA).
     */
    fun setMSAASamples(samples: Int)

    /**
     * Adds the given [call] to the list of draw calls which will be executed on [endFrame].
     *
     * Only use this if you know what you are doing!
     *
     * Unless stated otherwise all draw methods will handle this internally.
     *
     * If the call is empty it may be skipped.
     */
    fun addDrawCall(call: RenderCall)

    /**
     * Returns the last added draw call or null if the list is empty.
     *
     * This method is very useful as it allows you to modify the coloring behavior of any element constructed by this
     * library.
     * The following example shows how you can use this to draw a chroma rectangle.
     *
     *      Aurora.rect(0f, 0f, 100f, 100f, -1)
     *      Aurora.getLastDrawCall()?.enableChroma()
     * This will tell Aurora to color the rectangle with the chroma effect. If you want it to still use the alpha value
     * passed with the color argument, you can chain [enableChroma][RenderCall.enableChroma] with
     * [enableAlpha][RenderCall.enableAlpha].
     */
    fun getLastDrawCall(): RenderCall?

    /**
     * Sets up the main framebuffer as well as getters for the window dimensions.
     * [fbo] is expected to be a reference to the FBO that the frame should be rendered to.
     * The getters for the window dimensions will be used to automatically set up the coordinate space.
     *
     * **It is crucial that these return the exact dimensions of the FBO [fbo] is referring to.**
     *
     * Example usage:
     *
     *      Aurora.setMainBufferReference(fbo, window::getWidth, window::getHeight)
     */
    fun setMainBufferReference(fbo: Int, widthGetter: () -> Int, heightGetter: () -> Int)

    /**
     * Sets the main framebuffer that will be rendered to through its open gl reference [fbo].
     * When this is used it is crucial that you also set the dimensions through [setDimensions].
     * These will have to be updated whenever the framebuffer [fbo] is resized.
     */
    fun setMainBufferId(fbo: Int)

    /**
     * It is not recommended to use this unless you know what you are doing.
     */
    fun setMainBuffer(buffer: FrameBuffer)

    /**
     * Sets up a scissor rectangle.
     *
     * The coordinates are assumed to be in the current coordinate space and are transformed accordingly.
     * The scissor rectangle will be aligned with the screen coordinate system and will be the bounding box of the given possibly
     * rotated rectangle. If the axis of the current coordinate system are not aligned with screen coordinates the scissor
     * will set up a rectangle *ABCD* as shown in the following example.
     *
     *         A      (x+width,y)  B
     *          ┌─────────────╳───┐
     *          │      __──‾‾  ╲  │
     *    (x,y) │__──‾‾          ╲│ (x+width,y+height)
     *          │╲          __──‾‾│
     *          │  ╲  __──‾‾      │
     *          └───╳─────────────┘
     *         D    (x,y+height)   C
     *
     *
     */
    override fun scissor(x: Float, y: Float, width: Float, height: Float)

    /**
     * Begins drawing custom geometry.
     */
    fun beginShape() {
        vaoBuilder.begin()
    }

    /**
     * Ends the construction of custom geometry and generates the corresponding render call.
     */
    fun endShape(shapeMode: VAOBuilder2D.Mode, colorMode: RenderCall.ColorMode) {
        val range = vaoBuilder.generateIndices(shapeMode)
        addDrawCall(RenderCall(range, colorMode))
    }

    /**
     * Specifies the next vertex.
     */
    fun vertex(x: Float, y: Float, color: Int = -1, u: Float = 0f, v: Float = 0f) {
        vaoBuilder.vertex(matrices.peek(), x, y).color(color).texture(u, v).next()
    }

    /**
     * Specifies the next vertex.
     *
     * The color values are expected to be in the range [0..255].
     *
     * The texture coordinates are clamped to the range [0..1].
     */
    fun vertex(x: Float, y: Float, red: Int = -1, green: Int = -1, blue: Int = -1, alpha: Int = -1, u: Float = 0f, v: Float = 0f) {
        vaoBuilder.vertex(matrices.peek(), x, y).color(red, green, blue, alpha).texture(u, v).next()
    }
}