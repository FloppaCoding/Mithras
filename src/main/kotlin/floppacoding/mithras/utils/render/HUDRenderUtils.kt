package floppacoding.mithras.utils.render

import com.mojang.blaze3d.systems.RenderSystem
import floppacoding.mithras.Mithras.mc
import net.minecraft.client.render.Tessellator
import org.lwjgl.opengl.GL11

/**
 * ## A Collection of methods for rendering 2D Objects in orthographic projection for the HUD or for a gui.
 *
 * ### Coordinate space
 * The coordinate space used by the methods here sees the top left corner of your window as the origin 0,0.
 * The x-axis is pointing towards the right of the screen. and the y-axis is pointing **downwards**.
 *
 *
 * Heavily based on the rendering for [Funny Map by Harry282](https://github.com/Harry282/FunnyMap/blob/master/src/main/kotlin/funnymap/utils/RenderUtils.kt).
 *
 * @author Aton
 */
object HUDRenderUtils {

    private val tessellator: Tessellator = Tessellator.getInstance()
//    private val worldRenderer: WorldRenderer = tessellator.worldRenderer
//
//    fun renderRect(x: Double, y: Double, w: Double, h: Double, color: Color) {
//        if (color.alpha == 0) return
//        RenderSystem.enableBlend()
//        RenderSystem.disableTexture2D()
//        RenderSystem.enableAlpha()
//        RenderSystem.tryBlendFuncSeparate(770, 771, 1, 0)
//        RenderSystem.color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
//
//        worldRenderer.begin(GL11.GL_QUADS, VertexFormats.POSITION)
//        addQuadVertices(x, y, w, h)
//        tessellator.draw()
//
//        GlStateManager.disableAlpha()
//        GlStateManager.enableTexture2D()
//        GlStateManager.disableBlend()
//    }
//
//    fun renderRectBorder(x: Double, y: Double, w: Double, h: Double, thickness: Double, color: Color) {
//        if (color.alpha == 0) return
//        GlStateManager.enableBlend()
//        GlStateManager.disableTexture2D()
//        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
//        GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
//
//        worldRenderer.begin(GL11.GL_QUADS, VertexFormats.POSITION)
//        GlStateManager.shadeModel(GL11.GL_FLAT)
//
//        addQuadVertices(x - thickness, y, thickness, h)
//        addQuadVertices(x - thickness, y - thickness, w + thickness * 2, thickness)
//        addQuadVertices(x + w, y, thickness, h)
//        addQuadVertices(x - thickness, y + h, w + thickness * 2, thickness)
//
//        tessellator.draw()
//
//        GlStateManager.enableTexture2D()
//        GlStateManager.disableBlend()
//        GlStateManager.shadeModel(GL11.GL_SMOOTH)
//    }
//
//    private fun addQuadVertices(x: Double, y: Double, w: Double, h: Double) {
//        worldRenderer.pos(x, y + h, 0.0).endVertex()
//        worldRenderer.pos(x + w, y + h, 0.0).endVertex()
//        worldRenderer.pos(x + w, y, 0.0).endVertex()
//        worldRenderer.pos(x, y, 0.0).endVertex()
//    }
//
//    /**
//     * Used for funny map text rendering. Has the text scaling directly integrated.
//     */
//    fun renderCenteredText(text: List<String>, x: Int, y: Int, color: Int) {
//        GlStateManager.pushMatrix()
//
//        GlStateManager.translate(x.toFloat(), y.toFloat(), 0f)
//        GlStateManager.scale(DungeonMap.textScale.value, DungeonMap.textScale.value, 1.0)
//
//        if (text.isNotEmpty()) {
//            val yTextOffset = text.size * 5f
//            for (i in text.indices) {
//                mc.fontRendererObj.drawString(
//                    text[i],
//                    (-mc.fontRendererObj.getStringWidth(text[i]) shr 1).toFloat(),
//                    i * 10 - yTextOffset,
//                    color,
//                    true
//                )
//            }
//        }
//        GlStateManager.popMatrix()
//    }
//
//    fun drawTexturedModalRect(x: Int, y: Int, width: Int, height: Int) {
//        worldRenderer.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE)
//        worldRenderer.pos(x.toDouble(), (y + height).toDouble(), 0.0).tex(0.0, 1.0).endVertex()
//        worldRenderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0).tex(1.0, 1.0).endVertex()
//        worldRenderer.pos((x + width).toDouble(), y.toDouble(), 0.0).tex(1.0, 0.0).endVertex()
//        worldRenderer.pos(x.toDouble(), y.toDouble(), 0.0).tex(0.0, 0.0).endVertex()
//        tessellator.draw()
//    }

    /**
     * Sets up a GL scissor test for the specified region of the screen.
     *
     * Uses the same coordinate system as all the rendering methods.
     * The native OpenGL method [GL11.glScissor] uses a different coordinate system.
     * This method takes care of the coordinate transform for you.
     *
     * @param scale use this if your current draw scale does not match the window
     * [scaleFactor][net.minecraft.client.util.Window.scaleFactor]
     *
     * @see setUpScissor
     * @see endScissor
     */
    fun setUpScissorAbsolute(left: Int, top: Int, right: Int, bottom: Int, scale: Double = mc.window.scaleFactor) {
        setUpScissor(left, top, (right - left).coerceAtLeast(0), (bottom - top).coerceAtLeast(0), scale)
    }

    /**
     * Sets up a GL scissor test for the specified region of the screen.
     *
     * Uses the same coordinate system as all the rendering methods.
     * The native OpenGL method [GL11.glScissor] uses a different coordinate system.
     * This method takes care of the coordinate transform for you.
     *
     * @param scale use this if your current draw scale does not match the window
     * [scaleFactor][net.minecraft.client.util.Window.scaleFactor]
     *
     * @see setUpScissorAbsolute
     * @see endScissor
     */
    fun setUpScissor(x: Int, y: Int, width: Int, height: Int, scale: Double = mc.window.scaleFactor) {
        /*
        glScissor uses different coordinates than all the rendering methods.
        It uses absolute window coordinates starting with 0,0 in the bottom left corner of the window.
        The coordinates directly relate to pixels.
        It is not affected by things such as glTanslate and glScale.

        In contrast, all other hud rendering methods use the top left corner as 0,0
         */
        RenderSystem.enableScissor(
            (x * scale).toInt(),
            (mc.window.height - (height + y) *scale).toInt(),
            (width*scale).toInt(),
            (height * scale).toInt()
        )
    }

    /**
     * Disables the GL scissor test.
     * @see setUpScissor
     * @see setUpScissorAbsolute
     */
    fun endScissor() {
        RenderSystem.disableScissor()
    }
}
