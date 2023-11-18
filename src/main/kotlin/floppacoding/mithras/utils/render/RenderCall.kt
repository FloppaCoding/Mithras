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
 * @param scissorBox The bounding box of the current scissor region in screen coordinates.
 * Poss null when not scissoring.
 *
 * @author Aton
 */
class RenderCall @JvmOverloads constructor(
    /**
     * The position of the indices for this draw call in the index buffer.
     */
    val indexRange: IntRange,
    colorMode: ColorMode,
    /**
     * Optionally the id of a required texture.
     */
    val texture: Int? = null,
    /**
     * Optionally size information for text antialiasing.
     */
    val textScale : Float? = null,
    var scissorBox: BoundingBox? = null,
) {
    /**
     * The mode by which the fragment shader is supposed to determine the fragments color.
     */
    var colorModeId = colorMode.id
        private set

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
        return colorModeId == next.colorModeId  // no color mode change
            && indexRange.last + 1 == next.indexRange.first // no gap in between index ranges.
            && (next.textureUnit == null || textureUnit == next.textureUnit) // no texture change.
            && (next.textScale == null || textScale == next.textScale ) // no font size change
    }

    /**
     * Changes the color mode for this call to the specified [mode].
     */
    fun setColorMode(mode: ColorMode): RenderCall {
        colorModeId = mode.id
        return this
    }

    /**
     * Changes the coloring for this call to chroma.
     * This does not affect any other attributes of the current ColorMode.
     */
    fun enableChroma(): RenderCall {
        colorModeId = (colorModeId and REMOVE_COLOR_MASK) + CHROMA_COLOR
        return this
    }

    /**
     * Disables the alpha attribute affecting the final color.
     * When the color attribute is used for coloring this will have no effect.
     */
    fun disableAlpha(): RenderCall {
        colorModeId = colorModeId and COLOR_ALPHA_BIT.inv()
        return this
    }

    /**
     * Enables the alpha attribute affecting the final color.
     * When the color attribute is used for coloring the final color will have its alpha squared.
     */
    fun enableAlpha(): RenderCall {
        colorModeId = colorModeId or COLOR_ALPHA_BIT
        return this
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
        private const val VERTEX_COLOR = 0 shl 8
        private const val TEXTURE_COLOR = 1 shl 8
        private const val CHROMA_COLOR = 2 shl 8

        private const val TEXT_BIT = 0b10_0000
        private const val COLOR_ALPHA_BIT = 0b1_0000

        private val REMOVE_COLOR_MASK: Int = 0xff_ff_f0_ffu.toInt()

    }
}