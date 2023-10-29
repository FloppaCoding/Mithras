package floppacoding.mithras.utils.render.nanovg

import floppacoding.mithras.Mithras
import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.mixin.PlayerSkinAccessor
import floppacoding.mithras.utils.render.Image
import net.minecraft.client.texture.PlayerSkinTexture
import net.minecraft.util.Identifier
import org.apache.commons.io.IOUtils
import org.lwjgl.nanovg.NanoVG
import org.lwjgl.stb.STBImage
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files

typealias NVGImage = NVGImageManager.NVGImage

// TODO comment this class.
object NVGImageManager {
    // TODO it might be better to combine these two classes into one and make the constructor private.
    // That way using the buffer is enforced.
    val ICON: NVGImage = NVGImage( "/assets/${Mithras.RESOURCE_DOMAIN}/gui/icon.png")
    val HUE_SCALE: NVGImage = NVGImage( "/assets/${Mithras.RESOURCE_DOMAIN}/gui/huescale.png")
    val CHROMA: NVGImage = NVGImage( "/assets/${Mithras.RESOURCE_DOMAIN}/gui/huescale20_lowres.png")

    //Dungeon Map
    val NEU_GREEN        : NVGImage = NVGImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/neu/green_check.png", NanoVG.NVG_IMAGE_NEAREST)
    val NEU_WHITE        : NVGImage = NVGImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/neu/white_check.png", NanoVG.NVG_IMAGE_NEAREST)
    val NEU_CROSS        : NVGImage = NVGImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/neu/cross.png", NanoVG.NVG_IMAGE_NEAREST)
    val NEU_QUESTION     : NVGImage = NVGImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/neu/question.png", NanoVG.NVG_IMAGE_NEAREST)
    val DEFAULT_GREEN    : NVGImage = NVGImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/default/green_check.png", NanoVG.NVG_IMAGE_NEAREST)
    val DEFAULT_WHITE    : NVGImage = NVGImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/default/white_check.png", NanoVG.NVG_IMAGE_NEAREST)
    val DEFAULT_CROSS    : NVGImage = NVGImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/default/cross.png", NanoVG.NVG_IMAGE_NEAREST)
    val DEFAULT_QUESTION : NVGImage = NVGImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/default/question.png", NanoVG.NVG_IMAGE_NEAREST)

    private val bufferedImages = mutableMapOf<Identifier, NVGImage>()

    /**
     * @param imageFlags the image flags. One of:
     *
     * [IMAGE_GENERATE_MIPMAPS][NanoVG.NVG_IMAGE_GENERATE_MIPMAPS],
     * [IMAGE_REPEATX][NanoVG.NVG_IMAGE_REPEATX], [IMAGE_REPEATY][NanoVG.NVG_IMAGE_REPEATY]
     * [IMAGE_FLIPY][NanoVG.NVG_IMAGE_FLIPY], [IMAGE_PREMULTIPLIED][NanoVG.NVG_IMAGE_PREMULTIPLIED],
     * [IMAGE_NEAREST][NanoVG.NVG_IMAGE_NEAREST]
     */
    @Throws(IOException::class)
    fun createImage(identifier: Identifier, imageFlags: Int = 0): NVGImage {
        val bufferedImage = bufferedImages[identifier]
        if (bufferedImage != null) return bufferedImage
        val newImage = NVGImage(identifier,imageFlags)
        bufferedImages[identifier] = newImage
        return newImage
    }

    /**
     * Image for NanoVG.
     */
    class NVGImage : Image {
        private val path: String
        private val imageBuffer : ByteBuffer

        /**
         * Image id according to NanoVG.
         */
        val id: Int

        override val width: Int
        override val height: Int

        /**
         * @param path path to the resource. It looks like:
         *
         *      "/assets/mithras/gui/icon.png"
         *
         * @param imageFlags the image flags. One of:
         *
         * [IMAGE_GENERATE_MIPMAPS][NanoVG.NVG_IMAGE_GENERATE_MIPMAPS],
         * [IMAGE_REPEATX][NanoVG.NVG_IMAGE_REPEATX], [IMAGE_REPEATY][NanoVG.NVG_IMAGE_REPEATY]
         * [IMAGE_FLIPY][NanoVG.NVG_IMAGE_FLIPY], [IMAGE_PREMULTIPLIED][NanoVG.NVG_IMAGE_PREMULTIPLIED],
         * [IMAGE_NEAREST][NanoVG.NVG_IMAGE_NEAREST]
         */
        @Throws(IOException::class)
        constructor(path: String, imageFlags: Int = 0) {
            this.path = path
            this.imageBuffer = resourceToByteBuffer(path)
            val tempData = createImage(imageFlags)
            id = tempData.id
            width = tempData.width
            height = tempData.height
        }

        /**
         * @param imageFlags the image flags. One of:
         *
         * [IMAGE_GENERATE_MIPMAPS][NanoVG.NVG_IMAGE_GENERATE_MIPMAPS],
         * [IMAGE_REPEATX][NanoVG.NVG_IMAGE_REPEATX], [IMAGE_REPEATY][NanoVG.NVG_IMAGE_REPEATY]
         * [IMAGE_FLIPY][NanoVG.NVG_IMAGE_FLIPY], [IMAGE_PREMULTIPLIED][NanoVG.NVG_IMAGE_PREMULTIPLIED],
         * [IMAGE_NEAREST][NanoVG.NVG_IMAGE_NEAREST]
         */
        @Throws(IOException::class)
        constructor(identifier: Identifier, imageFlags: Int = 0) {
            this.path = identifier.path
            this.imageBuffer = byteBufferFromIdentifier(identifier)
            val tempData = createImage(imageFlags)
            id = tempData.id
            width = tempData.width
            height = tempData.height
        }

        @Throws(IOException::class)
        private fun createImage(imageFlags: Int): TemporaryImageData {
            val w = IntArray(1)
            val h = IntArray(1)
            val data: ByteBuffer =  STBImage.stbi_load_from_memory(
                imageBuffer,
                w,
                h,
                IntArray(1),
                4
            ) ?: throw FileNotFoundException(path)
            val id = NanoVG.nvgCreateImageRGBA(NVGR.nanoContext, w[0], h[0], imageFlags, data)
            return TemporaryImageData(id, w[0], h[0])
        }

        /**
         * Gets the resource from the identifier as a byte buffer.
         *
         * @throws FileNotFoundException when the resource could not be loaded.
         */
        @Throws(IOException::class)
        private fun byteBufferFromIdentifier(identifier: Identifier): ByteBuffer {
            val resource = mc.resourceManager.getResource(identifier)
            val inputStream = if (resource.isPresent) {
                resource.get().inputStream
            }else { // try to get from skin cache
                val texture = mc.textureManager.getTexture(identifier)
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
            val data = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder()).put(bytes)
            (data as Buffer).flip()
            stream.close()
            return data
        }

        private class TemporaryImageData(val id: Int, val width: Int, val height: Int)
    }
}