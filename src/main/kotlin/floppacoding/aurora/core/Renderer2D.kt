package floppacoding.aurora.core

import floppacoding.aurora.core.data.OffHeapMemoryConsumer
import floppacoding.aurora.core.font.FontRender2D
import floppacoding.aurora.core.images.Image
import org.joml.Vector2f
import org.joml.Vector4f

/**
 * # A library for antialiased 2D rendering.
 *
 * This library is designed for efficient and good-looking GUI and HUD rendering.
 * It is meant to be used as a (secondary) rendering system in your LWJGL project.
 * A window needs to be already set up.
 *
 * ## Setup
 * For the rendering to work correctly Aurora needs to know the size of the window it is rendering to.
 * Unless antialiasing is explicitly disabled Aurora also needs to know which framebuffer
 * to render to. Refer to the Antialiasing section for more information.
 * By default, Aurora will use VGA resolution and the default framebuffer.
 *
 * The recommended way to set all of that up is through [setMainBufferReference].
 * That method takes a reference to the desired FBO as well as function handles for retrieving the current window size.
 * By setting it up this way the window dimensions don't have to be updated manually to Aurora.
 *
 * Alternatively [setMainBuffer] or [setMainBufferId] together with [setDimensions] can be used.
 * Check the documentation of the individual methods for more information.
 *
 *
 * ## Usage
 * All rendering calls have to be wrapped in [beginFrame] and [endFrame] calls.
 * After the call to [beginFrame] the desired rendering calls can be set up. They will all be buffered and only drawn
 * once [endFrame] is called. Alternatively a frame can be cancelled through [cancelFrame].
 *
 * For common objects like colored or textured rectangles, etc. a collection of methods are provided to generate those.
 *
 * While those should suffice for most applications, more specific geometry or coloring effects might be required.
 * The following subsections cover ways to expand the basic capabilities of this library.
 *
 * ### Custom geometry
 * Before you construct your geometry you must begin drawing with [beginShape].
 * An arbitrary geometry can then be constructed by specifying the vertices of the geometry  with [vertex].
 * Once you are done you can specify how you want the shape to be constructed from the vertices and colored though
 * [endShape].
 *
 * If you need finer control you can also directly use [vaoBuilder] and [matrices].
 *
 * Check out [VAOBuilder2D] for more information.
 *
 *
 * ### Special effects
 * You can also modify the way how a shape is colored. To do so modify the corresponding [RenderCall].
 * Those are returned by the geometry generating functions or can be obtained by [getLastDrawCall].
 * Refer to [RenderCall] for information on coloring options.
 *
 * Some of the coloring methods may require a certain parametrization of the shape through the texture coordinates.
 * Unless stated otherwise geometry constructing methods in the library parametrize the shape in regard to its bounding
 * box. This is depicted in the following example.
 *
 *        (0,0)              (1,0)   ┌─> u
 *          ┌────╳────────╳───┐      ↓ v
 *          │   / ╲__──‾‾  ╲  │
 *          │__/             ╲│
 *          │╲          __──‾‾│
 *          │  ╲  __──‾‾      │
 *          └───╳─────────────┘
 *        (0,1)              (1,1)
 *
 * Images are similarly parametrized, however the texture coordinates do not necessarily range from 0 to 1.
 *
 * Borders are parametrized such that u is 0 on the inner edge and 1 on the outer edge.
 *
 *
 * ## Coordinate System
 * The coordinate system has its origin in the top left corner of the screen with x going to the right and y towards the
 * bottom. The coordinates scale 1 to 1 to the pixels in your main framebuffer which should match pixels on the screen.
 *
 *
 * ## Antialiasing
 * The Antialiasing technique used by Aurora is
 * [Multi-Sample-Antialiasing (MSAA)](https://www.khronos.org/opengl/wiki/Multisampling).
 * It is enabled and set to 8 samples per pixel by default.
 * Both can be changed through [useMSAA] and [setMSAASamples] respectively.
 *
 * To allow for multisampling a framebuffer with the desired samples is required. Aurora employs its own framebuffer
 * for this, so that it can be used in any setting with an arbitrary amount of samples in the framebuffer.
 * For this to work as expected Aurora first copies the current texture from the main framebuffer to its own.
 * Then the scene is rendered to that framebuffer and in the has to be copied back to the main framebuffer.
 * To do this Aurora needs to know which framebuffer read from and write to.
 *
 * Aurora also needs to know the dimensions of the main framebuffer, so that it can copy the texture correctly.
 * **It is therefore crucial to set up the window dimensions correctly**
 *
 * @see Aurora
 * @author Aton
 */
interface Renderer2D : FontRender2D {

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
    fun addDrawCall(call: RenderCall): RenderCall

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
     * Sets the screen dimensions for following frames.
     *
     * This defines the coordinate system. Coordinates within the screen will range from 0 to [width] / [height]
     * respectively.
     */
    fun setDimensions(width: Int, height: Int)

    /**
     * Begins drawing a new frame.
     *
     * This sets up the coordinate space such that the top left corner of the screen is the origin.
     * The coordinates of the bottom right corner of the screen are determined by [setDimensions].
     *
     * This also sets the dimensions of the
     *
     * All further rendering instructions have to be wrapped in [beginFrame] amd [endFrame].
     */
    fun beginFrame()

    /**
     * Ends drawing the frame.
     *
     * All rendering instructions have to be wrapped in [beginFrame] amd [endFrame].
     *
     * Implementation hint: The implementation should always include a call to super.endFrame().
     * This is required to run queued tasks on the render thread. In particular this is used for automatically freeing
     * GPU memory by the garbage collector.
     */
    fun endFrame() {
        OffHeapMemoryConsumer.replayCleanupQueue()
    }

    /**
     * Cancels the frame currently in construction.
     */
    fun cancelFrame()

    /**
     * Resets current render state to default values.
     */
    fun reset()

    /**
     * Translates the origin of the current coordinate system.
     */
    fun translate(x: Float, y: Float)

    /**
     * Translates the origin of the current coordinate system.
     */
    fun translate(x: Double, y: Double) = translate(x.toFloat(), y.toFloat())

    /**
     * Scales the current coordinate system.
     */
    fun scale(x: Float, y: Float)

    /**
     * Rotates by the given [angle] in degrees.
     */
    fun rotate(angle: Float)

    /**
     * Rotates clockwise by the given [angle] in radians.
     */
    fun rotateRadians(angle: Float)

    /**
     * Pushes the current rendering state to a stack.
     * [pop] must be used to restore that state.
     */
    fun push()

    /**
     * Restores the previous rendering state.
     */
    fun pop()

    /**
     * Draws a line from point 0 to point 1.
     *
     * This method does not generate any parametrization for the vertices.
     */
    fun line(x0: Float, y0: Float, x1: Float, y1: Float, width: Float, color: Int, capStyle: CapStyle = CapStyle.ROUND): RenderCall

    /**
     * Draws a rectangle with the given dimensions and color.
     */
    fun rect(x: Float, y: Float, width: Float, height: Float, color: Int): RenderCall

    /**
     * Draws a rectangle with rounded corners.
     */
    fun roundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Int): RenderCall

    /**
     * Draws a rectangle with rounded corners.
     */
    fun roundedRect(x: Float, y: Float, width: Float, height: Float, radii: Vector4f, color: Int): RenderCall

    /**
     * Draws the [image] at [x],[y].
     * If [width] and [height] don't match the images aspect ratio, the image will get stretched accordingly.
     * @param radius radius of the corner radius.
     */
    fun roundedImage(image: Image, x: Float, y: Float, width: Float, height: Float, radius: Float, imageX: Float, imageY: Float, imageWidth: Float, imageHeight: Float, alpha: Float): RenderCall

    /**
     * Draws the [image] at [x],[y].
     * If [width] and [height] don't match the images aspect ratio, the image will get stretched accordingly.
     * @param radius radius of the corner radius.
     */
    fun roundedImage(image: Image, x: Float, y: Float, width: Float, height: Float, radius: Float, imageX: Float, imageY: Float, imageWidth: Float, imageHeight: Float): RenderCall {
        return roundedImage(image, x, y, width, height, radius, imageX, imageY, imageWidth, imageHeight, 1f)
    }

    /**
     * Draws the [image] at [x],[y].
     * If [width] and [height] don't match the images aspect ratio, the image will get stretched accordingly.
     * @param radius radius of the corner radius.
     */
    fun roundedImage(image: Image, x: Float, y: Float, width: Float, height: Float, radius: Float, alpha: Float): RenderCall {
        return roundedImage(image, x, y, width, height, radius, 0f, 0f, image.width.toFloat(), image.height.toFloat(), alpha)
    }

    /**
     * Draws the [image] at [x],[y].
     * If [width] and [height] don't match the images aspect ratio, the image will get stretched accordingly.
     * @param radius radius of the corner radius.
     */
    fun roundedImage(image: Image, x: Float, y: Float, width: Float, height: Float, radius: Float): RenderCall {
        return roundedImage(image, x, y, width, height, radius, 1f)
    }


    /**
     * Draws the [image] at [x],[y].
     * If [width] and [height] don't match the images aspect ratio, the image will get stretched accordingly.
     */
    fun image(image: Image, x: Float, y: Float, width: Float, height: Float, imageX: Float, imageY: Float, imageWidth: Float, imageHeight: Float, alpha: Float): RenderCall {
        return roundedImage(image, x, y, width, height, 0f, imageX, imageY, imageWidth, imageHeight, alpha)
    }

    /**
     * Draws the [image] at [x],[y].
     * If [width] and [height] don't match the images aspect ratio, the image will get stretched accordingly.
     */
    fun image(image: Image, x: Float, y: Float, width: Float, height: Float, imageX: Float, imageY: Float, imageWidth: Float, imageHeight: Float): RenderCall {
        return image(image, x, y, width, height, imageX, imageY, imageWidth, imageHeight, 1f)
    }

    /**
     * Draws the [image] at [x],[y].
     * If [width] and [height] don't match the images aspect ratio, the image will get stretched accordingly.
     */
    fun image(image: Image, x: Float, y: Float, width: Float, height: Float, alpha: Float): RenderCall {
        return image(image, x, y, width, height, 0f, 0f, image.width.toFloat(), image.height.toFloat(), alpha)
    }

    /**
     * Draws the [image] at [x],[y].
     * If [width] and [height] don't match the images aspect ratio, the image will get stretched accordingly.
     */
    fun image(image: Image, x: Float, y: Float, width: Float, height: Float): RenderCall {
        return image(image, x, y, width, height, 1f)
    }

    /**
     * Draws a chroma border with rounded corner and the given dimensions.
     *
     * [width] and [height] are expected to be >= 0.
     * @param lineWidth The border extends half of this in both directions from the specified rectangle. This width is
     * affected by the coordinate transform and not a fixed pixel width on the screen.
     * The lineWidth is expected not to exceed the width or height of the rectangle.
     * @param color does nothing but gives this method the same signature as [border], so that both can be used with the
     * same syntax through a KFunction.
     */
    fun chromaBorder(x: Float, y: Float, width: Float, height: Float, lineWidth: Float, radius: Float, color: Int = 0): RenderCall

    /**
     * Draws a rectangle border with rounded corners.
     *
     * [width] and [height] are expected to be >= 0.
     * @param lineWidth The border extends half of this in both directions from the specified rectangle. This width is
     * affected by the coordinate transform and not a fixed pixel width on the screen.
     * The lineWidth is expected not to exceed the width or height of the rectangle.
     */
    fun border(x: Float, y: Float, width: Float, height: Float, lineWidth: Float, radius: Float, color: Int): RenderCall {
        return border(x, y, width, height, lineWidth, Vector4f(radius), color)
    }

    /**
     * Draws a rectangle border with rounded corners.
     *
     * [width] and [height] are expected to be >= 0.
     * @param lineWidth The border extends half of this in both directions from the specified rectangle. This width is
     * affected by the coordinate transform and not a fixed pixel width on the screen.
     * The lineWidth is expected not to exceed the width or height of the rectangle.
     * @param radii 4 individual corner radii in the order top-left, bottom-left, bottom-right, top-right.
     */
    fun border(x: Float, y: Float, width: Float, height: Float, lineWidth: Float, radii: Vector4f?, color: Int): RenderCall

    /**
     * Draws a rectangle border with square corners.
     *
     * [width] and [height] are expected to be >= 0.
     * @param lineWidth The border extends half of this in both directions from the specified rectangle. This width is
     * affected by the coordinate transform and not a fixed pixel width on the screen.
     * The lineWidth is expected not to exceed the width or height of the rectangle.
     */
    fun border(x: Float, y: Float, width: Float, height: Float, lineWidth: Float, color: Int): RenderCall {
        return border(x, y, width, height, lineWidth, null, color)
    }

    /**
     * Draws a circle centered at the specified location [[x],[y]] with the given [radius] and [color].
     * The radius is in local coordinates, so if the local coordinate system is not evenly scaled this will draw an
     * ellipse.
     */
    fun circle(x: Float, y: Float, radius: Float, color: Int): RenderCall

    /**
     * Draws an ellipse centered at [[x],[y]] with semi-axes [a] and [b] and the given [color].
     *
     * *Parametrization*: The ellipse is parametrized so that u is parallel to [a].
     *
     * @param a Is one of the semi-axes of the ellipse and determines the orientation of the ellipse.
     * @param b Is the length of the other semi-axis of the ellipse. It is oriented internally.
     */
    fun ellipse(x: Float, y: Float, a: Vector2f, b: Float, color: Int): RenderCall

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
    fun scissor(x: Float, y: Float, width: Float, height: Float)

    /**
     * Disables scissoring.
     */
    fun endScissor()

    /**
     * Disables scissoring.
     * Stores the scissor state internally to be resumed with [resumeScissor].
     */
    fun pauseScissor()

    /**
     * Resumes the previously paused scissor state.
     * @see pauseScissor
     */
    fun resumeScissor()

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

class BoundingBox(var xmin: Float, var ymin: Float, var xmax: Float, var ymax: Float) {
    fun width(): Float = xmax - xmin
    fun height() : Float = ymax - ymin

    fun shift(deltaX: Float, deltaY: Float): BoundingBox {
        xmin += deltaX
        ymin += deltaY
        xmax += deltaX
        ymax += deltaY
        return this
    }

    companion object {
        @JvmStatic
        fun ofDimensions(x0: Float, y0: Float, width: Float, height: Float): BoundingBox {
            return BoundingBox(x0, y0, x0+width, y0+height)
        }
    }
}

enum class CapStyle(val id: Int) {
    FLAT(0),
    ROUND(1);
}

enum class TextAlign(val vertical: Vertical, val horizontal: Horizontal) {
    LEFT_TOP(Vertical.TOP, Horizontal.LEFT),
    LEFT_BOTTOM(Vertical.BOTTOM, Horizontal.LEFT),
    RIGHT_BOTTOM(Vertical.BOTTOM, Horizontal.RIGHT),
    RIGHT_TOP(Vertical.TOP, Horizontal.RIGHT),
    CENTER_BOTTOM(Vertical.BOTTOM, Horizontal.CENTER),
    CENTER_MIDDLE(Vertical.MIDDLE, Horizontal.CENTER),
    CENTER_TOP(Vertical.TOP, Horizontal.CENTER),
    LEFT_MIDDLE(Vertical.MIDDLE, Horizontal.LEFT),
    RIGHT_MIDDLE(Vertical.MIDDLE, Horizontal.RIGHT),
    LEFT_BASELINE(Vertical.BASELINE, Horizontal.LEFT),
    CENTER_BASELINE(Vertical.BASELINE, Horizontal.CENTER),
    RIGHT_BASELINE(Vertical.BASELINE, Horizontal.RIGHT);


    enum class Vertical{TOP, MIDDLE, BOTTOM, BASELINE;}
    enum class Horizontal{LEFT, CENTER, RIGHT;}
}