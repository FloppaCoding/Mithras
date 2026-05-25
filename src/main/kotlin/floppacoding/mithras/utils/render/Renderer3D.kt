package floppacoding.mithras.utils.render

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexFormat
import floppacoding.mithras.Mithras
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec3d
import org.joml.Matrix4f
import org.joml.Vector3f
import java.awt.Color
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin


/**
 * # A collection of methods for rendering objects in the 3D world.
 *
 * The methods use the standard Minecraft world coordinates.
 *
 * To use these methods you will need the render context which you will get from the
 * [RenderWorldOverlayEvent][floppacoding.mithras.events.RenderWorldOverlayEvent].
 *
 * @author Aton
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
object Renderer3D {
    /* Some of the methods here could be a lot simpler if vanilla rendering layers were used, so that all the gl states do not have to be set individually.
    // However, those do not offer enough flexibility. The LINES and LINE_STRIP layers both do not support line widths other than 1.
    // And the DEBUG_LINE_STRIP layer technically should support custom line widths, however that does not seem to work.
    // And even if it did, it still does not support transparency.
    //
    // This is now implemented however line widths are for now no longer supported.
    //
    // Possible improvements for the future to this could be to either make a custom RenderLayer and properly disptach everything for it.
    // Or otherwise code a custom system similar to the render layers. */

    /**
     * Draws a line between the given two points [pos1] and [pos2].
     * @param color The color of the lines, transparency is supported.
     * @param lineWidth The line width.
     * @param phase Makes the box visible through blocks.
     */
    fun drawLine(context: WorldRenderContext, pos1: Vec3d, pos2: Vec3d, color: Color, lineWidth: Float = 1f, phase: Boolean = false) {
        drawLine(context, pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z, color, lineWidth, phase)
    }

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
        if (!color.isVisible()) return
        RenderSystem.assertOnRenderThread()

        val vec3d: Vec3d = context.worldState().cameraRenderState.pos
        val matrices = context.matrices()
        matrices.push()
        matrices.translate(-vec3d.getX(), -vec3d.getY(), -vec3d.getZ())

        val positionMatrix: Matrix4f = matrices.peek().positionMatrix
        val normalMatrix = matrices.peek()
        val rgba = color.rgb

        val tessellator = Tessellator.getInstance()
        val bufferBuilder = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH)

        val lineNormal = Vector3f(x2-x1, y2-y1, z2-z1).normalize()

        bufferBuilder.vertex(positionMatrix, x1, y1, z1).color(rgba).normal(normalMatrix, lineNormal.x, lineNormal.y, lineNormal.z).lineWidth(lineWidth)
        bufferBuilder.vertex(positionMatrix, x2, y2, z2).color(rgba).normal(normalMatrix, lineNormal.x, lineNormal.y, lineNormal.z).lineWidth(lineWidth)


        val builtBuffer = bufferBuilder.end()
        val layer = if (phase) RenderLayers.LINES_PHASE else RenderLayers.LINES
        layer.draw(builtBuffer)


        matrices.pop()
    }

    /**
     * ## Draws a circle that can be filled in and/or outlined.
     *
     * ### Coloring
     * If you do not want the outline or the filled plane to show set the corresponding color parameter to null.
     *
     * At least one of [outlineColor] or [fillColor] has to be not null for anything to be drawn.
     *
     * ### Orientation
     * The circle is centered at [[xCenter], [yCenter], [zCenter]].
     * It will be drawn in the plane defined by the [normal] vector. (That means the plane orthogonal to that vector.)
     *
     * ### Polygons
     * The circle is drawn as an approximation through a polygon with [segments] as side/edge number.
     *
     * Therefore, this method can also be used to render polygons if [segments] is set sufficiently low.
     * The polygon will be aligned with one corner in positive X-direction.
     * From there it is rotated into the plane given by the [normal].
     * If the polygon needs to be further rotated in the plane [drawEllipse] can be used along with its angle parameter.
     *
     * @param radius The radius of the circle.
     * @param outlineColor The color of the outline, transparency is supported. Set to null to not render the outline.
     * @param fillColor The fill color, transparency is supported. Set to null to not fill the circle.
     * @param segments The number of line / triangle segments used to approximate the circle.
     * @param lineWidth The outline width.
     * @param phase Makes the circle visible through blocks.
     *
     * @see drawEllipse
     */
    fun drawCircle(context: WorldRenderContext, xCenter: Float, yCenter: Float, zCenter:Float, radius: Float, normal: Vector3f, outlineColor: Color? = null, fillColor: Color? = null, segments: Int = 100, lineWidth: Float = 1f, phase: Boolean = false) {
        drawEllipse(context, xCenter, yCenter, zCenter, radius, radius, 0f, normal, outlineColor, fillColor, segments, lineWidth, phase)
    }

    /**
     * ## Draws an ellipse that can be filled in and/or outlined.
     *
     * ### Coloring
     * If you do not want the outline or the filled plane to show set the corresponding color parameter to null.
     *
     * At least one of [outlineColor] or [fillColor] has to be not null for anything to be drawn.
     *
     * The ellipse is centered at [[xCenter], [yCenter], [zCenter]].
     * It will be drawn in the plane defined by the [normal] vector. (That means the plane orthogonal to that vector.)
     *
     * ### Orientation
     * The ellipse is centered at [[xCenter], [yCenter], [zCenter]].
     * It will be drawn in the plane defined by the [normal] vector. (That means the plane orthogonal to that vector.)
     *
     * The Ellipse is drawn into X-Z-plane, it then is rotated by [angle] around the Y-axis.
     * (The X-axis gets rotated in direction of the -Z-axis by a 90° rotation.)
     * Finally, the polygon is rotated into the plane defined by teh [normal].
     * This is achieved by rotation the Y-axis into the direction of [normal].
     *
     *
     * ### Polygons
     * The ellipse is drawn as an approximation through a polygon with [segments] as side/edge number.
     *
     * Therefore, this method can also be used to render polygons if [segments] is set sufficiently low.
     * The polygon will be aligned with one corner in the direction defined by [angle].
     *
     * @param majorSemiaxis The length of the major semiaxis of the ellipse.
     * @param minor The length of the minor semiaxis of the ellipse.
     * @param outlineColor The color of the outline, transparency is supported. Set to null to not render the outline.
     * @param fillColor The fill color, transparency is supported. Set to null to not fill the circle.
     * @param segments The number of line / triangle segments used to approximate the circle.
     * @param lineWidth The outline width.
     * @param phase Makes the circle visible through blocks.
     *
     * @see drawCircle
     */
    fun drawEllipse(context: WorldRenderContext, xCenter: Float, yCenter: Float, zCenter: Float, majorSemiaxis: Float, minor: Float, angle: Float, normal: Vector3f, outlineColor: Color? = null, fillColor: Color? = null, segments: Int = 100, lineWidth: Float = 1f, phase: Boolean = false) {
        if (fillColor?.isVisible() != true && outlineColor?.isVisible() != true) return
        RenderSystem.assertOnRenderThread()

        val vec3d: Vec3d = context.worldState().cameraRenderState.pos
        val matrices = context.matrices()
        matrices.push()
        matrices.translate(-vec3d.getX(), -vec3d.getY(), -vec3d.getZ())

        // Translate to circle corresponding coordinate
        matrices.translate(xCenter,yCenter,zCenter)

        val rotationAxis = Vector3f(normal.normalize()).cross(0f,1f,0f)
        val angleZ = acos( normal.dot(0f,1f,0f) )
        if (angleZ > 0.0001f) {
            matrices.multiply(RotationAxis.of(rotationAxis).rotation(-angleZ))
        }
        if (angle > 0.0001f) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(angle))
        }
        // After this the z-axis is orthogonal to the ellipse.

        val positionMatrix: Matrix4f = matrices.peek().positionMatrix
        val normalMatrix = matrices.peek()
        val tessellator = Tessellator.getInstance()

        if (fillColor?.isVisible() == true) {
            fillEllipse(tessellator, positionMatrix, majorSemiaxis, minor, fillColor.rgb, segments, phase)
        }

        if (outlineColor?.isVisible() == true) {
            outlineEllipse(tessellator, positionMatrix, normalMatrix, majorSemiaxis, minor, outlineColor.rgb, segments, phase)
        }


        matrices.pop()
    }

    /**
     * Draws an outline for the block at the given [position].
     *
     * Both an outline at the edges and filled sides are possible.
     * If you do not want one of those to show set the corresponding color parameter to null.
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
    fun drawBlockBoundingBox(context: WorldRenderContext, position: BlockPos, outlineColor: Color? = null, fillColor: Color? = null, lineWidth: Float = 1f, phase: Boolean = false) {
        if (fillColor?.isVisible() != true && outlineColor?.isVisible() != true) return
        val state: BlockState = Mithras.mc.world?.getBlockState(position)?: return
        val shape = state.getOutlineShape(Mithras.mc.world, position, ShapeContext.of(Mithras.mc.player))
        if (shape.isEmpty) return
        val box = shape.boundingBox.offset(position)
        drawBox(context, box, outlineColor, fillColor, lineWidth, phase)
    }

    /**
     * Draws the bounding box of the given [entity].
     *
     * Both an outline at the edges and filled sides are possible.
     * If you do not want one of those to show set the corresponding color parameter to null.
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
    fun drawEntityBoundingBox(context: WorldRenderContext, entity: Entity, outlineColor: Color? = null, fillColor: Color? = null, lineWidth: Float = 1f, phase: Boolean = false) {
        val box = entity.boundingBox
        drawBox(context, box, outlineColor, fillColor, lineWidth, phase)
    }

    /**
     * Draws the given [box].
     *
     * Both an outline at the edges and filled sides are possible.
     * If you do not want one of those to show set the corresponding color parameter to null.
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
    fun drawBox(context: WorldRenderContext, box: Box, outlineColor: Color? = null, fillColor: Color? = null, lineWidth: Float = 1f, phase: Boolean = false) {
        if (outlineColor?.isVisible() == true && fillColor?.isVisible() != true) {
            drawBoxOutline(context,box, outlineColor, lineWidth, phase)
        }
        else if (outlineColor?.isVisible() != true && fillColor?.isVisible() == true) {
            drawFilledBox(context, box, fillColor, phase)
        }
        else if (outlineColor?.isVisible() == true && fillColor?.isVisible() == true) {
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
        if (!color.isVisible()) return
        RenderSystem.assertOnRenderThread()

        val vec3d: Vec3d = context.worldState().cameraRenderState.pos
        val matrices = context.matrices()
        matrices.push()
        matrices.translate(-vec3d.getX(), -vec3d.getY(), -vec3d.getZ())

        val positionMatrix: Matrix4f = matrices.peek().positionMatrix
        val normalMatrix = matrices.peek()

        val tessellator = Tessellator.getInstance()

        outlineBox(tessellator, positionMatrix, normalMatrix, x1, y1, z1, x2, y2, z2, color.rgb, lineWidth, phase)

        matrices.pop()
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
        if (!fillColor.isVisible()) return
        RenderSystem.assertOnRenderThread()

        val vec3d: Vec3d = context.worldState().cameraRenderState.pos
        val matrices = context.matrices()
        matrices.push()
        matrices.translate(-vec3d.getX(), -vec3d.getY(), -vec3d.getZ())
        val matrix4f: Matrix4f = matrices.peek().positionMatrix

        val tessellator = Tessellator.getInstance()

        fillSides(tessellator, matrix4f, x1, y1, z1, x2, y2, z2, fillColor.rgb, phase)

        matrices.pop()
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
        if (!fillColor.isVisible() && !outlineColor.isVisible()) return
        RenderSystem.assertOnRenderThread()

        val vec3d: Vec3d = context.worldState().cameraRenderState.pos
        val matrices = context.matrices()
        matrices.push()
        matrices.translate(-vec3d.getX(), -vec3d.getY(), -vec3d.getZ())
        val positionMatrix: Matrix4f = matrices.peek().positionMatrix
        val normalMatrix = matrices.peek()

        val tessellator = Tessellator.getInstance()

        if (fillColor.isVisible())
            fillSides(tessellator, positionMatrix, x1, y1, z1, x2, y2, z2, fillColor.rgb, phase)
        if (outlineColor.isVisible())
            outlineBox(tessellator, positionMatrix, normalMatrix, x1, y1, z1, x2, y2, z2, outlineColor.rgb, lineWidth, phase)

        matrices.pop()
    }

    private fun fillSides(tessellator: Tessellator, positionMatrix: Matrix4f, x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, color: Int, phase: Boolean) {
        val bufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)

        // Bottom side
        bufferBuilder.vertex(positionMatrix, x1, y1, z1).color(color)
        bufferBuilder.vertex(positionMatrix, x2, y1, z1).color(color)
        bufferBuilder.vertex(positionMatrix, x2, y1, z2).color(color)
        bufferBuilder.vertex(positionMatrix, x1, y1, z2).color(color)

        // Top side
        bufferBuilder.vertex(positionMatrix, x1, y2, z1).color(color)
        bufferBuilder.vertex(positionMatrix, x2, y2, z1).color(color)
        bufferBuilder.vertex(positionMatrix, x2, y2, z2).color(color)
        bufferBuilder.vertex(positionMatrix, x1, y2, z2).color(color)

        // West (-X) side
        bufferBuilder.vertex(positionMatrix, x1, y1, z1).color(color)
        bufferBuilder.vertex(positionMatrix, x1, y1, z2).color(color)
        bufferBuilder.vertex(positionMatrix, x1, y2, z2).color(color)
        bufferBuilder.vertex(positionMatrix, x1, y2, z1).color(color)

        // East (+X) side
        bufferBuilder.vertex(positionMatrix, x2, y1, z1).color(color)
        bufferBuilder.vertex(positionMatrix, x2, y1, z2).color(color)
        bufferBuilder.vertex(positionMatrix, x2, y2, z2).color(color)
        bufferBuilder.vertex(positionMatrix, x2, y2, z1).color(color)

        // North (-Z) side
        bufferBuilder.vertex(positionMatrix, x1, y1, z1).color(color)
        bufferBuilder.vertex(positionMatrix, x2, y1, z1).color(color)
        bufferBuilder.vertex(positionMatrix, x2, y2, z1).color(color)
        bufferBuilder.vertex(positionMatrix, x1, y2, z1).color(color)

        // South (+Z) side
        bufferBuilder.vertex(positionMatrix, x1, y1, z2).color(color)
        bufferBuilder.vertex(positionMatrix, x2, y1, z2).color(color)
        bufferBuilder.vertex(positionMatrix, x2, y2, z2).color(color)
        bufferBuilder.vertex(positionMatrix, x1, y2, z2).color(color)

        val builtBuffer = bufferBuilder.end()
        val layer = if (phase) RenderLayers.QUADS_PHASE else RenderLayers.QUADS
        layer.draw(builtBuffer)
    }

    private fun outlineBox(tessellator: Tessellator, positionMatrix: Matrix4f, normalMatrix: MatrixStack.Entry, x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, color: Int, lineWidth: Float, phase: Boolean) {
        val bufferBuilder = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH)
        // This rendering works by using 2 vertices per line, one for the start point and one for the end.
        // The normal goes along the line and is used for properly displaying the line width.

        // Bottom 4 edges
        bufferBuilder.vertex(positionMatrix, x1, y1, z1).color(color).normal(normalMatrix,1f,0f,0f).lineWidth(lineWidth)
        bufferBuilder.vertex(positionMatrix, x2, y1, z1).color(color).normal(normalMatrix,1f,0f,0f).lineWidth(lineWidth)
        bufferBuilder.vertex(positionMatrix, x2, y1, z1).color(color).normal(normalMatrix,0f,0f,1f).lineWidth(lineWidth)
        bufferBuilder.vertex(positionMatrix, x2, y1, z2).color(color).normal(normalMatrix,0f,0f,1f).lineWidth(lineWidth)
        bufferBuilder.vertex(positionMatrix, x2, y1, z2).color(color).normal(normalMatrix,-1f,0f,0f).lineWidth(lineWidth)
        bufferBuilder.vertex(positionMatrix, x1, y1, z2).color(color).normal(normalMatrix,-1f,0f,0f).lineWidth(lineWidth)
        bufferBuilder.vertex(positionMatrix, x1, y1, z2).color(color).normal(normalMatrix,0f,0f,-1f).lineWidth(lineWidth)
        bufferBuilder.vertex(positionMatrix, x1, y1, z1).color(color).normal(normalMatrix,0f,0f,-1f).lineWidth(lineWidth)

        // Top 4 edges
        bufferBuilder.vertex(positionMatrix, x1, y2, z1).color(color).normal(normalMatrix,1f,0f,0f).lineWidth(lineWidth)
        bufferBuilder.vertex(positionMatrix, x2, y2, z1).color(color).normal(normalMatrix,1f,0f,0f).lineWidth(lineWidth)
        bufferBuilder.vertex(positionMatrix, x2, y2, z1).color(color).normal(normalMatrix,0f,0f,1f).lineWidth(lineWidth)
        bufferBuilder.vertex(positionMatrix, x2, y2, z2).color(color).normal(normalMatrix,0f,0f,1f).lineWidth(lineWidth)
        bufferBuilder.vertex(positionMatrix, x2, y2, z2).color(color).normal(normalMatrix,-1f,0f,0f).lineWidth(lineWidth)
        bufferBuilder.vertex(positionMatrix, x1, y2, z2).color(color).normal(normalMatrix,-1f,0f,0f).lineWidth(lineWidth)
        bufferBuilder.vertex(positionMatrix, x1, y2, z2).color(color).normal(normalMatrix,0f,0f,-1f).lineWidth(lineWidth)
        bufferBuilder.vertex(positionMatrix, x1, y2, z1).color(color).normal(normalMatrix,0f,0f,-1f).lineWidth(lineWidth)

        // 4 Side edges
        bufferBuilder.vertex(positionMatrix, x1, y1, z1).color(color).normal(normalMatrix,0f,1f,0f).lineWidth(lineWidth)
        bufferBuilder.vertex(positionMatrix, x1, y2, z1).color(color).normal(normalMatrix,0f,1f,0f).lineWidth(lineWidth)
        bufferBuilder.vertex(positionMatrix, x2, y1, z1).color(color).normal(normalMatrix,0f,1f,0f).lineWidth(lineWidth)
        bufferBuilder.vertex(positionMatrix, x2, y2, z1).color(color).normal(normalMatrix,0f,1f,0f).lineWidth(lineWidth)
        bufferBuilder.vertex(positionMatrix, x1, y1, z2).color(color).normal(normalMatrix,0f,1f,0f).lineWidth(lineWidth)
        bufferBuilder.vertex(positionMatrix, x1, y2, z2).color(color).normal(normalMatrix,0f,1f,0f).lineWidth(lineWidth)
        bufferBuilder.vertex(positionMatrix, x2, y1, z2).color(color).normal(normalMatrix,0f,1f,0f).lineWidth(lineWidth)
        bufferBuilder.vertex(positionMatrix, x2, y2, z2).color(color).normal(normalMatrix,0f,1f,0f).lineWidth(lineWidth)

        val builtBuffer = bufferBuilder.end()
        val layer = if(phase) RenderLayers.LINES_PHASE else RenderLayers.LINES
        layer.draw(builtBuffer)
    }

    private fun outlineEllipse(tessellator: Tessellator, positionMatrix: Matrix4f, normalMatrix: MatrixStack.Entry, xRadius: Float, yRadius: Float, color: Int, segments: Int, phase: Boolean) {
        val bufferBuilder = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR_NORMAL)

        // Entries of the rotation matrix
        val segmentAngle = 2f * 3.1415925f / segments
        val c = cos(segmentAngle)
        val s = sin(segmentAngle)
        var t : Float

        // Vertex position
        var x = 1f
        var y = 0f

        for (ii in 0..segments) {
            bufferBuilder.vertex(positionMatrix, x * xRadius, 0f, y * yRadius).color(color).normal(normalMatrix, -y, 0f, x )

            // Matrix multiplication
            t = x
            x = c * x - s * y
            y = s * t + c * y
        }

        val builtBuffer = bufferBuilder.end()
        val layer = if(phase) RenderLayers.LINE_STRIP_PHASE else RenderLayers.LINE_STRIP
        layer.draw(builtBuffer)
    }

    private fun fillEllipse(tessellator: Tessellator, positionMatrix: Matrix4f, xRadius: Float, yRadius: Float, color: Int, segments: Int, phase: Boolean) {
        val bufferBuilder = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR)

        // Entries of the rotation matrix
        val segmentAngle = 2f * 3.1415925f / segments
        val c = cos(segmentAngle)
        val s = sin(segmentAngle)
        var t : Float

        // Vertex position
        var x = 1f
        var y = 0f

        // Center point of the fan
        bufferBuilder.vertex(positionMatrix, 0f, 0f, 0f).color(color)

        for (ii in 0..segments) {
            bufferBuilder.vertex(positionMatrix, x * xRadius, 0f, y * yRadius).color(color)

            // Matrix multiplication
            t = x
            x = c * x - s * y
            y = s * t + c * y
        }

        val builtBuffer = bufferBuilder.end()
        val layer = if (phase) RenderLayers.TRIANGLE_FAN_PHASE else RenderLayers.TRIANGLE_FAN
        layer.draw(builtBuffer)
    }

    /**
     * Checks whether the alpha value of this color is not 0.
     */
    private fun Color.isVisible(): Boolean = this.alpha != 0



}