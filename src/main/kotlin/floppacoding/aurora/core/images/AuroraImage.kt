package floppacoding.aurora.core.images

import floppacoding.aurora.core.data.OffHeapMemoryConsumer
import floppacoding.aurora.core.data.ResourceLoader
import org.lwjgl.opengl.GL45.*
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryUtil
import java.io.IOException
import java.nio.ByteBuffer

/**
 * Creates Open GL textures from image files.
 *
 * @param imageBuffer The image data in a byte buffer. This should be the entire content of the image file. It will be
 * read by [STBImage.stbi_load_from_memory] and freed afterward through [MemoryUtil.memFree].
 * @param imageFlags the image flags. Any of:
 *
 * [GENERATE_MIPMAPS][Image.Flags.GENERATE_MIPMAPS],
 * [REPEAT_XX][Image.Flags.REPEAT_X],
 * [IMAGE_REPEAT_Y][Image.Flags.REPEAT_Y]
 * [FLIP_Y][Image.Flags.FLIP_Y],
 * [NEAREST][Image.Flags.NEAREST]
 *
 *
 * @author Aton
 */
open class AuroraImage
@Throws(IOException::class) internal constructor(imageBuffer: ByteBuffer, vararg imageFlags: Image.Flags) :
    Image, OffHeapMemoryConsumer() {
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
        val texture = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, texture)

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
        glPixelStorei(GL_UNPACK_ROW_LENGTH, w[0])
        glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0)
        glPixelStorei(GL_UNPACK_SKIP_ROWS, 0)

        if (imageFlags.contains(Image.Flags.GENERATE_MIPMAPS)) {
            glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP, GL_TRUE)
        }


        if (imageFlags.contains(Image.Flags.GENERATE_MIPMAPS)) {
            if (imageFlags.contains(Image.Flags.NEAREST)) {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST)
            } else {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
            }
        } else {
            if (imageFlags.contains(Image.Flags.NEAREST)) {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
            } else {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            }
        }

        if (imageFlags.contains(Image.Flags.NEAREST)) {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        } else {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        }

        if (imageFlags.contains(Image.Flags.REPEAT_X))
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        else
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)

        if (imageFlags.contains(Image.Flags.REPEAT_Y))
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        else
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)



        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w[0], h[0], 0, GL_RGBA, GL_UNSIGNED_BYTE, data)



        glPixelStorei(GL_UNPACK_ALIGNMENT, 4)
        glPixelStorei(GL_UNPACK_ROW_LENGTH, 0)
        MemoryUtil.memFree(data)
        MemoryUtil.memFree(imageBuffer)


        glID = texture
        width = w[0]
        height = h[0]
        flags = imageFlags.toList()

        this.addCleanables(
            registerTextureCleaner(glID)
        )
    }

    /**
     * @param path path to the resource. It looks like:
     *
     *      "/assets/mithras/gui/icon.png"
     *
     * @param imageFlags the image flags. Any of:
     *
     * [GENERATE_MIPMAPS][Image.Flags.GENERATE_MIPMAPS],
     * [REPEAT_XX][Image.Flags.REPEAT_X],
     * [IMAGE_REPEAT_Y][Image.Flags.REPEAT_Y]
     * [FLIP_Y][Image.Flags.FLIP_Y],
     * [NEAREST][Image.Flags.NEAREST]
     */
    @Throws(IOException::class)
    constructor(path: String, vararg imageFlags: Image.Flags) : this(ResourceLoader.resourceToByteBuffer(path), *imageFlags)
}
