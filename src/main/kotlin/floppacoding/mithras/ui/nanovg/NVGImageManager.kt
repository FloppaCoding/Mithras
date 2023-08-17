package floppacoding.mithras.ui.nanovg

import floppacoding.mithras.Mithras
import org.apache.commons.io.IOUtils
import org.lwjgl.nanovg.NanoVG
import org.lwjgl.stb.STBImage
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

typealias NVGImage = NVGImageManager.Image

object NVGImageManager {
    val ICON: Image = Image( "/assets/${Mithras.RESOURCE_DOMAIN}/gui/icon.png")

    /**
     * Image for NanoVG.
     * @param path path to the resource. It looks like:
     *
     *      "/assets/mithras/gui/icon.png"
     */
    class Image(val path: String) {
        val imageBuffer : ByteBuffer = resourceToByteBuffer(path)

        /**
         * Image id according to NanoVG.
         */
        val id: Int

        init {
            val w = IntArray(1)
            val h = IntArray(1)
            val data: ByteBuffer =  STBImage.stbi_load_from_memory(
                imageBuffer,
                w,
                h,
                IntArray(1),
                4
            ) ?: throw FileNotFoundException(path)
            id = NanoVG.nvgCreateImageRGBA(NVGR.nanoContext, w[0], h[0], 0, data)
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
            val stream = this.javaClass.getResourceAsStream(path) ?: throw FileNotFoundException(path)
            val bytes = IOUtils.toByteArray(stream)
            val data = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder()).put(bytes)
            (data as Buffer).flip()
            stream.close()
            return data
        }
    }
}