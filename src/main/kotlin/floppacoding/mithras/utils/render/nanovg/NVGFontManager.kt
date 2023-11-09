package floppacoding.mithras.utils.render.nanovg

import floppacoding.mithras.Mithras
import floppacoding.mithras.utils.render.Font
import org.apache.commons.io.IOUtils
import org.lwjgl.nanovg.NanoVG
import org.lwjgl.system.MemoryUtil
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.ByteBuffer

typealias NVGFont = NVGFontManager.NVGFont

/**
 * Provides fonts for the [NanoVG Renderer][NVGR].
 *
 * When a font file is not present when it is being loaded in the game will crash.
 * @author Aton
 */
object NVGFontManager {

    val ROBOTO: NVGFont =
        NVGFont("roboto", "/assets/${Mithras.RESOURCE_DOMAIN}/gui/fonts/roboto-regular.ttf")

    val KURINTO: NVGFont =
        NVGFont("kurinto", "/assets/${Mithras.RESOURCE_DOMAIN}/gui/fonts/KurintoSans-Rg.ttf")

    /**
     * Font for NanoVG.
     * @param path path to the resource. It looks like:
     *
     *      "/assets/mithras/gui/fonts/roboto-regular.ttf"
     */
    class NVGFont(val name: String, val path: String) : Font {
        /**
         * Font id according to NanoVG.
         */
        val id: Int

        init {
            val fontBuffer : ByteBuffer = resourceToByteBuffer(path)
            id = NanoVG.nvgCreateFontMem(NVGR.nanoContext, name, fontBuffer, 1)
        }

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
            val data = MemoryUtil.memAlloc(bytes.size).put(0, bytes)
            stream.close()
            return data
        }
    }
}