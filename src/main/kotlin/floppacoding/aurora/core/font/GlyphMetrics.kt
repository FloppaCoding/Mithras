package floppacoding.aurora.core.font

/**
 * Stores glyph specific spacing information, as well as the texture coordinates of the glyph.
 * ([u0],[v0]) is the top left corner of the glyph in the font atlas. This includes surrounding padding.
 * ([u1],[v1]) is the bottom right corner respectively.
 *
 * @author Aton
 */
class GlyphMetrics(
    /**
     * Left edge of the Glyph in the font atlas including padding.
     */
    val u0: Float,
    /**
     * Top edge of the Glyph in the font atlas including padding.
     */
    val v0: Float,
    /**
     * Right edge of the Glyph in the font atlas including padding.
     */
    val u1: Float,
    /**
     * Bottom edge of the Glyph in the font atlas including padding.
     */
    val v1: Float,
    /**
     * The total width of the glyph including padding.
     * The quad used for drawing the glyph with [u0], [u1], [v0], [v1], should have this width.
     */
    val width: Float,
    /**
     * The distance by which the origin should be advanced horizontally for the next glyph.
     */
    val advance: Float,
    /**
     * Horizontal shift of this glyph relative to the texture origin. This does not include the padding.
     */
    val leftSiderBearing: Float,
)