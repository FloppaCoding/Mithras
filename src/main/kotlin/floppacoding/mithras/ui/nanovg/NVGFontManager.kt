package floppacoding.mithras.ui.nanovg

import org.apache.commons.io.IOUtils
import org.lwjgl.nanovg.NanoVG
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Provides fonts for the [VanoVG Renderer][NVGR].
 *
 * When a font file is not present when it is being loaded in the game will crash.
 * @author Aton
 */
object NVGFontManager {

    val ROBOTO: Font = Font("roboto", "/assets/mithras/gui/fonts/roboto-regular.ttf")

    /**
     * Font for NanoVG.
     * @param path path to the resource. It looks like:
     *
     *      "/assets/mithras/gui/fonts/roboto-regular.ttf"
     */
    class Font(val name: String, val path: String) {
        val fontBuffer : ByteBuffer = resourceToByteBuffer(path)

        /**
         * Font id according to NanoVG.
         */
        val id = NanoVG.nvgCreateFontMem(NVGR.nanoContext, "roboto", fontBuffer, 0)

        /**
         * Loads the resource as a byte buffer for use with NanoVG.
         * For mod assets the path has to look like:
         *
         *      "/assets/mithras/gui/fonts/roboto-regular.ttf"
         *
         * @throws FileNotFoundException when the file does not exist.
         */
        @Throws(IOException::class)
        private fun resourceToByteBuffer(path: String): ByteBuffer {
            val stream = this.javaClass.getResourceAsStream(path) ?: throw FileNotFoundException(path)
            val bytes = IOUtils.toByteArray(stream)
            val data = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder()).put(bytes)
            (data as Buffer).flip()
            return data
        }
    }
}