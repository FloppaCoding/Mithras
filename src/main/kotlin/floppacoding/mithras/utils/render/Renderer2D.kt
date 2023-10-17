package floppacoding.mithras.utils.render

interface Renderer2D {

    val defaultFontHeight: Float
        get() = DEFAULT_FONT_HEIGHT
    val defaultFont: Font


    /**
     * Begins drawing a new frame.
     *
     * All further rendering instructions have to be wrapped in [beginFrame] amd [endFrame].
     */
    fun beginFrame()


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
     * Renders text aligned with the left bottom corner to the given coordinates.
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
        text: String,
        x: Float,
        y: Float,
        color: Int,
        fontSize: Float = DEFAULT_FONT_HEIGHT,
        font: Font = defaultFont,
        textAlign: TextAlign = TextAlign.TOP_LEFT,
        splitWidth: Float? = null
    )

    /**
     * Returns the width of the given [text].
     */
    fun textWidth(text: String, fontSize: Float = DEFAULT_FONT_HEIGHT, font: Font = defaultFont): Float

    /**
     * Returns the bounding box of the given [text] if it were drawn at 0,0 in the current coordinate system.
     * @param width If width is null then the text will be considered as one line. Otherwise
     */
    fun textBounds(text: String, width: Float? = null, fontSize: Float = DEFAULT_FONT_HEIGHT, font: Font = defaultFont) : BoundingBox

    // TODO split this in two methods maybe.
    // one for resized images and one for just full res
    /**
     * Draws the [image] at [x],[y].
     * If [width] and [height] don't match the images aspect ratio, the image will get stretched accordingly.
     * @param radius radius of the corner radius.
     */
    fun image(image: Image, x: Float, y: Float, width: Float, height: Float, radius: Float = 0f, imageX: Float = 0f, imageY: Float = 0f, imageWidth: Float = image.width.toFloat(), imageHeight: Float = image.height.toFloat(), alpha: Float = 1f)

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


    /**
     * Fills the current path with the given color.
     */
    fun fillWithColor(color: Int)

    /**
     * Strokes the current path with the given color.
     */
    fun strokeWithColor(width: Float, color: Int)

    /**
     * Fills the current path with the chroma pattern
     */
    fun fillWithChroma()

    /**
     * Strokes the current path with the chroma pattern.
     */
    fun strokeWithChroma(lineWidth: Float)

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

enum class CapStyle {
    ROUND,
    SQUARE;
}

enum class TextAlign {
    TOP_LEFT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    TOP_RIGHT,
    CENTER_BOTTOM,
    CENTER_MIDDLE,
    CENTER_TOP,
    LEFT_MIDDLE,
    RIGHT_MIDDLE,
    LEFT,
    RIGHT,
    MIDDLE;
}