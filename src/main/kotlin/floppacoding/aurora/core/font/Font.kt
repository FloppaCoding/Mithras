package floppacoding.aurora.core.font

interface Font {
    /**
     * The name OpenGL assigned to the font atlas.
     */
    val glID: Int

    /**
     * Font wide metrics. This includes the vertical spacing information.
     * @see glyphMetrics
     */
    val fontMetrics: FontMetrics

    /**
     * Glyph specific metrics. This contains horizontal metrics as well as coordinates for the font atlas.
     */
    val glyphMetrics: Map<Char, GlyphMetrics>
}