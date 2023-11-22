package floppacoding.mithras.utils.render.nanovg

import floppacoding.mithras.Mithras
import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.mixin.PlayerSkinAccessor
import floppacoding.aurora.core.images.Image
import net.minecraft.client.texture.PlayerSkinTexture
import net.minecraft.util.Identifier
import org.apache.commons.io.IOUtils
import org.lwjgl.nanovg.NanoVG.*
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryUtil
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
    val NEU_GREEN        : NVGImage = NVGImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/neu/green_check.png", Image.Flags.NEAREST)
    val NEU_WHITE        : NVGImage = NVGImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/neu/white_check.png", Image.Flags.NEAREST)
    val NEU_CROSS        : NVGImage = NVGImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/neu/cross.png", Image.Flags.NEAREST)
    val NEU_QUESTION     : NVGImage = NVGImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/neu/question.png", Image.Flags.NEAREST)
    val DEFAULT_GREEN    : NVGImage = NVGImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/default/green_check.png", Image.Flags.NEAREST)
    val DEFAULT_WHITE    : NVGImage = NVGImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/default/white_check.png", Image.Flags.NEAREST)
    val DEFAULT_CROSS    : NVGImage = NVGImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/default/cross.png", Image.Flags.NEAREST)
    val DEFAULT_QUESTION : NVGImage = NVGImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/default/question.png", Image.Flags.NEAREST)

    private val bufferedImages = mutableMapOf<Identifier, NVGImage>()
    private val bufferedImages2 = mutableMapOf<String, NVGImage>()

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
    fun createImage(identifier: Identifier, vararg imageFlags: Image.Flags): NVGImage {
        val bufferedImage = bufferedImages[identifier]
        if (bufferedImage != null) return bufferedImage
        val newImage = NVGImage(identifier, *imageFlags)
        bufferedImages[identifier] = newImage
        return newImage
    }

    @Throws(IOException::class)
    fun createImage(path: String, vararg imageFlags: Image.Flags): NVGImage {
        val bufferedImage = bufferedImages2[path]
        if (bufferedImage != null) return bufferedImage
        val newImage = NVGImage(path, *imageFlags)
        bufferedImages2[path] = newImage
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
        override val id: Int

        override val width: Int
        override val height: Int
        override val flags: List<Image.Flags>

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
        constructor(path: String, vararg imageFlags: Image.Flags) {
            this.path = path
            this.flags = imageFlags.toList()
            this.imageBuffer = resourceToByteBuffer(path)
            val tempData = createImage(*imageFlags)
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
        constructor(identifier: Identifier, vararg imageFlags: Image.Flags) {
            this.path = identifier.path
            this.flags = imageFlags.toList()
            this.imageBuffer = byteBufferFromIdentifier(identifier)
            val tempData = createImage(*imageFlags)
            id = tempData.id
            width = tempData.width
            height = tempData.height
        }

        @Throws(IOException::class)
        private fun createImage(vararg imageFlags: Image.Flags): TemporaryImageData {
            val w = IntArray(1)
            val h = IntArray(1)
            val channels = IntArray(1)
            val data: ByteBuffer =  STBImage.stbi_load_from_memory(imageBuffer, w, h, channels, 4) ?: throw FileNotFoundException(path)
            val id = nvgCreateImageRGBA(NVGR.nanoContext, w[0], h[0], imageFlags.nvgInt, data)
            MemoryUtil.memFree(data)
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

        private val Array<out Image.Flags>.nvgInt: Int
            get() {
                var nvg = 0

                this.forEach {
                    nvg = when(it) {
                        Image.Flags.GENERATE_MIPMAPS -> nvg or NVG_IMAGE_GENERATE_MIPMAPS
                        Image.Flags.REPEATX -> nvg or NVG_IMAGE_REPEATX
                        Image.Flags.REPEATY -> nvg or NVG_IMAGE_REPEATY
                        Image.Flags.FLIPY -> nvg or NVG_IMAGE_FLIPY
                        Image.Flags.NEAREST -> nvg or NVG_IMAGE_NEAREST
                    }
                }
                return nvg
            }

        private class TemporaryImageData(val id: Int, val width: Int, val height: Int)
    }
}