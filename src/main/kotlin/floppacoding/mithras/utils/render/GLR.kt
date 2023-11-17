package floppacoding.mithras.utils.render

import com.google.common.collect.ImmutableMap
import com.mojang.blaze3d.systems.RenderSystem
import floppacoding.mithras.shaders.impl.*
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.*
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.RotationAxis
import org.apache.commons.lang3.tuple.MutablePair
import org.joml.*
import org.lwjgl.opengl.GL45.*
import org.lwjgl.system.MemoryUtil
import kotlin.math.*

// TODO consider not using the position matrix on the cpu when creating vertices and instead let the model view matrix handle that.
//   so instead of using VertexConsumer.vertex(matrix4f, x, y, z) using VertexConsumer(x,y,z)
//   and letting all transformation methods act on RenderSystem.getModelViewMatrix instead.
//   Or maybe even use a new matrix stack,  to prevent compatibility issues.
//
// TODO also consider adding option for the antialising, to disable / enable it or to change the samples.
//
// TODO also consider not using other global shader settings like line witdh.
//   pro of using global:  the result is consistent with vanilla rendering
//   con the state of vanilla rendering is changed.
//
//   Doing so is more efficient since it reduces teh cpu load. (might not be relevant tho)
//   Also consider better buffering so that everything which uses the same shader gets drawn at once.

// TODO Consider making the padding wider: 3 -5 texels maybe instead of just 2, or alternatively just less steep.
//  This may be used for special outline effects around characters

object GLR: Renderer2D {

    private val vaoBuilder = VAOBuilder2D()

    private var matrices: MatrixStack = MatrixStack()
    val projectionMatrix: Matrix4f = Matrix4f().setOrtho(0.0f, 1920f, 1080f, 0.0f, 1000.0f, 21000.0f)

    private val mainBuffer: Framebuffer = MinecraftClient.getInstance().framebuffer
    private var msaaBuffer = MSAAFrameBuffer(8, mainBuffer.textureWidth, mainBuffer.textureHeight)

    private val mc = MinecraftClient.getInstance()
    override val defaultFont: Font
        get() = GLFontManager.ROBOTO

    private val drawCalls: MutableList<RenderCall> = mutableListOf()

    private var requiredPrecision = 3f
    var useMSAA = true
        private set

    fun setMaxDeviation(deviation: Float) {
        requiredPrecision = abs(1/deviation)
    }

    fun useMSAA(use: Boolean) {
        useMSAA = use
    }

    fun changeMSAASamples(newSamples: Int) {
        msaaBuffer.delete()
        msaaBuffer = MSAAFrameBuffer(newSamples, mainBuffer.textureWidth, mainBuffer.textureHeight)
    }

    override fun beginFrame() {
        RenderSystem.disableCull()
        this.matrices = MatrixStack()
        projectionMatrix.setOrtho(0.0f, mc.window.framebufferWidth.toFloat(), mc.window.framebufferHeight.toFloat(), 0.0f, 1000.0f, -1000.0f)
        if(useMSAA) msaaBuffer.useAndCopyFrom(mainBuffer)
        drawCalls.clear()
        vaoBuilder.reset()
    }

    override fun beginFrame(context: DrawContext) {
        beginFrame()
        setTransform(context)
    }

    override fun setTransform(context: DrawContext) {
        matrices.loadIdentity()
        matrices.scale(mc.window.scaleFactor.toFloat(), mc.window.scaleFactor.toFloat(), 1f)
        matrices.peek().positionMatrix.mul(context.matrices.peek().positionMatrix)
        matrices.peek().normalMatrix.mul(context.matrices.peek().normalMatrix)
    }

    override fun endFrame() {
        flushDraw()

        if(useMSAA) msaaBuffer.copyBackTo(mainBuffer)
    }

    private fun flushDraw() {
        if(drawCalls.isEmpty()) return
        vaoBuilder.upload()
        // Set states
        NewShader.setProjectionMatrix(projectionMatrix)
        NewShader.useShader()
        RenderSystem.enableBlend()


        var unit: Int; var id: Int; var firstInBatch = 0; var lastInBatch: Int = drawCalls.size - 1; var call: RenderCall
        val textures: LinkedHashMap<Int, Int> = linkedMapOf()
        val textureBuffer = MemoryUtil.memAllocInt(32)
        do {
            //Bind as many of the required textures as possible
            unit = 0
            for (ii in firstInBatch until drawCalls.size) {
                call = drawCalls[ii]
                id = call.texture ?: continue

                call.textureUnit = textures.getOrPut(id) { unit++ }

                if (unit > 31) {
                    lastInBatch = ii
                    break
                }
            }
            if (textures.isNotEmpty()) {
                textureBuffer.limit(textures.keys.size)
                textureBuffer.put(0, textures.keys.toIntArray())
                glBindTextures(0, textureBuffer)
            }

            // Group consecutive render calls together when no state change is required.
            var startCall: RenderCall = drawCalls[firstInBatch]; var count: Int
            for (ii in firstInBatch .. lastInBatch) {
                call = drawCalls[ii]
                if( ii < lastInBatch && call.combinable(drawCalls[ii+1])) {
                    continue
                }
                NewShader.setColorMode(call.colorMode)
                NewShader.uploadColorMode()
                call.textureUnit?.let { NewShader.setTextureUnit(it); NewShader.uploadTextureUnit() }
                call.textAAWidth?.let { NewShader.setAAwidth(it); NewShader.uploadAAwidth() }

                count = call.indexRange.last - startCall.indexRange.first + 1
                // indices of glDrawElements is the offset in Bytes and not in indices of size defined by type!
                glDrawElements(GL_TRIANGLES, count, GL_UNSIGNED_INT, (startCall.indexRange.first * Int.SIZE_BYTES).toLong())
                if( ii < lastInBatch) startCall = drawCalls[ii + 1]
            }

            textures.clear()
            firstInBatch = lastInBatch + 1
        }while (firstInBatch < drawCalls.size)
        MemoryUtil.memFree(textureBuffer)
    }

    override fun reset() {
        matrices.loadIdentity()
    }

    /**
     * Translates the origin of the current coordinate system.
     */
    override fun translate(x: Float, y: Float) = matrices.translate(x, y, 0f)

    /**
     * Translates the origin of the current coordinate system.
     */
    override fun translate(x: Double, y: Double) = matrices.translate(x, y, 0.0)

    /**
     * Scales the current coordinate system.
     */
    override fun scale(x: Float, y: Float) = matrices.scale(x, y, 1f)

    /**
     * Rotates clockwise by the given [angle] in degrees.
     */
    override fun rotate(angle: Float) = matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle))

    /**
     * Pushes the current rendering state to a stack.
     * [pop] must be used to restore that state.
     */
    override fun push() = matrices.push()

    /**
     * Restores the previous rendering state.
     */
    override fun pop() = matrices.pop()

    override fun line(x1: Float, y1: Float, x2: Float, y2: Float, width: Float, color: Int, capStyle: CapStyle) {
        RenderSystem.assertOnRenderThread()
        RenderSystem.enableBlend()
        RenderSystem.lineWidth(width)
        Lines.setLinesMode()
        Lines.setCapStyle(capStyle)

        val positionMatrix = matrices.peek().positionMatrix
        val normalMatrix = matrices.peek().normalMatrix
        val bufferBuilder = RenderSystem.renderThreadTesselator().buffer
        bufferBuilder.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES)

        val lineNormal = Vector3f(x2-x1, y2-y1,0f).mul(normalMatrix).normalize()

        bufferBuilder.vertex(positionMatrix, x1, y1, 0f).color(color).normal(lineNormal.x, lineNormal.y, 0f).next()
        bufferBuilder.vertex(positionMatrix, x2, y2, 0f).color(color).normal(lineNormal.x, lineNormal.y, 0f).next()

        val builtBuffer = bufferBuilder.end()

        Lines.useShader()
        BufferRenderer.draw(builtBuffer)
        Lines.stopShader()
    }

    override fun rect(x: Float, y: Float, width: Float, height: Float, color: Int) {
        val positionMatrix = matrices.peek().positionMatrix
        vaoBuilder.begin()
        val x1 = x + width; val y1 = y+height

        vaoBuilder.vertex(positionMatrix,  x,  y).color(color).next()
        vaoBuilder.vertex(positionMatrix,  x, y1).color(color).next()
        vaoBuilder.vertex(positionMatrix, x1, y1).color(color).next()
        vaoBuilder.vertex(positionMatrix, x1,  y).color(color).next()

        val range = vaoBuilder.generateIndices(VAOBuilder2D.Mode.QUAD)
        drawCalls.add(RenderCall(range, RenderCall.ColorMode.COLOR))
    }

    /**
     * Draws a rectangle with rounded corners.
     */
    override fun roundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Int) {
        val positionMatrix = matrices.peek().positionMatrix
        val range =iterateRoundedRect(positionMatrix, x, y, x+width, y+height, radius) { position ->
            vaoBuilder.vertex(positionMatrix, position).color(color).next()
        }
        drawCalls.add(RenderCall(range, RenderCall.ColorMode.COLOR))
    }

    override fun roundedRect(x: Float, y: Float, width: Float, height: Float, radii: Vector4f, color: Int) {
        val positionMatrix = matrices.peek().positionMatrix
        val range = iterateRoundedRect(matrices.peek().positionMatrix, x, y, x+width, y+height, radii) { position ->
            vaoBuilder.vertex(positionMatrix, position).color(color).next()
        }
        drawCalls.add(RenderCall(range, RenderCall.ColorMode.COLOR))
    }

    fun fontAtlas(font: GLFontManager.GLFont, x: Float, y: Float) {
        RenderSystem.assertOnRenderThread()

        val width = 1024f
        val height = 2048f

        val u1 = 0f
        val u2 = 1f
        val v1 = 0f
        val v2 = 1f

        RenderSystem.enableBlend()
        font.bindFont()
        val positionMatrix = matrices.peek().positionMatrix
        val bufferBuilder = RenderSystem.renderThreadTesselator().buffer
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE)

        bufferBuilder.vertex(positionMatrix, x,             y,        0f).texture(u1, v1).next()
        bufferBuilder.vertex(positionMatrix, x,          y+height, 0f).texture(u1, v2).next()
        bufferBuilder.vertex(positionMatrix, x+width, y+height, 0f).texture(u2, v2).next()
        bufferBuilder.vertex(positionMatrix, x+width,    y,        0f).texture(u2, v1).next()

        Texture.setAlpha(1f)
        Texture.useShader()
        BufferRenderer.draw(bufferBuilder.end())
        Texture.stopShader()

    }

    override fun text(text: CharSequence, x: Float, y: Float, color: Int, fontSize: Float, font: Font, textAlign: TextAlign, splitWidth: Float?) {
        if (font !is GLFontManager.GLFont) throw return
        RenderSystem.assertOnRenderThread()

        val fontMetrics = font.fontMetrics

        val scale = fontSize/(fontMetrics.normalHeight)

        val lines = splitLines(text, font, splitWidth?.div(scale))

        var y0: Float; var y1: Float
        when(textAlign.vertical) {
            TextAlign.Vertical.TOP -> { y0 = -fontMetrics.topOffset; y1 =  y0 + fontMetrics.totalHeight }
            TextAlign.Vertical.MIDDLE -> {
                val mid = (fontMetrics.normalAscent - fontMetrics.normalDescent)/2
                y0 = - mid - fontMetrics.topOffset; y1 = mid - fontMetrics.bottomOffset
            }
            TextAlign.Vertical.BOTTOM -> {y1 = -fontMetrics.bottomOffset; y0 = y1 - fontMetrics.totalHeight}
            TextAlign.Vertical.BASELINE -> { y0 = -fontMetrics.ascent; y1 = -fontMetrics.descent }
        }
        lines.map {
            it.right = when(textAlign.horizontal) {
                TextAlign.Horizontal.LEFT -> 0f
                TextAlign.Horizontal.CENTER -> -it.right/2
                TextAlign.Horizontal.RIGHT -> -it.right
            }
        }

        y0 -= fontMetrics.padding
        y1 += fontMetrics.padding


        push()
        translate(x,y)
        scale(scale, scale)

        RenderSystem.enableBlend()
        font.bindFont()
        val positionMatrix = matrices.peek().positionMatrix
        val bufferBuilder = RenderSystem.renderThreadTesselator().buffer
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE)

        for (line in lines) {
            translate(line.right, 0f)
            drawLineInternal(bufferBuilder, line.left, y0, y1, font, color)
            translate(-line.right, fontMetrics.normalHeight)
        }

        TextShader.adjustAAwidth(positionMatrix)
        TextShader.useShader()
        BufferRenderer.draw(bufferBuilder.end())
        TextShader.stopShader()
        pop()
    }

    override fun textBox(text: CharSequence, x: Float, y: Float, color: Int, width: Float, fontSize: Float, font: Font, textAlign: TextAlign, boxAlign: TextAlign) {
        if (font !is GLFontManager.GLFont) throw return
        RenderSystem.assertOnRenderThread()

        val fontMetrics = font.fontMetrics

        val scale = fontSize/(fontMetrics.normalHeight)

        val lines = splitLines(text, font, width.div(scale))

        var y0: Float; var y1: Float
        when(textAlign.vertical) {
            TextAlign.Vertical.TOP -> { y0 = -fontMetrics.topOffset; y1 =  y0 + fontMetrics.totalHeight }
            TextAlign.Vertical.MIDDLE -> {
                val mid = (fontMetrics.normalAscent - fontMetrics.normalDescent)/2
                y0 = - mid - fontMetrics.topOffset; y1 = mid - fontMetrics.bottomOffset
            }
            TextAlign.Vertical.BOTTOM -> {y1 = -fontMetrics.bottomOffset; y0 = y1 - fontMetrics.totalHeight}
            TextAlign.Vertical.BASELINE -> { y0 = -fontMetrics.ascent; y1 = -fontMetrics.descent }
        }
        lines.map {
            it.right = when(textAlign.horizontal) {
                TextAlign.Horizontal.LEFT -> 0f
                TextAlign.Horizontal.CENTER -> -it.right/2
                TextAlign.Horizontal.RIGHT -> -it.right
            }
        }

        y0 -= fontMetrics.padding
        y1 += fontMetrics.padding


        push()
        translate(x,y)
        scale(scale, scale)
        val yShift = when(boxAlign.vertical) {
            TextAlign.Vertical.TOP -> 0f
            TextAlign.Vertical.MIDDLE -> -lines.size * (fontMetrics.lineHeight) / 2
            TextAlign.Vertical.BOTTOM -> -lines.size * (fontMetrics.lineHeight)
            TextAlign.Vertical.BASELINE -> -lines.size * (fontMetrics.lineHeight)
        }
        var xShift = when(boxAlign.horizontal) {
            TextAlign.Horizontal.LEFT -> 0f
            TextAlign.Horizontal.CENTER -> - width /2
            TextAlign.Horizontal.RIGHT -> -width
        }
        xShift += when(textAlign.horizontal) {
            TextAlign.Horizontal.LEFT -> 0f
            TextAlign.Horizontal.CENTER ->+ width /2
            TextAlign.Horizontal.RIGHT -> +width
        }
        translate(xShift/scale,yShift)

        RenderSystem.enableBlend()
        font.bindFont()
        val positionMatrix = matrices.peek().positionMatrix
        val bufferBuilder = RenderSystem.renderThreadTesselator().buffer
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE)

        for (line in lines) {
            translate(line.right, 0f)
            drawLineInternal(bufferBuilder, line.left, y0, y1, font, color)
            translate(-line.right, fontMetrics.normalHeight)
        }

        TextShader.adjustAAwidth(positionMatrix)
        TextShader.useShader()
        BufferRenderer.draw(bufferBuilder.end())
        TextShader.stopShader()
        pop()
    }

    override fun textLine(text: CharSequence, x: Float, y: Float, color: Int, fontSize: Float, font: Font, textAlign: TextAlign) {
        if (font !is GLFontManager.GLFont) return
        RenderSystem.assertOnRenderThread()

        val fontMetrics = font.fontMetrics

        val scale = fontSize/(fontMetrics.normalHeight)

        var y0: Float; var y1: Float
        when(textAlign.vertical) {
            TextAlign.Vertical.TOP -> { y0 = -fontMetrics.topOffset; y1 =  y0 + fontMetrics.totalHeight }
            TextAlign.Vertical.MIDDLE -> {
                val mid = (fontMetrics.normalAscent - fontMetrics.normalDescent)/2
                y0 = - mid - fontMetrics.topOffset; y1 = mid - fontMetrics.bottomOffset
            }
            TextAlign.Vertical.BOTTOM -> {y1 = -fontMetrics.bottomOffset; y0 = y1 - fontMetrics.totalHeight}
            TextAlign.Vertical.BASELINE -> { y0 = -fontMetrics.ascent; y1 = -fontMetrics.descent }
        }
        val offset = when(textAlign.horizontal) {
            TextAlign.Horizontal.LEFT -> 0f
            TextAlign.Horizontal.CENTER -> -textWidthInternal(text, font) /2
            TextAlign.Horizontal.RIGHT -> -textWidthInternal(text, font)
        }
        y0 -= fontMetrics.padding
        y1 += fontMetrics.padding

        push()
        translate(x,y)
        scale(scale, scale)

        RenderSystem.enableBlend()
        font.bindFont()
        val positionMatrix = matrices.peek().positionMatrix
        val bufferBuilder = RenderSystem.renderThreadTesselator().buffer
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE)


        translate(offset, 0f)
        drawLineInternal(bufferBuilder, text, y0, y1, font, color)


        TextShader.adjustAAwidth(positionMatrix)
        TextShader.useShader()
        BufferRenderer.draw(bufferBuilder.end())
        TextShader.stopShader()
        pop()
    }

    private fun splitLines(text: CharSequence, font: GLFontManager.GLFont,  splitWidth: Float?) : List<MutablePair<CharSequence, Float>> {
        val lines = mutableListOf<MutablePair<CharSequence, Float>>()
        if (splitWidth != null) {
            try {
                var width = 0f
                var advance: Float
                var jump = 0
                for ((index, char) in text.withIndex()) {
                    if (char == '\n') {
                        lines.add(MutablePair(text.subSequence(jump, index), width))
                        width = 0f
                        jump = index + 1
                        continue
                    }
                    advance = font.glyphMetrics[char]?.advance ?: 0f
                    if (width > splitWidth) {
                        lines.add(MutablePair(text.subSequence(jump, index - 1), width))
                        width = advance
                        jump = index
                        continue
                    }

                    width += advance
                }
            }catch (_: Exception) {
                return emptyList()
            }
        }else {
            text.split('\n').mapTo(lines){ MutablePair(it, textWidthInternal(it, font)) }
        }
        return  lines
    }

    private fun drawLineInternal(bufferBuilder: BufferBuilder, text: CharSequence, y0: Float, y1: Float, font: GLFontManager.GLFont, color: Int) {
        var x0: Float; var x1: Float; var pos = 0f
        var metrics: GLFontManager.GLFont.GlyphMetrics
        val positionMatrix = matrices.peek().positionMatrix
        for(char in text) {
            metrics = font.glyphMetrics[char] ?: continue
            x0 = pos+metrics.leftSiderBearing - font.fontMetrics.padding
            x1 = x0 + metrics.width
            bufferBuilder.vertex(positionMatrix, x0,y0,0f).color(color).texture(metrics.u0, metrics.v0).next()
            bufferBuilder.vertex(positionMatrix, x0,y1,0f).color(color).texture(metrics.u0, metrics.v1).next()
            bufferBuilder.vertex(positionMatrix, x1,y1,0f).color(color).texture(metrics.u1, metrics.v1).next()
            bufferBuilder.vertex(positionMatrix, x1,y0,0f).color(color).texture(metrics.u1, metrics.v0).next()
            pos += metrics.advance
        }

    }

    override fun textWidth(text: CharSequence, fontSize: Float, font: Font): Float {
        if (font !is GLFontManager.GLFont) throw Error("Invalid Font")
        return textWidthInternal(text, font) * fontSize / font.fontMetrics.normalHeight
    }

    private fun textWidthInternal(text: CharSequence, font: GLFontManager.GLFont): Float {
        var width = 0f
        for(char in text) {
            width += font.glyphMetrics[char]?.advance ?: 0f
        }
        return width
    }

    override fun textBounds(text: CharSequence, width: Float?, fontSize: Float, font: Font): BoundingBox {
        if (font !is GLFontManager.GLFont) throw Error("Invalid Font")
        var rows = 1
        var longestLine = 0f
        if (width != null) {
            var lineWidth = 0f
            var advance: Float
            for (char in text) {
                if (char == '\n') {
                    if (lineWidth > longestLine) longestLine = lineWidth
                    lineWidth = 0f
                    rows++
                    continue
                }
                advance = font.glyphMetrics[char]?.advance ?: 0f
                if (lineWidth > width) {
                    if (lineWidth > longestLine) longestLine = lineWidth
                    lineWidth = advance
                    rows++
                    continue
                }

                lineWidth += advance
            }

        }else {
            val lineLenghts = text.split('\n').map{ textWidthInternal(it, font) }
            rows = lineLenghts.size
            longestLine= lineLenghts.max()
        }
        longestLine *= fontSize/font.fontMetrics.normalHeight
        val height = rows * font.fontMetrics.normalHeight * fontSize/font.fontMetrics.normalHeight
        return BoundingBox(0f, 0f, longestLine, height)
    }

    override fun image(image: Image, x: Float, y: Float, width: Float, height: Float, imageX: Float, imageY: Float, imageWidth: Float, imageHeight: Float, alpha: Float) {
        val u0 = imageX / image.width
        val u1 = u0 + imageWidth / image.width
        var v0 = imageY / image.height
        var v1 = v0 + imageHeight / image.height
        if (image.flags.contains(Image.Flags.FLIPY)) {
            v0 = 1-v0
            v1 = 1-v1
        }
        val x1 = x + width; val y1 = y+height; val a = (alpha * 255).toInt()
        val positionMatrix = matrices.peek().positionMatrix
        vaoBuilder.begin()

        vaoBuilder.vertex(positionMatrix,  x,  y).alpha(a).texture(u0, v0).next()
        vaoBuilder.vertex(positionMatrix,  x, y1).alpha(a).texture(u0, v1).next()
        vaoBuilder.vertex(positionMatrix, x1, y1).alpha(a).texture(u1, v1).next()
        vaoBuilder.vertex(positionMatrix, x1,  y).alpha(a).texture(u1, v0).next()

        val range = vaoBuilder.generateIndices(VAOBuilder2D.Mode.QUAD)
        drawCalls.add(RenderCall(range, RenderCall.ColorMode.TEXTURE_ALPHA, image.id))
    }

    override fun roundedImage(image: Image, x: Float, y: Float, width: Float, height: Float, radius: Float, imageX: Float, imageY: Float, imageWidth: Float, imageHeight: Float, alpha: Float) {
        val u0 = imageX / image.width
        val u1 = u0 + imageWidth / image.width
        var v0 = imageY / image.height
        var v1 = v0 + imageHeight / image.height
        if (image.flags.contains(Image.Flags.FLIPY)) {
            v0 = 1-v0
            v1 = 1-v1
        }

        val uv0 = Vector2f(u0, v0)
        val uv1 = Vector2f(u1, v1)
        val r0 = Vector2f(x,y)
        val dimensions = Vector2f(width, height)
        val tex = Vector2f()

        val a = (alpha * 255).toInt()
        val positionMatrix = matrices.peek().positionMatrix
        vaoBuilder.begin()

        iterateRoundedRect(positionMatrix, x, y, x + width, y+height, radius) { position ->
            interpolateRectangeTex(r0, dimensions, position, uv0, uv1, tex)
            vaoBuilder.vertex(positionMatrix, position).alpha(a).texture(tex).next()
        }
        val range = vaoBuilder.generateIndices(VAOBuilder2D.Mode.TRIANGLE_FAN)
        drawCalls.add(RenderCall(range, RenderCall.ColorMode.TEXTURE_ALPHA, image.id))
    }

    override fun chromaBorder(x: Float, y: Float, width: Float, height: Float, lineWidth: Float, radius: Float, color: Int) {
        if (radius == 0f) {
            RectBorder.setChroma(true)
        }else {
            RoundedRectBorder.setChroma(true)
        }
        border(x, y, width, height, lineWidth, radius, color)
    }

    override fun border(x: Float, y: Float, width: Float, height: Float, lineWidth: Float, radii: Vector4f?, color: Int) {
        RenderSystem.assertOnRenderThread()
        RenderSystem.enableBlend()

        val positionMatrix = matrices.peek().positionMatrix
        val bufferBuilder = RenderSystem.renderThreadTesselator().buffer
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR)

        bufferBuilder.vertex(positionMatrix, x, y, 0f).color(color).next()
        bufferBuilder.vertex(positionMatrix, x, y+height, 0f).color(color).next()
        bufferBuilder.vertex(positionMatrix, x+width, y+height, 0f).color(color).next()
        bufferBuilder.vertex(positionMatrix, x+width, y, 0f).color(color).next()
        bufferBuilder.vertex(positionMatrix, x, y, 0f).color(color).next()

        if (radii == null || radii.x == 0f && radii.y == 0f && radii.z == 0f && radii.w == 0f) {
            RectBorder.setLineWidth(lineWidth)
            RectBorder.setTransform(positionMatrix)
            RectBorder.useShader()
            BufferRenderer.draw(bufferBuilder.end())
            RectBorder.stopShader()
        }else {
            RoundedRectBorder.setLineWidth(lineWidth)
            RoundedRectBorder.setRadii(radii)
            RoundedRectBorder.setTransform(positionMatrix)
            RoundedRectBorder.useShader()
            BufferRenderer.draw(bufferBuilder.end())
            RoundedRectBorder.stopShader()
        }

    }

    override fun textField(text: String, x: Float, y: Float, width: Float, color: Int, fontSize: Float, radius: Float, font: Font) {
        TODO("Not yet implemented")
    }

    fun circle(x: Float, y: Float, radius: Float, color: Int) {
        ellipse(x, y, Vector2f(radius, 0f), radius, color)
    }

    /**
     * Draws an ellipse centered at [[x],[y]] with semi-axes [a] and [b].
     *
     * @param a Is one of the semi-axes of the ellipse and determines the orientation of the ellipse.
     * @param b Is the length of the other semi-axis of the ellipse. It is oriented internally.
     */
    fun ellipse(x: Float, y: Float, a: Vector2f, b: Float, color: Int) {
        RenderSystem.assertOnRenderThread()

        val posMat = matrices.peek().positionMatrix
        val transform = Matrix2f(posMat.m00(), posMat.m10(), posMat.m01(), posMat.m11())

        val aVec = a.mul(transform)
        val bVec = Vector2f(-a.y, a.x).normalize(b).mul(transform)
        RenderSystem.enableBlend()

        val bufferBuilder = RenderSystem.renderThreadTesselator().buffer
        bufferBuilder.begin(POINTS, POSITION_COLOR_TEX_TEX)

        bufferBuilder.vertex(posMat, x, y, 0f).color(color).texture(aVec.x, aVec.y).texture(bVec.x, bVec.y).next()
//        bufferBuilder.vertex(positionMatrix, x, y, 0f).color(color).texture(aVec.x, aVec.y).texture(bVec.x, bVec.y).next()

        Ellipse.useShader()
        BufferRenderer.draw(bufferBuilder.end())
        Ellipse.stopShader()
    }

    /**
     * Sets up a scissor rectangle.
     *
     * The coordinates are assumed to be in the current coordinate space and are transformed accordingly.
     * The scissor rectangle will be aligned with the screen coordinate system and will be the bounding box of the given possibly
     * rotated rectangle. If the axis of the current coordinate system are not aligned with screen coordinates the scissor
     * will set up a rectangle *ABCD* as shown in the following example.
     *
     *         A      (x+width,y)  B
     *          ┌─────────────╳───┐
     *          │      __──‾‾  ╲  │
     *    (x,y) │__──‾‾          ╲│ (x+width,y+height)
     *          │╲          __──‾‾│
     *          │  ╲  __──‾‾      │
     *          └───╳─────────────┘
     *         D    (x,y+height)   C
     *
     *
     */
    override fun scissor(x: Float, y: Float, width: Float, height: Float) {
        val bbox = getAbsoluteBoundingBox(x, y, width, height)

        RenderSystem.enableScissor(
            round(bbox.xmin).toInt(),
            round(mc.window.height - bbox.ymax).toInt(),
            round(bbox.width()).toInt(),
            round(bbox.height()).toInt()
        )
    }

    /**
     * Disables scissoring.
     */
    override fun endScissor() = RenderSystem.disableScissor()

    val POSITION_COLOR_TEX_TEX = VertexFormat(ImmutableMap.builder<String, VertexFormatElement>().put("Position", VertexFormats.POSITION_ELEMENT).put("Color", VertexFormats.COLOR_ELEMENT).put("UV0", VertexFormats.TEXTURE_ELEMENT).put("UV1", VertexFormats.TEXTURE_ELEMENT).build())
    val POINTS = VertexFormat.DrawMode.valueOf("POINTS")

    /**
     * Interpolates texture coordinates for the given [position] inside the rectangle defined by its origin [r0] with
     * the given [dimensions]. [uv0] is assumed to be the texture coordiante at [r0] and [uv1] the texture coordinate at
     * [r0] + [dimensions]. The result is written into [dest] and returned.
     */
    private fun interpolateRectangeTex(r0: Vector2f, dimensions: Vector2f, position: Vector2f, uv0: Vector2f, uv1: Vector2f, dest: Vector2f): Vector2f {
        return lerp(uv0, uv1,
                relativeRectangleCoordinates(r0, dimensions, position, dest),
                dest
            )
    }

    /**
     * Determines relative coordinates of [position] in a rectangle with origin [r0] and the given [dimensions].
     * The result is written into [dest] and returned.
     * The relative coordinates will range from 0 to 1 when [position] is in the rectangle and the [dimensions] are positive.
     */
    private fun relativeRectangleCoordinates(r0: Vector2f, dimensions: Vector2f, position: Vector2f, dest: Vector2f): Vector2f {
        return dest.set(position).sub(r0).div(dimensions)
    }

    /**
     * Component wise linear interpolation between [a] and [b] with [t].
     * The result is written into [dest] and returned.
     * [t] and [dest] are allowed to be identical.
     */
    private fun lerp(a: Vector2f, b: Vector2f, t: Vector2f, dest: Vector2f ): Vector2f {
        dest.x = Math.fma(b.x - a.x, t.x, a.x )
        dest.y = Math.fma(b.y - a.y, t.y, a.y )
        return dest
    }

    private fun iterateRoundedRect(positionMatrix: Matrix4f, x0: Float, y0: Float, x1: Float, y1: Float, radius: Float, vertexGenerator: (Vector2f) -> Unit): IntRange {
        val midPoints: List<Vector2f> = listOf(
                Vector2f(x0,y0).fma(radius, CORNER_OFFSETS[0]),
                Vector2f(x0,y1).fma(radius, CORNER_OFFSETS[1]),
                Vector2f(x1,y1).fma(radius, CORNER_OFFSETS[2]),
                Vector2f(x1,y0).fma(radius, CORNER_OFFSETS[3])
        )

        val size = getScale(positionMatrix) * radius
        val segments = ceil(circleSegments(size) / 4f).toInt()

        val directions = if (segments  == 0) listOf(
                Vector2f( -ONE_OVER_SQRT_2, -ONE_OVER_SQRT_2).mul(radius),
                Vector2f(-ONE_OVER_SQRT_2,  ONE_OVER_SQRT_2).mul(radius),
                Vector2f( ONE_OVER_SQRT_2,  ONE_OVER_SQRT_2).mul(radius),
                Vector2f( ONE_OVER_SQRT_2,  -ONE_OVER_SQRT_2).mul(radius),
        )
        else listOf(
                Vector2f( 0f, -1f).mul(radius),
                Vector2f(-1f,  0f).mul(radius),
                Vector2f( 0f,  1f).mul(radius),
                Vector2f( 1f,  0f).mul(radius),
        )

        // Rotation matrix
        val segmentAngle = PI_HALF / segments
        val c = cos(segmentAngle)
        val s = sin(segmentAngle)
        val rotationMatrix = Matrix2f(c, -s, s, c)
        val position = Vector2f()

        vaoBuilder.begin()
        for (corner in 0..3) {
            for (ii in 0 ..segments){
                vertexGenerator(position.set(midPoints[corner]).add(directions[corner]))
                directions[corner].mul(rotationMatrix)
            }
        }
        return vaoBuilder.generateIndices(VAOBuilder2D.Mode.TRIANGLE_FAN)
    }

    private fun iterateRoundedRect(positionMatrix: Matrix4f, x0: Float, y0: Float, x1: Float, y1: Float, radii: Vector4f, vertexGenerator: (Vector2f) -> Unit): IntRange {
        val midPoints: List<Vector2f> = listOf(
                Vector2f(x0,y0).fma(radii.x, CORNER_OFFSETS[0]),
                Vector2f(x0,y1).fma(radii.y, CORNER_OFFSETS[1]),
                Vector2f(x1,y1).fma(radii.z, CORNER_OFFSETS[2]),
                Vector2f(x1,y0).fma(radii.w, CORNER_OFFSETS[3])
        )
        val scale = getScale(positionMatrix)
        var segmentAngle: Float
        var c: Float; var s: Float
        var rotationMatrix: Matrix2f
        val direction = Vector2f(); val position = Vector2f()
        var segments: Int

        vaoBuilder.begin()
        for (corner in 0..3) {
            segments = ceil(circleSegments(radii[corner] * scale) / 4f).toInt()
            direction.set(if (segments == 0) DIAGONALS[corner] else ORTHOGONALS[corner] ).mul(radii[corner])
            segmentAngle = PI_HALF / segments
            c = cos(segmentAngle)
            s = sin(segmentAngle)
            rotationMatrix = Matrix2f(c, -s, s, c)
            for (ii in 0 ..segments){
                vertexGenerator(position.set(midPoints[corner]).add(direction))
                direction.mul(rotationMatrix)
            }
        }
        return vaoBuilder.generateIndices(VAOBuilder2D.Mode.TRIANGLE_FAN)
    }

    /**
     * Returns the bounding box of the given rectangle in screen coordinates to be used with the Scissor test.
     *
     * The given input coordinates are assumed to span a rectangle in the current coordinate system.
     * The returned bounding box is axis aligned with the window coordinate system.
     *
     */
    private fun getAbsoluteBoundingBox(x: Float, y: Float, width: Float, height: Float): BoundingBox {
        val mat = matrices.peek().positionMatrix
        val transform = Matrix3f(mat).m20(mat.m30()).m21(mat.m31()).m22(1f)//.m02(0f).m12(0f) // These values will anyway be discarded.

        val corner = Vector3f()

        corner.set(x, y, 1f).mul(transform)
        val boundingBox = BoundingBox(corner.x, corner.y, corner.x, corner.y)

        listOf(0 to 1, 1 to 1, 1 to 0).forEach {
            corner.set(x+width*it.first, y + height * it.second, 1f).mul(transform)

            if (corner.x < boundingBox.xmin) boundingBox.xmin = corner.x
            else if (corner.x > boundingBox.xmax) boundingBox.xmax = corner.x
            if (corner.y < boundingBox.ymin) boundingBox.ymin = corner.y
            else if (corner.y > boundingBox.ymax) boundingBox.ymax = corner.y
        }

        return boundingBox
    }

    /**
     * for the given local transform this returns the average scale factor by which lengths will be distorted.
     * Use this to estimate the length in pixels of an object with arbitrary rotation on the screen.
     */
    private fun getScale(posMat: Matrix4f) : Float {
        return sqrt((posMat.m00()*posMat.m00() + posMat.m10()*posMat.m10() + posMat.m01()*posMat.m01() + posMat.m11()*posMat.m11())*0.5f)
    }

    /**
     * for the given local transform this returns the average scale factor by which lengths will be distorted.
     * Use this to estimate the length in pixels of an object with arbitrary rotation on the screen.
     */
    private fun getScale(posMat: Matrix3f) : Float {
        return sqrt((posMat.m00()*posMat.m00() + posMat.m10()*posMat.m10() + posMat.m01()*posMat.m01() + posMat.m11()*posMat.m11())*0.5f)
    }

    /**
     * Returns the number of segments to approximatea a circle so that it does not deviate more than the values set by
     * [setMaxDeviation] from the desired radius.
     * @param radius The expected radius of the circle in pixels.
     */
    private fun circleSegments(radius: Float): Int {
        // Analytical solution for the number of segments so that a regular polygon of radius r
        // (distance from center to the vertices) has a maximum deviation (in the middle of the segments) of h from a circle is:
        // N = pi / acos(1-h/r). This can be approximated really well by N = 2.21*sqrt(r/h)
        return ( round(2.21*sqrt(radius * requiredPrecision))).toInt()
    }

    private const val ONE_OVER_SQRT_2 = 0.7071068f
    private const val PI_HALF = 1.5707963f
    private const val PI_QUATER = 0.7853982f

    /**
     * Directions from rectangle corners to the center of the circle required for rounded corners.
     * In the order top-left, bottom-left, bottom-right, top-right.
     */
    private val CORNER_OFFSETS = listOf(
        Vector2f(1f, 1f),
        Vector2f(1f, -1f),
        Vector2f(-1f, -1f),
        Vector2f(-1f, 1f)
    )

    private val DIAGONALS = listOf(
        Vector2f(-ONE_OVER_SQRT_2, -ONE_OVER_SQRT_2),
        Vector2f(-ONE_OVER_SQRT_2,  ONE_OVER_SQRT_2),
        Vector2f( ONE_OVER_SQRT_2,  ONE_OVER_SQRT_2),
        Vector2f( ONE_OVER_SQRT_2, -ONE_OVER_SQRT_2)
    )

    private val ORTHOGONALS = listOf(
        Vector2f( 0f, -1f),
        Vector2f(-1f,  0f),
        Vector2f( 0f,  1f),
        Vector2f( 1f,  0f)
    )
}