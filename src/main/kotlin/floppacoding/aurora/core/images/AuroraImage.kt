package floppacoding.aurora.core.images

import org.apache.commons.io.IOUtils
import org.lwjgl.opengl.GL46
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryUtil
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.ByteBuffer

/**
 * Creates Open GL textures from image files.
 *
 * @author Aton
 */
open class AuroraImage
@Throws(IOException::class) internal constructor(imageBuffer: ByteBuffer, vararg imageFlags: Image.Flags) : Image {
    final override val glID: Int
    final override val width: Int
    final override val height: Int
    final override val flags: List<Image.Flags>

    init {
        val w = IntArray(1)
        val h = IntArray(1)
        val channels = IntArray(1)
        val data: ByteBuffer =  STBImage.stbi_load_from_memory(imageBuffer, w, h, channels, 4)
            ?: throw IllegalArgumentException("Could not load image data. This should not have happened.")
        val texture = GL46.glGenTextures()
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, texture)

        GL46.glPixelStorei(GL46.GL_UNPACK_ALIGNMENT, 1)
        GL46.glPixelStorei(GL46.GL_UNPACK_ROW_LENGTH, w[0])
        GL46.glPixelStorei(GL46.GL_UNPACK_SKIP_PIXELS, 0)
        GL46.glPixelStorei(GL46.GL_UNPACK_SKIP_ROWS, 0)

        if (imageFlags.contains(Image.Flags.GENERATE_MIPMAPS)) {
            GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_GENERATE_MIPMAP, GL46.GL_TRUE)
        }


        if (imageFlags.contains(Image.Flags.GENERATE_MIPMAPS)) {
            if (imageFlags.contains(Image.Flags.NEAREST)) {
                GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MIN_FILTER, GL46.GL_NEAREST_MIPMAP_NEAREST)
            } else {
                GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MIN_FILTER, GL46.GL_LINEAR_MIPMAP_LINEAR)
            }
        } else {
            if (imageFlags.contains(Image.Flags.NEAREST)) {
                GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MIN_FILTER, GL46.GL_NEAREST)
            } else {
                GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MIN_FILTER, GL46.GL_LINEAR)
            }
        }

        if (imageFlags.contains(Image.Flags.NEAREST)) {
            GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_NEAREST)
        } else {
            GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_LINEAR)
        }

        if (imageFlags.contains(Image.Flags.REPEAT_X))
            GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_S, GL46.GL_REPEAT)
        else
            GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_S, GL46.GL_CLAMP_TO_EDGE)

        if (imageFlags.contains(Image.Flags.REPEAT_Y))
            GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_T, GL46.GL_REPEAT)
        else
            GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_T, GL46.GL_CLAMP_TO_EDGE)



        GL46.glTexImage2D(GL46.GL_TEXTURE_2D, 0, GL46.GL_RGBA, w[0], h[0], 0, GL46.GL_RGBA, GL46.GL_UNSIGNED_BYTE, data)



        GL46.glPixelStorei(GL46.GL_UNPACK_ALIGNMENT, 4)
        GL46.glPixelStorei(GL46.GL_UNPACK_ROW_LENGTH, 0)
        MemoryUtil.memFree(data)
        MemoryUtil.memFree(imageBuffer)


        glID = texture
        width = w[0]
        height = h[0]
        flags = imageFlags.toList()
    }

    /**
     * @param path path to the resource. It looks like:
     *
     *      "/assets/mithras/gui/icon.png"
     *
     * @param imageFlags the image flags. Any of:
     *
     * [GENERATE_MIPMAPS][Image.Flags.GENERATE_MIPMAPS],
     * [REPEATX][Image.Flags.REPEAT_X],
     * [IMAGE_REPEATY][Image.Flags.REPEAT_Y]
     * [FLIPY][Image.Flags.FLIP_Y],
     * [NEAREST][Image.Flags.NEAREST]
     */
    @Throws(IOException::class)
    constructor(path: String, vararg imageFlags: Image.Flags) : this(resourceToByteBuffer(path), *imageFlags)

    companion object {
        /**
         * Loads the resource as a byte buffer for use with NanoVG.
         * For mod assets the path has to look like:
         *
         *      "/assets/mithras/gui/icon.png"
         *
         * @throws FileNotFoundException when the file does not exist.
         */
        @Throws(IOException::class)
        private fun resourceToByteBuffer(path: String): ByteBuffer {
            // This works for mod assets, otherwise "Files.newInputStream(file.toPath())" should be used.
            val stream = this::class.java.getResourceAsStream(path) ?: throw FileNotFoundException(path)
            val bytes = IOUtils.toByteArray(stream)
            val data = MemoryUtil.memAlloc(bytes.size).put(0, bytes)
            stream.close()
            return data
        }
    }
}
