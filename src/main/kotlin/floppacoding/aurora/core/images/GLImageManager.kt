package floppacoding.aurora.core.images

import floppacoding.mithras.Mithras
import floppacoding.mithras.mixin.PlayerSkinAccessor
import floppacoding.aurora.core.images.Image.Flags
import net.minecraft.client.texture.PlayerSkinTexture
import net.minecraft.util.Identifier
import org.apache.commons.io.IOUtils
import org.lwjgl.opengl.GL46.*
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryUtil
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files

// TODO somehow join this with NVGImageManager.
object GLImageManager {

    // TODO it might be better to combine these two classes into one and make the constructor private.
    // That way using the buffer is enforced.
    val ICON: GLImage = GLImage( "/assets/${Mithras.RESOURCE_DOMAIN}/gui/icon.png")
    val HUE_SCALE: GLImage = GLImage( "/assets/${Mithras.RESOURCE_DOMAIN}/gui/huescale.png")
    val CHROMA: GLImage = GLImage( "/assets/${Mithras.RESOURCE_DOMAIN}/gui/huescale20_lowres.png")

    //Dungeon Map
    val NEU_GREEN        : GLImage = GLImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/neu/green_check.png", Flags.NEAREST)
    val NEU_WHITE        : GLImage = GLImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/neu/white_check.png", Flags.NEAREST)
    val NEU_CROSS        : GLImage = GLImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/neu/cross.png", Flags.NEAREST)
    val NEU_QUESTION     : GLImage = GLImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/neu/question.png", Flags.NEAREST)
    val DEFAULT_GREEN    : GLImage = GLImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/default/green_check.png", Flags.NEAREST)
    val DEFAULT_WHITE    : GLImage = GLImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/default/white_check.png", Flags.NEAREST)
    val DEFAULT_CROSS    : GLImage = GLImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/default/cross.png", Flags.NEAREST)
    val DEFAULT_QUESTION : GLImage = GLImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/default/question.png", Flags.NEAREST)

    private val bufferedImages = mutableMapOf<Identifier, GLImage>()
    private val bufferedImages2 = mutableMapOf<String, GLImage>()

    /**
     * @param imageFlags the image flags. Any of:
     *
     * [GENERATE_MIPMAPS][Image.Flags.GENERATE_MIPMAPS],
     * [REPEATX][Image.Flags.REPEATX],
     * [IMAGE_REPEATY][Image.Flags.REPEATY]
     * [FLIPY][Image.Flags.FLIPY],
     * [NEAREST][Image.Flags.NEAREST]
     */
    @Throws(IOException::class)
    fun createImage(identifier: Identifier, vararg imageFlags: Flags): GLImage {
        val bufferedImage = bufferedImages[identifier]
        if (bufferedImage != null) return bufferedImage
        val newImage = GLImage(identifier, *imageFlags)
        bufferedImages[identifier] = newImage
        return newImage
    }

    @Throws(IOException::class)
    fun createImage(path: String, vararg imageFlags: Flags): GLImage {
        val bufferedImage = bufferedImages2[path]
        if (bufferedImage != null) return bufferedImage
        val newImage = GLImage(path, *imageFlags)
        bufferedImages2[path] = newImage
        return newImage
    }

    /**
     * Image for NanoVG.
     */
    class GLImage : Image {
        private val path: String

        /**
         * Image id according to NanoVG.
         */
        override val id: Int

        override val width: Int
        override val height: Int
        override val flags: List<Flags>

        /**
         * @param path path to the resource. It looks like:
         *
         *      "/assets/mithras/gui/icon.png"
         *
         * @param imageFlags the image flags. Any of:
         *
         * [GENERATE_MIPMAPS][Image.Flags.GENERATE_MIPMAPS],
         * [REPEATX][Image.Flags.REPEATX],
         * [IMAGE_REPEATY][Image.Flags.REPEATY]
         * [FLIPY][Image.Flags.FLIPY],
         * [NEAREST][Image.Flags.NEAREST]
         */
        @Throws(IOException::class)
        constructor(path: String, vararg imageFlags: Flags) {
            this.path = path
            this.flags = imageFlags.toList()
            val imageBuffer = resourceToByteBuffer(path)
            val tempData = createImage(imageBuffer, *imageFlags)
            id = tempData.id
            width = tempData.width
            height = tempData.height
        }

        /**
         * @param imageFlags the image flags. Any of:
         *
         * [GENERATE_MIPMAPS][Image.Flags.GENERATE_MIPMAPS],
         * [REPEATX][Image.Flags.REPEATX],
         * [IMAGE_REPEATY][Image.Flags.REPEATY]
         * [FLIPY][Image.Flags.FLIPY],
         * [NEAREST][Image.Flags.NEAREST]
         */
        @Throws(IOException::class)
        constructor(identifier: Identifier, vararg imageFlags: Flags) {
            this.path = identifier.path
            this.flags = imageFlags.toList()
            val imageBuffer = byteBufferFromIdentifier(identifier)
            val tempData = createImage(imageBuffer, *imageFlags)
            id = tempData.id
            width = tempData.width
            height = tempData.height
        }

        @Throws(IOException::class)
        private fun createImage(imageBuffer: ByteBuffer, vararg imageFlags: Flags): TemporaryImageData {
            val w = IntArray(1)
            val h = IntArray(1)
            val channels = IntArray(1)
            val data: ByteBuffer =  STBImage.stbi_load_from_memory(imageBuffer, w, h, channels, 4) ?: throw FileNotFoundException(path)
            val texture = glGenTextures()
            glBindTexture(GL_TEXTURE_2D, texture)

            glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
            glPixelStorei(GL_UNPACK_ROW_LENGTH, w[0] )
            glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0)
            glPixelStorei(GL_UNPACK_SKIP_ROWS, 0)

            if (imageFlags.contains(Flags.GENERATE_MIPMAPS)) {
                glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP, GL_TRUE)
            }


            if (imageFlags.contains(Flags.GENERATE_MIPMAPS)) {
                if (imageFlags.contains(Flags.NEAREST)) {
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST)
                } else {
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
                }
            } else {
                if (imageFlags.contains(Flags.NEAREST)) {
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
                } else {
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
                }
            }

            if (imageFlags.contains(Flags.NEAREST)) {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
            } else {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            }

            if (imageFlags.contains(Flags.REPEATX))
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
            else
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)

            if (imageFlags.contains(Flags.REPEATY))
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
            else
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)



            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w[0], h[0], 0, GL_RGBA, GL_UNSIGNED_BYTE, data)



            glPixelStorei(GL_UNPACK_ALIGNMENT, 4)
            glPixelStorei(GL_UNPACK_ROW_LENGTH, 0)
            MemoryUtil.memFree(data)
            MemoryUtil.memFree(imageBuffer)
            return TemporaryImageData(texture, w[0], h[0])
        }

        /**
         * Gets the resource from the identifier as a byte buffer.
         *
         * @throws FileNotFoundException when the resource could not be loaded.
         */
        @Throws(IOException::class)
        private fun byteBufferFromIdentifier(identifier: Identifier): ByteBuffer {
            val resource = Mithras.mc.resourceManager.getResource(identifier)
            val inputStream = if (resource.isPresent) {
                resource.get().inputStream
            }else { // try to get from skin cache
                val texture = Mithras.mc.textureManager.getTexture(identifier)
                val cacheFile = ((texture as? PlayerSkinTexture) as? PlayerSkinAccessor)?.cacheFile
                if (cacheFile != null) {
                    Files.newInputStream(cacheFile.toPath())
                } else throw FileNotFoundException(identifier.namespace +":"+identifier.path)
            }
            val bytes = IOUtils.toByteArray(inputStream)
            val data = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder())
                .put(bytes)
            (data as Buffer).flip()
            inputStream.close()
            return data
        }

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
            val stream = this.javaClass.getResourceAsStream(path) ?: throw FileNotFoundException(path)
            val bytes = IOUtils.toByteArray(stream)
            val data = MemoryUtil.memAlloc(bytes.size).put(0, bytes)
            stream.close()
            return data
        }

        private class TemporaryImageData(val id: Int, val width: Int, val height: Int)
    }
}