package floppacoding.mithras.utils.render

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import floppacoding.mithras.Mithras
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Vector3f
import java.awt.Color


/**
 * A collection of methods for rendering §D objects in the world.
 *
 * The methods use the standard Minecraft world coordinates.
 *
 * To use these methods you will need the render context which you will get from the
 * [RenderWorldOverlayEvent][floppacoding.mithras.events.RenderWorldOverlayEvent].
 *
 * @author Aton
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
object WorldRenderer {
    /* Some of the methods here could be a lot simpler if vanilla rendering layers were used, so that all the gl states do not have to be set individually.
    // However, those do not offer enough flexibility. The LINES and LINE_STRIP layers both do not support line widths other than 1.
    // And the DEBUG_LINE_STRIP layer technically should support custom line widths, however that does not seem to work.
    // And even if it did, it still does not support transparency.
    //
    // Creating custom rendering layers is also not that good of an option because it is awkward with relevant
    // methods and classes being private.
    //
    // Possible improvements for the future to this could be to either make a custom RenderLayer and properly disptach everything for it.
    // Or otherwise code a custom system similar to the render layers. */

    /**
     * Draws a line between the given two points [[x1],[y1],[z1]] and [[x2],[y2],[z2]].
     * @param color The color of the lines, transparency is supported.
     * @param lineWidth The line width.
     * @param phase Makes the box visible through blocks.
     */
    fun drawLine(context: WorldRenderContext, x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double, color: Color, lineWidth: Float = 1f, phase: Boolean = false) {
        drawLine(context, x1.toFloat(), y1.toFloat(), z1.toFloat(), x2.toFloat(), y2.toFloat(), z2.toFloat(), color, lineWidth, phase)
    }

    /**
     * Draws a line between the given two points [[x1],[y1],[z1]] and [[x2],[y2],[z2]].
     * @param color The color of the lines, transparency is supported.
     * @param lineWidth The line width.
     * @param phase Makes the box visible through blocks.
     */
    fun drawLine(context: WorldRenderContext, x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, color: Color, lineWidth: Float = 1f, phase: Boolean = false) {
        RenderSystem.assertOnRenderThread()

        val vec3d: Vec3d = context.camera().pos
        val cameraX = vec3d.getX()
        val cameraY = vec3d.getY()
        val cameraZ = vec3d.getZ()

        val matrices = context.matrixStack()
        matrices.push()
        matrices.translate(-cameraX, -cameraY, -cameraZ)
        val positionMatrix: Matrix4f = matrices.peek().positionMatrix
        val normalMatrix = matrices.peek().normalMatrix

        RenderSystem.depthMask(false)
        RenderSystem.disableCull()
        if (phase) RenderSystem.disableDepthTest() else RenderSystem.enableDepthTest()
        RenderSystem.lineWidth(lineWidth)
        RenderSystem.enableBlend()
        RenderSystem.blendFuncSeparate(
            GlStateManager.SrcFactor.SRC_ALPHA,
            GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SrcFactor.ONE,
            GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA
        )

        val rgba = color.rgb

        val tessellator = RenderSystem.renderThreadTesselator()

        RenderSystem.setShader { GameRenderer.getRenderTypeLinesProgram() }
        val bufferBuilder = tessellator.buffer
        bufferBuilder.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES)

        val lineNormal = Vector3f(x2-x1, y2-y1, z2-z1).normalize()

        bufferBuilder.vertex(positionMatrix, x1, y1, z1).color(rgba).normal(normalMatrix, lineNormal.x, lineNormal.y, lineNormal.z).next()
        bufferBuilder.vertex(positionMatrix, x2, y2, z2).color(rgba).normal(normalMatrix, lineNormal.x, lineNormal.y, lineNormal.z).next()

        tessellator.draw()


        matrices.pop()
        RenderSystem.lineWidth(1.0f)
        RenderSystem.enableCull()
        RenderSystem.depthMask(true)
        RenderSystem.disableBlend()
        RenderSystem.defaultBlendFunc()
    }

    /**
     * Draws an outline for the block at the given [position].
     *
     * Both an outline at the edges and filled sides are possible.
     * If you do not want one of those to show set the corresponding color parameter to null.
     * This is more efficient than just setting the alpha of the color to 0.
     *
     * At least one of [outlineColor] or [fillColor] has to be not null for anything to be drawn.
     *
     * @param outlineColor The color of the lines, transparency is supported. Set to null to not render the outline.
     * @param fillColor The color of the sides, transparency is supported. Set to null to not render the filled sides.
     * @param lineWidth The line width.
     * @param phase Makes the box visible through blocks.
     *
     * @see drawBoxOutline
     * @see drawFilledBox
     * @see drawOutlinedFilledBox
     */
    fun drawBlockBoundingBox(context: WorldRenderContext, position: BlockPos, outlineColor: Color? = Color(0xff0000), fillColor: Color? = null, lineWidth: Float = 1f, phase: Boolean = false) {
        val state: BlockState = Mithras.mc.world?.getBlockState(position)?: return
        val shape = state.getOutlineShape(Mithras.mc.world, position, ShapeContext.of(Mithras.mc.player))
        val box = shape.boundingBox.offset(position)
        drawBox(context, box, outlineColor, fillColor, lineWidth, phase)
    }

    /**
     * Draws the given [box].
     *
     * Both an outline at the edges and filled sides are possible.
     * If you do not want one of those to show set the corresponding color parameter to null.
     * This is more efficient than just setting the alpha of the color to 0.
     *
     * At least one of [outlineColor] or [fillColor] has to be not null for anything to be drawn.
     *
     *
     * @param outlineColor The color of the lines, transparency is supported. Set to null to not render the outline.
     * @param fillColor The color of the sides, transparency is supported. Set to null to not render the filled sides.
     * @param lineWidth The line width.
     * @param phase Makes the box visible through blocks.
     *
     * @see drawBoxOutline
     * @see drawFilledBox
     * @see drawOutlinedFilledBox
     */
    fun drawBox(context: WorldRenderContext, box: Box, outlineColor: Color? = Color(0xff0000), fillColor: Color? = null, lineWidth: Float = 1f, phase: Boolean = false) {
        if (outlineColor != null && fillColor == null) {
            drawBoxOutline(context,box, outlineColor, lineWidth, phase)
        }
        else if (outlineColor == null && fillColor != null) {
            drawFilledBox(context, box, fillColor, phase)
        }
        else if (outlineColor != null && fillColor != null) {
                    drawOutlinedFilledBox(context, box, outlineColor, fillColor, lineWidth, phase)
                }
    }

    /**
     * Draws a box outline for the given [box].
     *
     * @param color The color of the lines, transparency is supported.
     * @param lineWidth The line width.
     * @param phase Makes the box visible through blocks.
     */
    fun drawBoxOutline(context: WorldRenderContext, box: Box, color: Color, lineWidth: Float = 1f, phase: Boolean = false) {
        drawBoxOutline(context, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, color, lineWidth, phase)
    }

    /**
     * Draws a box outline for a box aligned with the world's coordinate axes spanned by the two points [[x1],[y1],[z1]] and [[x2],[y2],[z2]].
     *
     * It is assumed that [x2],[y2],[z2] are bigger than the corresponding [x1],[y1],[z1].
     * @param color The color of the lines, transparency is supported.
     * @param lineWidth The line width.
     * @param phase Makes the box visible through blocks.
     */
    fun drawBoxOutline(context: WorldRenderContext, x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double, color: Color, lineWidth: Float = 1f, phase: Boolean = false) {
        drawBoxOutline(context, x1.toFloat(), y1.toFloat(), z1.toFloat(), x2.toFloat(), y2.toFloat(), z2.toFloat(), color, lineWidth, phase)
    }

    /**
     * Draws a box outline for a box aligned with the world's coordinate axes spanned by the two points [[x1],[y1],[z1]] and [[x2],[y2],[z2]].
     *
     * It is assumed that [x2],[y2],[z2] are bigger than the corresponding [x1],[y1],[z1].
     * @param color The color of the lines, transparency is supported.
     * @param lineWidth The line width.
     * @param phase Makes the box visible through blocks.
     */
    fun drawBoxOutline(context: WorldRenderContext, x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, color: Color, lineWidth: Float = 1f, phase: Boolean = false) {
        RenderSystem.assertOnRenderThread()

        val vec3d: Vec3d = context.camera().pos
        val cameraX = vec3d.getX()
        val cameraY = vec3d.getY()
        val cameraZ = vec3d.getZ()

        val matrices = context.matrixStack()
        matrices.push()
        matrices.translate(-cameraX, -cameraY, -cameraZ)
        val positionMatrix: Matrix4f = matrices.peek().positionMatrix
        val normalMatrix = matrices.peek().normalMatrix

        RenderSystem.depthMask(false)
        RenderSystem.disableCull()
        if (phase) RenderSystem.disableDepthTest() else RenderSystem.enableDepthTest()
        RenderSystem.lineWidth(lineWidth)
        RenderSystem.enableBlend()
        RenderSystem.blendFuncSeparate(
            GlStateManager.SrcFactor.SRC_ALPHA,
            GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SrcFactor.ONE,
            GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA
        )

        val tessellator = RenderSystem.renderThreadTesselator()

        outlineBox(tessellator, positionMatrix, normalMatrix, x1, y1, z1, x2, y2, z2, color.rgb)

        matrices.pop()
        RenderSystem.lineWidth(1.0f)
        RenderSystem.enableCull()
        RenderSystem.depthMask(true)
        RenderSystem.disableBlend()
        RenderSystem.defaultBlendFunc()

    }

    /**
     * Draws a box with filled sides for the given [box].
     *
     * @param fillColor The color of the sides, transparency is supported.
     * @param phase Makes the box visible through blocks.
     */
    fun drawFilledBox(context: WorldRenderContext, box: Box, fillColor: Color, phase: Boolean = false) {
        drawFilledBox(context, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, fillColor, phase)
    }

    /**
     * Draws a box with filled sides but without an outline for a box aligned with the world's coordinate axes spanned by the two points [[x1],[y1],[z1]] and [[x2],[y2],[z2]].
     *
     * It is assumed that [x2],[y2],[z2] are bigger than the corresponding [x1],[y1],[z1].
     * @param fillColor The color of the sides, transparency is supported.
     * @param phase Makes the box visible through blocks.
     */
    fun drawFilledBox(context: WorldRenderContext, x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double, fillColor: Color, phase: Boolean = false) {
        drawFilledBox(context, x1.toFloat(), y1.toFloat(), z1.toFloat(), x2.toFloat(), y2.toFloat(), z2.toFloat(), fillColor, phase)
    }

    /**
     * Draws a box with filled sides but without an outline for a box aligned with the world's coordinate axes spanned by the two points [[x1],[y1],[z1]] and [[x2],[y2],[z2]].
     *
     * It is assumed that [x2],[y2],[z2] are bigger than the corresponding [x1],[y1],[z1].
     * @param fillColor The color of the sides, transparency is supported.
     * @param phase Makes the box visible through blocks.
     */
    fun drawFilledBox(context: WorldRenderContext, x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, fillColor: Color, phase: Boolean = false) {
        RenderSystem.assertOnRenderThread()

        val vec3d: Vec3d = context.camera().pos
        val cameraX = vec3d.getX()
        val cameraY = vec3d.getY()
        val cameraZ = vec3d.getZ()

        val matrices = context.matrixStack()
        matrices.push()
        matrices.translate(-cameraX, -cameraY, -cameraZ)
        val matrix4f: Matrix4f = matrices.peek().positionMatrix

        RenderSystem.depthMask(false)
        RenderSystem.disableCull()
        if (phase) RenderSystem.disableDepthTest() else RenderSystem.enableDepthTest()
        RenderSystem.enableBlend()
        RenderSystem.blendFuncSeparate(
            GlStateManager.SrcFactor.SRC_ALPHA,
            GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SrcFactor.ONE,
            GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA
        )

        val tessellator = RenderSystem.renderThreadTesselator()

        fillSides(tessellator, matrix4f, x1, y1, z1, x2, y2, z2, fillColor.rgb)

        matrices.pop()
        RenderSystem.enableCull()
        RenderSystem.depthMask(true)
        RenderSystem.disableBlend()
        RenderSystem.defaultBlendFunc()
    }

    /**
     * Draws a box outline with filled sides for the given [box].
     *
     * @param outlineColor The color of the lines, transparency is supported.
     * @param fillColor The color of the sides, transparency is supported.
     * @param lineWidth The line width.
     * @param phase Makes the box visible through blocks.
     */
    fun drawOutlinedFilledBox(context: WorldRenderContext, box: Box, outlineColor: Color, fillColor: Color, lineWidth: Float = 1f, phase: Boolean = false) {
        drawOutlinedFilledBox(context, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, outlineColor, fillColor, lineWidth, phase)
    }

    /**
     * Draws a box outline with filled sides for a box aligned with the world's coordinate axes spanned by the two points [[x1],[y1],[z1]] and [[x2],[y2],[z2]].
     *
     * It is assumed that [x2],[y2],[z2] are bigger than the corresponding [x1],[y1],[z1].
     * @param outlineColor The color of the lines, transparency is supported.
     * @param fillColor The color of the sides, transparency is supported.
     * @param lineWidth The line width.
     * @param phase Makes the box visible through blocks.
     */
    fun drawOutlinedFilledBox(context: WorldRenderContext, x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double, outlineColor: Color, fillColor: Color, lineWidth: Float = 1f, phase: Boolean = false) {
        drawOutlinedFilledBox(context, x1.toFloat(), y1.toFloat(), z1.toFloat(), x2.toFloat(), y2.toFloat(), z2.toFloat(), outlineColor, fillColor, lineWidth, phase)
    }

    /**
     * Draws a box outline with filled sides for a box aligned with the world's coordinate axes spanned by the two points [[x1],[y1],[z1]] and [[x2],[y2],[z2]].
     *
     * It is assumed that [x2],[y2],[z2] are bigger than the corresponding [x1],[y1],[z1].
     * @param outlineColor The color of the lines, transparency is supported.
     * @param fillColor The color of the sides, transparency is supported.
     * @param lineWidth The line width.
     * @param phase Makes the box visible through blocks.
     */
    fun drawOutlinedFilledBox(context: WorldRenderContext, x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, outlineColor: Color, fillColor: Color, lineWidth: Float = 1f, phase: Boolean = false) {
        RenderSystem.assertOnRenderThread()

        val vec3d: Vec3d = context.camera().pos
        val cameraX = vec3d.getX()
        val cameraY = vec3d.getY()
        val cameraZ = vec3d.getZ()

        val matrices = context.matrixStack()
        matrices.push()
        matrices.translate(-cameraX, -cameraY, -cameraZ)
        val positionMatrix: Matrix4f = matrices.peek().positionMatrix
        val normalMatrix: Matrix3f = matrices.peek().normalMatrix

        RenderSystem.depthMask(false)
        RenderSystem.disableCull()
        if (phase) RenderSystem.disableDepthTest() else RenderSystem.enableDepthTest()
        RenderSystem.lineWidth(lineWidth)
        RenderSystem.enableBlend()
        RenderSystem.blendFuncSeparate(
            GlStateManager.SrcFactor.SRC_ALPHA,
            GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SrcFactor.ONE,
            GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA
        )

        val tessellator = RenderSystem.renderThreadTesselator()

        fillSides(tessellator, positionMatrix, x1, y1, z1, x2, y2, z2, fillColor.rgb)

        outlineBox(tessellator, positionMatrix, normalMatrix, x1, y1, z1, x2, y2, z2, outlineColor.rgb)


        matrices.pop()
        RenderSystem.lineWidth(1.0f)
        RenderSystem.enableCull()
        RenderSystem.depthMask(true)
        RenderSystem.disableBlend()
        RenderSystem.defaultBlendFunc()
    }

    private fun fillSides(tessellator: Tessellator, positionMatrix: Matrix4f, x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, color: Int) {
        RenderSystem.setShader { GameRenderer.getPositionColorProgram() }
        // This polygon offset takes care of the Z-fighting that would otherwise happen when one of the planes coincides
        // with the side of a block. The block texture and the quad here drawn would clash in the depth test and
        // rounding errors would determine which end up on top for every pixel individually.
        RenderSystem.enablePolygonOffset()
        RenderSystem.polygonOffset(-1f, -1f)
        val bufferBuilder = tessellator.buffer
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)

        // Bottom side
        bufferBuilder.vertex(positionMatrix, x1, y1, z1).color(color).next()
        bufferBuilder.vertex(positionMatrix, x2, y1, z1).color(color).next()
        bufferBuilder.vertex(positionMatrix, x2, y1, z2).color(color).next()
        bufferBuilder.vertex(positionMatrix, x1, y1, z2).color(color).next()

        // Top side
        bufferBuilder.vertex(positionMatrix, x1, y2, z1).color(color).next()
        bufferBuilder.vertex(positionMatrix, x2, y2, z1).color(color).next()
        bufferBuilder.vertex(positionMatrix, x2, y2, z2).color(color).next()
        bufferBuilder.vertex(positionMatrix, x1, y2, z2).color(color).next()

        // West (-X) side
        bufferBuilder.vertex(positionMatrix, x1, y1, z1).color(color).next()
        bufferBuilder.vertex(positionMatrix, x1, y1, z2).color(color).next()
        bufferBuilder.vertex(positionMatrix, x1, y2, z2).color(color).next()
        bufferBuilder.vertex(positionMatrix, x1, y2, z1).color(color).next()

        // East (+X) side
        bufferBuilder.vertex(positionMatrix, x2, y1, z1).color(color).next()
        bufferBuilder.vertex(positionMatrix, x2, y1, z2).color(color).next()
        bufferBuilder.vertex(positionMatrix, x2, y2, z2).color(color).next()
        bufferBuilder.vertex(positionMatrix, x2, y2, z1).color(color).next()

        // North (-Z) side
        bufferBuilder.vertex(positionMatrix, x1, y1, z1).color(color).next()
        bufferBuilder.vertex(positionMatrix, x2, y1, z1).color(color).next()
        bufferBuilder.vertex(positionMatrix, x2, y2, z1).color(color).next()
        bufferBuilder.vertex(positionMatrix, x1, y2, z1).color(color).next()

        // South (+Z) side
        bufferBuilder.vertex(positionMatrix, x1, y1, z2).color(color).next()
        bufferBuilder.vertex(positionMatrix, x2, y1, z2).color(color).next()
        bufferBuilder.vertex(positionMatrix, x2, y2, z2).color(color).next()
        bufferBuilder.vertex(positionMatrix, x1, y2, z2).color(color).next()

        tessellator.draw()
        RenderSystem.disablePolygonOffset()
    }

    private fun outlineBox(tessellator: Tessellator, positionMatrix: Matrix4f, normalMatrix: Matrix3f, x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, color: Int) {
        RenderSystem.setShader { GameRenderer.getRenderTypeLinesProgram() }
        val bufferBuilder = tessellator.buffer
        bufferBuilder.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES)
        // This rendering works by using 2 vertices per line, one for the start point and one for the end.
        // The normal goes along the line and is used for properly displaying the line width.

        // Bottom 4 edges
        bufferBuilder.vertex(positionMatrix, x1, y1, z1).color(color).normal(normalMatrix,1f,0f,0f).next()
        bufferBuilder.vertex(positionMatrix, x2, y1, z1).color(color).normal(normalMatrix,1f,0f,0f).next()
        bufferBuilder.vertex(positionMatrix, x2, y1, z1).color(color).normal(normalMatrix,0f,0f,1f).next()
        bufferBuilder.vertex(positionMatrix, x2, y1, z2).color(color).normal(normalMatrix,0f,0f,1f).next()
        bufferBuilder.vertex(positionMatrix, x2, y1, z2).color(color).normal(normalMatrix,-1f,0f,0f).next()
        bufferBuilder.vertex(positionMatrix, x1, y1, z2).color(color).normal(normalMatrix,-1f,0f,0f).next()
        bufferBuilder.vertex(positionMatrix, x1, y1, z2).color(color).normal(normalMatrix,0f,0f,-1f).next()
        bufferBuilder.vertex(positionMatrix, x1, y1, z1).color(color).normal(normalMatrix,0f,0f,-1f).next()

        // Top 4 edges
        bufferBuilder.vertex(positionMatrix, x1, y2, z1).color(color).normal(normalMatrix,1f,0f,0f).next()
        bufferBuilder.vertex(positionMatrix, x2, y2, z1).color(color).normal(normalMatrix,1f,0f,0f).next()
        bufferBuilder.vertex(positionMatrix, x2, y2, z1).color(color).normal(normalMatrix,0f,0f,1f).next()
        bufferBuilder.vertex(positionMatrix, x2, y2, z2).color(color).normal(normalMatrix,0f,0f,1f).next()
        bufferBuilder.vertex(positionMatrix, x2, y2, z2).color(color).normal(normalMatrix,-1f,0f,0f).next()
        bufferBuilder.vertex(positionMatrix, x1, y2, z2).color(color).normal(normalMatrix,-1f,0f,0f).next()
        bufferBuilder.vertex(positionMatrix, x1, y2, z2).color(color).normal(normalMatrix,0f,0f,-1f).next()
        bufferBuilder.vertex(positionMatrix, x1, y2, z1).color(color).normal(normalMatrix,0f,0f,-1f).next()

        // 4 Side edges
        bufferBuilder.vertex(positionMatrix, x1, y1, z1).color(color).normal(normalMatrix,0f,1f,0f).next()
        bufferBuilder.vertex(positionMatrix, x1, y2, z1).color(color).normal(normalMatrix,0f,1f,0f).next()
        bufferBuilder.vertex(positionMatrix, x2, y1, z1).color(color).normal(normalMatrix,0f,1f,0f).next()
        bufferBuilder.vertex(positionMatrix, x2, y2, z1).color(color).normal(normalMatrix,0f,1f,0f).next()
        bufferBuilder.vertex(positionMatrix, x1, y1, z2).color(color).normal(normalMatrix,0f,1f,0f).next()
        bufferBuilder.vertex(positionMatrix, x1, y2, z2).color(color).normal(normalMatrix,0f,1f,0f).next()
        bufferBuilder.vertex(positionMatrix, x2, y1, z2).color(color).normal(normalMatrix,0f,1f,0f).next()
        bufferBuilder.vertex(positionMatrix, x2, y2, z2).color(color).normal(normalMatrix,0f,1f,0f).next()

        tessellator.draw()
    }
}