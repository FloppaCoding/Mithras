package floppacoding.mithras.utils.render


import floppacoding.mithras.Mithras
import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import net.minecraft.util.Identifier
import org.apache.commons.io.IOUtils
import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NanoVG.*
import org.lwjgl.nanovg.NanoVGGL3
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files


object ExperimentalRenderer {

    private val nanoContext: Long = NanoVGGL3.nvgCreate(NanoVGGL3.NVG_ANTIALIAS)

    private var fontBuffer: ByteBuffer? = null

    init {
        loadFont()
    }


    fun draw() {
        nvgBeginFrame(nanoContext, mc.window.width.toFloat(), mc.window.height.toFloat(), 1f)
        rect()
        nvgEndFrame(nanoContext)
    }

    fun rect() {
        text()
        nvgBeginPath(nanoContext)
//        nvgRect(nanoContext, 10f, 100f, 50f, 100f)
        nvgRoundedRect(nanoContext, 600f, 300f, 200f, 400f, 13f)
        val nvgColor: NVGColor = color(nanoContext, ColorUtil.clickGUIColor.rgb)
        nvgFill(nanoContext)
        nvgColor.free()
    }

    fun text() {
        nvgBeginPath(nanoContext)
        nvgFontSize(nanoContext, 31f)
        nvgFontFace(nanoContext, "roboto")
        nvgTextAlign(nanoContext, NVG_ALIGN_LEFT or NVG_ALIGN_MIDDLE)
        val nvgColor = color(nanoContext, ColorUtil.outlineColor)
        nvgFillColor(nanoContext, nvgColor)
        nvgText(nanoContext, 20f, 70f, "Hamburgers and cheesburgers go well with fries!")
//        nvgFill(nanoContext)
        nvgColor.free()
    }

    /**
     * Create a [NVGColor] from the provided RGBA values.
     *
     * @param vg    The NanoVG context.
     * @param color The color.
     * @return The [NVGColor] created.
     */
    fun color(vg: Long, color: Int): NVGColor {
        val nvgColor = NVGColor.calloc()
        nvgRGBA(
            (color shr 16 and 0xFF).toByte(),
            (color shr 8 and 0xFF).toByte(),
            (color and 0xFF).toByte(),
            (color shr 24 and 0xFF).toByte(),
            nvgColor
        )
        nvgFillColor(vg, nvgColor)
        return nvgColor
    }


    fun loadFont() {
        if (fontBuffer != null) return


        val identifier = Identifier(Mithras.RESOURCE_DOMAIN, "gui/fonts/roboto-regular.ttf")

        try {
            val buffer = byteBufferFromIdentifier(identifier)
            nvgCreateFontMem(nanoContext, "roboto", buffer, 0)
            fontBuffer = buffer
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    /**
     * For mod assets the path has to look like:
     *
     *      "/assets/mithras/gui/fonts/roboto-regular.ttf"
     *
     * @throws FileNotFoundException when the file does not exist.
     */
    @Throws(IOException::class)
    fun resourceToByteBuffer(path: String): ByteBuffer {
        val trimmedPath = path.trim { it <= ' ' }
        val stream: InputStream?
        val file = File(trimmedPath)
        if (!file.exists() || !file.isFile()) throw FileNotFoundException(trimmedPath)
        stream = Files.newInputStream(file.toPath())
        val bytes: ByteArray = IOUtils.toByteArray(stream)
        val data = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder())
            .put(bytes)
        (data as Buffer).flip()
        return data
    }


    /**
     * Gets the resource from the identifier as a byte buffer.
     *
     * @throws FileNotFoundException when the resource could not be loaded.
     */
    @Throws(IOException::class)
    fun byteBufferFromIdentifier(identifier: Identifier): ByteBuffer {
        val resource = mc.resourceManager.getResource(identifier)
        if (!resource.isPresent) throw FileNotFoundException(identifier.namespace +":"+identifier.path)
        val bytes = IOUtils.toByteArray(resource.get().inputStream)
        val data = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder())
            .put(bytes)
        (data as Buffer).flip()
        return data
    }
}