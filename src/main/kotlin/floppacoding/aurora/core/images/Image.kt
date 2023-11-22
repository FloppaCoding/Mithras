package floppacoding.aurora.core.images

interface Image {
    val id: Int
    val width: Int
    val height: Int
    val flags: List<Flags>


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
        REPEATX,

        /**
         * For coordinates outside the image, it will  be periodically repeated.
         * By default, the value at the edge is used.
         */
        REPEATY,

        /**
         * Flips the image vertically before rendering.
         *
         * **NOTE:** the flip has to be implemented whenever the image is rendered. Unlike the other Flags this does not
         * affect the created image.
         */
        FLIPY,

        /**
         * Sets the sampling to use nearest texels color. Use this for intentionally pixelated images.
         * By default, the color is linearly interpolated.
         */
        NEAREST,
    }
}