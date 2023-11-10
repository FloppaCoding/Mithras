package floppacoding.mithras.utils.render

import floppacoding.mithras.utils.render.nanovg.NVGR.beginFrame
import floppacoding.mithras.utils.render.nanovg.NVGR.endFrame
import net.minecraft.client.gui.DrawContext
import org.joml.Vector4f

// TODO add overloads for methods with default parameters for java compatibility

/**
 * # A library for antialiased 2D rendering.
 *
 * This library is meant for nice GUI and HUD rendering.
 *
 * ## Usage
 * All rendering related instructions from this library have to be wrapped in [beginFrame] and [endFrame].
 * The coordinate system has its origin in the top left corner of the screen with x going to the right and y towards the bottom.
 * The coordinates scale 1 to 1 to pixels on the screen.
 *
 * @author Aton
 */
interface Renderer2D {

    val defaultFontHeight: Float
        get() = DEFAULT_FONT_HEIGHT
    val defaultFont: Font


    /**
     * Begins drawing a new frame.
     *
     * This sets up the coordinate space such that the top left corner of the screen is the origin.
     * The coordinates of the bottom right corner of the screen are
     * [[mc.window.width][net.minecraft.client.util.Window.width], [mc.window.height][net.minecraft.client.util.Window.height]].
     *
     * All further rendering instructions have to be wrapped in [beginFrame] amd [endFrame].
     */
    fun beginFrame()

    /**
     * Begins drawing a new frame.
     *
     * Sets the current transformation based on the given [context].
     *
     * All further rendering instructions have to be wrapped in [beginFrame] amd [endFrame].
     * @see setTransform
     */
    fun beginFrame(context: DrawContext) {
        beginFrame()
        setTransform(context)
    }

    /**
     * Sets the current transform so that the coordinate system matches that of the given [context].
     * This also takes the current [GUI Scale][net.minecraft.client.util.Window.scaleFactor] into account and scales
     * the coordinate system accordingly. The GUI scale is not present in the [context]. In the vanilla rendering it gets
     * applied separately through the [ProjectionMatix][com.mojang.blaze3d.systems.RenderSystem.getProjectionMatrix] in
     * the vertex shader.
     *
     * After calling this method
     *
     *      rect(0f,0f,100f,50f,-1)
     * should produce the same result as
     *
     *      context.fill(0,0,100,50,-1)
     */
    fun setTransform(context: DrawContext)

    /**
     * Ends drawing the frame.
     *
     * All rendering instructions have to be wrapped in [beginFrame] amd [endFrame].
     */
    fun endFrame()

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
    fun translate(x: Double, y: Double)

    /**
     * Scales the current coordinate system.
     */
    fun scale(x: Float, y: Float)

    /**
     * Rotates by the given [angle] in degrees.
     */
    fun rotate(angle: Float)

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
     * Draws a line from point 1 to point 2.
     */
    fun line(x1: Float, y1: Float, x2: Float, y2: Float, width: Float, color: Int, capStyle: CapStyle = CapStyle.ROUND)

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
     * Renders text. This supports both line-breaks '\n' and a maximum line width.
     * The vertical alignment will always be done for the top line, with all further lines being positioned below it.
     * @param text The text to be rendered.
     * @param x Yhe text x-coordinate.
     * @param y Yhe text y-coordinate.
     * @param color The text color.
     * @param fontSize Height for the letters.
     * @param font Font to use.
     * @param textAlign The align type for the text.
     * @param splitWidth The width at which the test will be split into a new line. If this value is null, the text will
     * not be split. If this value is set, the alignment will be relative to a box from [x],[y] to [x]+[splitWidth],[y]+hieght.
     */
    fun text(
        text: CharSequence,
        x: Float,
        y: Float,
        color: Int,
        fontSize: Float = DEFAULT_FONT_HEIGHT,
        font: Font = defaultFont,
        textAlign: TextAlign = TextAlign.LEFT_TOP,
        splitWidth: Float? = null
    )

    /**
     * Renders a single line of text.
     * Does not split on line-breaks '\n'
     * @param text The text to be rendered.
     * @param x Yhe text x-coordinate.
     * @param y Yhe text y-coordinate.
     * @param color The text color.
     * @param fontSize Height for the letters.
     * @param font Font to use.
     * @param textAlign The align type for the text.
     */
    fun textLine(
        text: CharSequence,
        x: Float,
        y: Float,
        color: Int,
        fontSize: Float = DEFAULT_FONT_HEIGHT,
        font: Font = defaultFont,
        textAlign: TextAlign = TextAlign.LEFT_TOP,
    )

    /**
     * Renders text aligned with the left bottom corner to the given coordinates.
     * @param text The text to be rendered.
     * @param x Yhe text x-coordinate.
     * @param y Yhe text y-coordinate.
     * @param color The text color.
     * @param width The width of the box.
     * @param fontSize Height for the letters.
     * @param font Font to use.
     * @param textAlign The align type for the text.
     * @param boxAlign Determines the overall horizontal align. [TOP][TextAlign.Vertical.TOP] will result in all new
     * lines appearing below the first one. [BOTTOM][TextAlign.Vertical.BOTTOM] position the text, so that the last line
     * is aligned with [x],[y].
     * [MIDDLE][TextAlign.Vertical.MIDDLE] will result in the entire text box being centered vertically on [y].
     */
    fun textBox(
        text: CharSequence,
        x: Float,
        y: Float,
        color: Int,
        width: Float,
        fontSize: Float = DEFAULT_FONT_HEIGHT,
        font: Font = defaultFont,
        textAlign: TextAlign = TextAlign.LEFT_TOP,
        boxAlign: TextAlign = TextAlign.LEFT_TOP,
    )

    /**
     * Returns the width of the given single line of [text]. Line-breaks '\n' are not accounted for.
     */
    fun textWidth(text: CharSequence, fontSize: Float = DEFAULT_FONT_HEIGHT, font: Font = defaultFont): Float

    /**
     * Returns the bounding box of the given [text] if it were drawn at 0,0 in the current coordinate system.
     * @param width If width is null then the text will be considered as one line. Otherwise
     */
    fun textBounds(text: CharSequence, width: Float? = null, fontSize: Float = DEFAULT_FONT_HEIGHT, font: Font = defaultFont) : BoundingBox

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
     * @param color does nothing but gives this method the same signature as [border], so that both can be used with the
     * same syntax through a KFunction.
     */
    fun chromaBorder(x: Float, y: Float, width: Float, height: Float, lineWidth: Float, radius: Float, color: Int = 0)

    /**
     * Draws a border with rounded corner and the given dimensions.
     */
    fun border(x: Float, y: Float, width: Float, height: Float, lineWidth: Float, radius: Float, color: Int)

    fun textField(text: String,
                  x: Float,
                  y: Float,
                  width: Float,
                  color: Int,
                  fontSize: Float = DEFAULT_FONT_HEIGHT,
                  radius: Float = 3f,
                  font: Font = defaultFont
    )

    /**
     * Sets up a scissor rectangle.
     *
     */
    fun scissor(x: Float, y: Float, width: Float, height: Float)

    /**
     * Disables scissoring.
     */
    fun endScissor()

    companion object {
        /**
         * The font height that will be used for text rendering unless specified otherwise.
         */
        const val DEFAULT_FONT_HEIGHT: Float = 9f
    }
}

class BoundingBox(var xmin: Float, var ymin: Float, var xmax: Float, var ymax: Float) {
    fun width(): Float = xmax - xmin

    fun height() : Float = ymax - ymin
}

enum class CapStyle(val id: Int) {
    // The id matches NVG_BUTT / NVG_ROUND
    // NVG_SQUARE will draw an additional square at the ends of the line increasing the length of the line by 2*LineWidth.
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