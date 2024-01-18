package floppacoding.aurora.core

import floppacoding.aurora.core.data.OffHeapMemoryConsumer
import floppacoding.aurora.core.font.FontRender2D
import floppacoding.aurora.core.images.Image
import org.joml.Vector2f
import org.joml.Vector4f

/**
 * # A library for antialiased 2D rendering.
 *
 * This library is meant for nice GUI and HUD rendering.
 *
 * ## Usage
 * Before any rendering can be done the window size / coordinate system have to be set up first.
 * The method [setDimensions] is meant to take care of that.
 *
 * **NOTE**: Implementations may require a different setup, or additional steps.
 *
 * All rendering related instructions from this library have to be wrapped in [beginFrame] and [endFrame].
 *
 * For more details see [Aurora].
 *
 * ## Coordinate System
 * The coordinate system has its origin in the top left corner of the screen with x going to the right and y towards the bottom.
 * The coordinates scale 1 to 1 to pixels on the screen.
 *
 * @see Aurora
 * @author Aton
 */
interface Renderer2D : FontRender2D {

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
     */
    fun line(x0: Float, y0: Float, x1: Float, y1: Float, width: Float, color: Int, capStyle: CapStyle = CapStyle.ROUND)

    /**
     * Draws a rectangle with the given dimensions and color.
     */
    fun rect(x: Float, y: Float, width: Float, height: Float, color: Int)

    /**
     * Draws a rectangle with rounded corners.
     */
    fun roundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Int)

    /**
     * Draws a rectangle with rounded corners.
     */
    fun roundedRect(x: Float, y: Float, width: Float, height: Float, radii: Vector4f, color: Int)

    /**
     * Draws the [image] at [x],[y].
     * If [width] and [height] don't match the images aspect ratio, the image will get stretched accordingly.
     * @param radius radius of the corner radius.
     */
    fun roundedImage(image: Image, x: Float, y: Float, width: Float, height: Float, radius: Float, imageX: Float, imageY: Float, imageWidth: Float, imageHeight: Float, alpha: Float)

    /**
     * Draws the [image] at [x],[y].
     * If [width] and [height] don't match the images aspect ratio, the image will get stretched accordingly.
     * @param radius radius of the corner radius.
     */
    fun roundedImage(image: Image, x: Float, y: Float, width: Float, height: Float, radius: Float, imageX: Float, imageY: Float, imageWidth: Float, imageHeight: Float) {
        roundedImage(image, x, y, width, height, radius, imageX, imageY, imageWidth, imageHeight, 1f)
    }

    /**
     * Draws the [image] at [x],[y].
     * If [width] and [height] don't match the images aspect ratio, the image will get stretched accordingly.
     * @param radius radius of the corner radius.
     */
    fun roundedImage(image: Image, x: Float, y: Float, width: Float, height: Float, radius: Float, alpha: Float) {
        roundedImage(image, x, y, width, height, radius, 0f, 0f, image.width.toFloat(), image.height.toFloat(), alpha)
    }

    /**
     * Draws the [image] at [x],[y].
     * If [width] and [height] don't match the images aspect ratio, the image will get stretched accordingly.
     * @param radius radius of the corner radius.
     */
    fun roundedImage(image: Image, x: Float, y: Float, width: Float, height: Float, radius: Float) {
        roundedImage(image, x, y, width, height, radius, 1f)
    }


    /**
     * Draws the [image] at [x],[y].
     * If [width] and [height] don't match the images aspect ratio, the image will get stretched accordingly.
     */
    fun image(image: Image, x: Float, y: Float, width: Float, height: Float, imageX: Float, imageY: Float, imageWidth: Float, imageHeight: Float, alpha: Float){
        roundedImage(image, x, y, width, height, 0f, imageX, imageY, imageWidth, imageHeight, alpha)
    }

    /**
     * Draws the [image] at [x],[y].
     * If [width] and [height] don't match the images aspect ratio, the image will get stretched accordingly.
     */
    fun image(image: Image, x: Float, y: Float, width: Float, height: Float, imageX: Float, imageY: Float, imageWidth: Float, imageHeight: Float) {
        image(image, x, y, width, height, imageX, imageY, imageWidth, imageHeight, 1f)
    }

    /**
     * Draws the [image] at [x],[y].
     * If [width] and [height] don't match the images aspect ratio, the image will get stretched accordingly.
     */
    fun image(image: Image, x: Float, y: Float, width: Float, height: Float, alpha: Float) {
        image(image, x, y, width, height, 0f, 0f, image.width.toFloat(), image.height.toFloat(), alpha)
    }

    /**
     * Draws the [image] at [x],[y].
     * If [width] and [height] don't match the images aspect ratio, the image will get stretched accordingly.
     */
    fun image(image: Image, x: Float, y: Float, width: Float, height: Float) {
        image(image, x, y, width, height, 1f)
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
    fun chromaBorder(x: Float, y: Float, width: Float, height: Float, lineWidth: Float, radius: Float, color: Int = 0)

    /**
     * Draws a rectangle border with rounded corners.
     *
     * [width] and [height] are expected to be >= 0.
     * @param lineWidth The border extends half of this in both directions from the specified rectangle. This width is
     * affected by the coordinate transform and not a fixed pixel width on the screen.
     * The lineWidth is expected not to exceed the width or height of the rectangle.
     */
    fun border(x: Float, y: Float, width: Float, height: Float, lineWidth: Float, radius: Float, color: Int) {
        border(x, y, width, height, lineWidth, Vector4f(radius), color)
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
    fun border(x: Float, y: Float, width: Float, height: Float, lineWidth: Float, radii: Vector4f?, color: Int)

    /**
     * Draws a rectangle border with square corners.
     *
     * [width] and [height] are expected to be >= 0.
     * @param lineWidth The border extends half of this in both directions from the specified rectangle. This width is
     * affected by the coordinate transform and not a fixed pixel width on the screen.
     * The lineWidth is expected not to exceed the width or height of the rectangle.
     */
    fun border(x: Float, y: Float, width: Float, height: Float, lineWidth: Float, color: Int) {
        border(x, y, width, height, lineWidth, null, color)
    }

    /**
     * Draws a circle centered at the specified location [[x],[y]] with the given [radius] and [color].
     * The radius is in local coordinates, so if the local coordinate system is not evenly scaled this will draw an
     * ellipse.
     */
    fun circle(x: Float, y: Float, radius: Float, color: Int)

    /**
     * Draws an ellipse centered at [[x],[y]] with semi-axes [a] and [b] and the given [color].
     *
     * @param a Is one of the semi-axes of the ellipse and determines the orientation of the ellipse.
     * @param b Is the length of the other semi-axis of the ellipse. It is oriented internally.
     */
    fun ellipse(x: Float, y: Float, a: Vector2f, b: Float, color: Int)

    /**
     * Sets up a scissor rectangle.
     *
     */
    fun scissor(x: Float, y: Float, width: Float, height: Float)

    /**
     * Disables scissoring.
     */
    fun endScissor()
}

class BoundingBox(var xmin: Float, var ymin: Float, var xmax: Float, var ymax: Float) {
    fun width(): Float = xmax - xmin
    fun height() : Float = ymax - ymin
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