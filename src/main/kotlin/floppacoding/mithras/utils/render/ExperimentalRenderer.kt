package floppacoding.mithras.utils.render


import floppacoding.mithras.ui.clickgui.util.ColorUtil
import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NanoVG.*
import org.lwjgl.nanovg.NanoVGGL3


object ExperimentalRenderer {

    private val nanoContext: Long = NanoVGGL3.nvgCreate(NanoVGGL3.NVG_ANTIALIAS)


    fun draw() {
        nvgBeginFrame(nanoContext, 1f, 1f, 1f)
        rect()
        nvgEndFrame(nanoContext)
    }

    fun rect() {
        nvgBeginPath(nanoContext)
        nvgRect(nanoContext, 0.3f, 0f, 0.6f, 1f)
        val nvgColor: NVGColor = color(nanoContext, ColorUtil.buttonColor)
        nvgFill(nanoContext)
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
}