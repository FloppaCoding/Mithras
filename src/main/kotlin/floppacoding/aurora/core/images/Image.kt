package floppacoding.aurora.core.images

/**
 * The base requirement for images to be able to be rendered by Aurora.
 *
 * @author Aton
 */
interface Image {
    /**
     * The name OpenGL assigned to the image.
     */
    val glID: Int

    /**
     * The image width in pixels.
     */
    val width: Int

    /**
     * The image height in pixels.
     */
    val height: Int

    /**
     * Creation flags for the image.
     */
    val flags: List<Flags>

    /**
     * A set of flags that control how the image is created.
     */
    enum class Flags{
        /**
         * Generates mipmaps.
         * Disabled by default.
         */
        GENERATE_MIPMAPS,
        /**
         * For coordinates outside the image, it will  be periodically repeated.
         * By default, the value at the edge is used.
         */
        REPEAT_X,

        /**
         * For coordinates outside the image, it will  be periodically repeated.
         * By default, the value at the edge is used.
         */
        REPEAT_Y,

        /**
         * Flips the image vertically before rendering.
         *
         * **NOTE:** the flip has to be implemented whenever the image is rendered. Unlike the other Flags this does not
         * affect the created image.
         */
        FLIP_Y,

        /**
         * Sets the sampling to use the nearest texels color. Use this for intentionally pixelated images.
         * By default, the color is linearly interpolated.
         */
        NEAREST,
    }
}