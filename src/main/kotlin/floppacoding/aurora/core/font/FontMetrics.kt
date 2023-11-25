package floppacoding.aurora.core.font

/**
 * Stores general font size information. Those are mainly vertical distances.
 *
 * @author Aton
 */
class FontMetrics(
    /**
     * The ascent of tall 'normal' letters above the baseline.
     */
    val normalAscent: Float,
    /**
     * The maximum descent of 'normal' letters below the baseline.
     */
    val normalDescent: Float,
    /**
     * How far the tallest letters ascent above the baseline. This is positive.
     */
    val ascent: Float,
    /**
     * How far lowest glyph in the font extends below the baseline. This is negative.
     */
    val descent: Float,
    /**
     * Gap between lines as required by the font.
     * This may be 0.
     */
    val lineGap: Float,
    /**
     * The size of the padding around glyphs.
     */
    val padding: Int,
) {
    /**
     * The size of 'normal' underlying glyphs.
     * This is determined by the highest point of the letter 'f' and the lowest point of the letter 'g'.
     * It is what should be used for scaling the font.
     * Accents and special characters may very well exceed this.
     * @see totalHeight
     */
    val normalHeight: Float = normalAscent - normalDescent
    /**
     * The total height of the font.
     * This is defined as the distance between the highest and lowest point reachable by any glyph in this font.
     * @see normalHeight
     */
    val totalHeight = ascent - descent
    /**
     * Sum of [totalHeight] and [lineGap]
     */
    val lineHeight = ascent - descent + lineGap
    /**
     * Difference between [ascent] and [normalAscent].
     * This is >= 0.
     */
    val topOffset: Float = ascent - normalAscent
    /**
     * Difference between [descent] and [normalDescent].
     * This is <= 0.
     */
    val bottomOffset: Float = descent - normalDescent
}