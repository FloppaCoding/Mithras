package floppacoding.mithras.utils.render

/**
 * # 2D Font rendering library.
 *
 * @author Aton
 */
interface FontRender2D {
    val defaultFontHeight: Float
        get() = DEFAULT_FONT_HEIGHT
    val defaultFont: Font

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

    fun textField(text: String,
                  x: Float,
                  y: Float,
                  width: Float,
                  color: Int,
                  fontSize: Float = DEFAULT_FONT_HEIGHT,
                  radius: Float = 3f,
                  font: Font = defaultFont
    )

    companion object {
        /**
         * The font height that will be used for text rendering unless specified otherwise.
         */
        const val DEFAULT_FONT_HEIGHT: Float = 9f
    }
}