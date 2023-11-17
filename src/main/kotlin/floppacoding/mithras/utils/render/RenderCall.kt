package floppacoding.mithras.utils.render

/**
 * Contains all state information for a rendering call.
 *
 * @param indexRange Range of positions of the indices corresponding to this call in the index buffer.
 * This will usually be the returned range from [VAOBuilder2D.generateIndices].
 * @param colorMode Determines how the defined geometry will be colored. See [ColorMode].
 * @param texture When drawing a texture or text this is the reference to the corresponding Open GL texture or font-atlas.
 * See [GLImageManager.GLImage.id] or [GLFontManager.GLFont.id]. Pass null when no texture is required.
 * @param textScale The current absolute scale factor of the local coordinate system in use when drawing the text.
 * See [GLR.getScale]. Pass null when not drawing text.
 *
 * @author Aton
 */
class RenderCall(
        /**
     * The position of the indices for this draw call in the index buffer.
     */
    val indexRange: IntRange,
        /**
     * The mode by which the fragment shader is supposed to color the fragments color.
     */
    val colorMode: ColorMode,
        /**
     * Optionally the id of a required texture.
     */
    val texture: Int? = null,
    /**
     * Optionally size information for text antialiasing.
     */
    val textAAWidth : Float? = null,
) {
    /**
     * The unit to which the texture for this call is bound.
     * This is set later when the render calls are dispatched.
     */
    var textureUnit: Int? = null

    /**
     * Returns whether the next render call can be combined with the current one.
     * This is the case when the index ranges are back to back and no state/uniform changes have to be made
     */
    fun combinable(next: RenderCall) : Boolean{
        return colorMode == next.colorMode  // no color mode change
            && indexRange.last + 1 == next.indexRange.first // no gap in between index ranges.
            && (next.textureUnit == null || textureUnit == next.textureUnit) // no texture change.
            && (next.textAAWidth == null || textAAWidth == next.textAAWidth ) // no font size change
    }

    /**
     * The mode by which the fragment shader is supposed to color the fragments color.
     */
    enum class ColorMode(val id: Int) {
        /**
         * Color by the vertex color attribute.
         */
        COLOR(0),

        /**
         * Sample the currently bound texture to determine the color.
         */
        TEXTURE(TEXTURE_COLOR),

        /**
         * Same as [TEXTURE] but multiplies with the vertex colors alpha.
         */
        TEXTURE_ALPHA(TEXTURE_COLOR or COLOR_ALPHA_BIT),

        /**
         * Interpret the currently bound texture as an SDF font atlas and use that together with the vertex color to
         * color the fragment.
         */
        TEXT(VERTEX_COLOR or TEXT_BIT),

        /**
         * Color with a global chroma effect.
         */
        CHROMA(CHROMA_COLOR),

        /**
         * Like [CHROMA] but uses the alpha value of the vertex color.
         */
        CHROMA_ALPHA(CHROMA_COLOR or COLOR_ALPHA_BIT),

        /**
         * Like [TEXT] but uses the chroma effect to determine the color.
         * Does not use the alpha value of the vertex color.
         */
        CHROMA_TEXT(CHROMA_COLOR or TEXT_BIT),

        /**
         * Like [TEXT] but uses the chroma effect to determine the color.
         * Uses the alpha value of the vertex color.
         */
        CHROMA_TEXT_ALPHA(CHROMA_COLOR or TEXT_BIT or COLOR_ALPHA_BIT);
    }

    companion object {
        /**
         * This value determines the width of the antialiasing.
         * It should be proportional to the derivative dSDF / dr of the SDF with respect to the distance in texels.
         * That makes it inversely proportional to the padding used for the SDF glyphs.
         * So if changes are made to that this value has to be adjusted accordingly.
         */
        private const val SCALE_FACTOR = 0.18f

        private const val VERTEX_COLOR = 0 shl 8
        private const val TEXTURE_COLOR = 1 shl 8
        private const val CHROMA_COLOR = 2 shl 8

        private const val TEXT_BIT = 0b10_0000
        private const val COLOR_ALPHA_BIT = 0b1_0000
    }
}