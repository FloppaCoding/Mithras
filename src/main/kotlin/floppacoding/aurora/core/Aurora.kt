package floppacoding.aurora.core

import floppacoding.aurora.core.font.AuroraFontRenderer
import floppacoding.aurora.core.font.FontRender2D
import floppacoding.aurora.core.images.Image
import floppacoding.aurora.core.shader.impl.MainShader
import org.joml.*
import org.lwjgl.opengl.GL46.*
import org.lwjgl.system.MemoryStack
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.math.*

/**
 * # Aurora - 2D Rendering Library Implementation with OpenGL
 *
 * This is the OpenGl 4.5 implementation of the [Renderer2D] interface.
 *
 * @author Aton
 */
object Aurora: Renderer2D, FontRender2D by AuroraFontRenderer {
    var runDirectory: String? = null
    override var matrices: MatrixStack2D = MatrixStack2D()
        private set
    override val vaoBuilder = VAOBuilder2D()
    internal val projectionMatrix: Matrix4f = Matrix4f()
    internal var mainBuffer: FrameBuffer = ResizableFrameBufferReference(0, 640, 480)
    private var msaaBuffer = MSAAFrameBuffer(8, mainBuffer.width, mainBuffer.height)

    // States
    /**
     * Do not access this directly!
     * Use [addDrawCall] or [getLastDrawCall] instead.
     */
    private val drawCalls: MutableList<RenderCall> = mutableListOf()
    private var requiredPrecision = 4f
    var useMSAA = true
        private set
    private var scissorBox: BoundingBox? = null
    private var pausedScissorBox: BoundingBox? = null

    override fun setMaxDeviation(deviation: Float) {
        requiredPrecision = abs(1/deviation)
    }

    override fun useMSAA(use: Boolean) {
        useMSAA = use
    }

    override fun setMSAASamples(samples: Int) {
        if (samples == msaaBuffer.samples) return
        msaaBuffer.delete()
        msaaBuffer = MSAAFrameBuffer(samples, mainBuffer.width, mainBuffer.height)
    }

    override fun addDrawCall(call: RenderCall): RenderCall {
        if (call.indexRange.isEmpty()) return call
        scissorBox?.let { call.scissorBox = scissorBox }
        drawCalls.add(call)
        return call
    }

    override fun getLastDrawCall(): RenderCall? {
        return drawCalls.lastOrNull()
    }

    override fun setMainBufferReference(fbo: Int, widthGetter: () -> Int, heightGetter: () -> Int) {
        setMainBuffer( FrameBufferReference(fbo, widthGetter, heightGetter))
    }

    override fun setMainBufferId(fbo: Int) {
        setMainBuffer(ResizableFrameBufferReference(fbo, mainBuffer.width, mainBuffer.height))
    }

    override fun setMainBuffer(buffer: FrameBuffer) {
        mainBuffer = buffer
    }

    /**
     * Sets the dimensions of the window and framebuffer.
     */
    override fun setDimensions(width: Int, height: Int) {
        mainBuffer.width = width
        mainBuffer.height = height
    }

    override fun beginFrame() {
        matrices.clear()
        drawCalls.clear()
        vaoBuilder.reset()
    }

    override fun endFrame() {
        super.endFrame()
        if(drawCalls.isEmpty()) return

        if(useMSAA)
            msaaBuffer.useAndCopyFrom(mainBuffer)
        else
            mainBuffer.use()
        flushDraw()
        if(useMSAA) msaaBuffer.copyBackTo(mainBuffer)
    }

    private fun flushDraw() {
        projectionMatrix.setOrtho(0.0f, mainBuffer.width.toFloat(), mainBuffer.height.toFloat(), 0.0f, 1000.0f, -1000.0f)
        vaoBuilder.upload()
        // Set states
        MainShader.useShader()
        GLStateTracker.setupState()
        var scissoring = false

        // OpenGL4 should support at least 80 texture units. This should be sufficient for now.
        val avaliableUnits = 32
        val unitShift = 10 // This prevents already bound textures from being affected. Only works if no more than 10 are in use. But easier fix than restoring them all.

        MemoryStack.stackPush().use { stack ->
            var unit: Int; var id: Int; var firstInBatch = 0; var lastInBatch: Int = drawCalls.size - 1; var call: RenderCall
            val textures: LinkedHashMap<Int, Int> = linkedMapOf()

            val textureBuffer = stack.mallocInt(avaliableUnits)
            do {
                //Bind as many of the required textures as possible
                unit = 0
                for (ii in firstInBatch until drawCalls.size) {
                    call = drawCalls[ii]
                    id = call.texture ?: continue

                    call.textureUnit = textures.getOrPut(id) { unitShift + unit++ }
                    if (unit > avaliableUnits-1) {
                        lastInBatch = ii
                        break
                    }
                }
                if (textures.isNotEmpty()) {
                    textureBuffer.limit(textures.keys.size)
                    textureBuffer.put(0, textures.keys.toIntArray())
                    glBindTextures(unitShift, textureBuffer)
                }

                // Group consecutive render calls together when no state change is required.
                var startCall: RenderCall = drawCalls[firstInBatch]; var count: Int
                for (ii in firstInBatch .. lastInBatch) {
                    call = drawCalls[ii]
                    if( ii < lastInBatch && call.isCombinable(drawCalls[ii+1])) {
                        continue
                    }

                    call.uploadUniforms()
                    val scissorBox = call.scissorBox
                    if (scissorBox!= null) {
                        if (!scissoring) {
                            scissoring = true
                            glEnable(GL_SCISSOR_TEST)

                        }
                        glScissor(scissorBox.xmin.toInt(), scissorBox.ymin.toInt(), scissorBox.width().toInt(), scissorBox.height().toInt())
                    } else {
                        if (scissoring) {
                            scissoring = false
                            glDisable(GL_SCISSOR_TEST)
                        }
                    }

                    count = call.indexRange.last - startCall.indexRange.first + 1
                    // indices of glDrawElements is the offset in Bytes and not in indices of size defined by type!
                    glDrawElements(GL_TRIANGLES, count, GL_UNSIGNED_INT, (startCall.indexRange.first * Int.SIZE_BYTES).toLong())
                    if( ii < lastInBatch) startCall = drawCalls[ii + 1]
                }

                textures.clear()
                firstInBatch = lastInBatch + 1
            }while (firstInBatch < drawCalls.size)
        }

        // Restore states
        if (scissoring) glDisable(GL_SCISSOR_TEST)
        GLStateTracker.restoreState()
    }

    override fun cancelFrame() {
        drawCalls.clear()
        vaoBuilder.reset()
    }

    override fun reset() {
        matrices.loadIdentity()
    }

    override fun translate(x: Float, y: Float) = matrices.translate(x, y)

    override fun scale(x: Float, y: Float) = matrices.scale(x, y)

    override fun rotate(angle: Float) = matrices.rotate(angle * DEG_TO_RAD)

    override fun rotateRadians(angle: Float) = matrices.rotate(angle)

    override fun push() = matrices.push()

    override fun pop() = matrices.pop()

    override fun line(x0: Float, y0: Float, x1: Float, y1: Float, width: Float, color: Int, capStyle: CapStyle): RenderCall {
        var n0 = y1 - y0
        var n1 = x0 - x1
        val scale = width / (2*sqrt(Math.fma(n0,n0, n1*n1)))
        n0 *= scale
        n1 *= scale

        val positionMatrix = matrices.peek()

        val range: IntRange = when(capStyle) {
            CapStyle.FLAT -> {
                vaoBuilder.begin()

                vaoBuilder.vertex(positionMatrix, x0 + n0, y0 + n1).color(color).next()
                vaoBuilder.vertex(positionMatrix, x0 - n0, y0 - n1).color(color).next()
                vaoBuilder.vertex(positionMatrix, x1 + n0, y1 + n1).color(color).next()
                vaoBuilder.vertex(positionMatrix, x1 - n0, y1 - n1).color(color).next()

                vaoBuilder.generateIndices(VAOBuilder2D.Mode.TRIANGLE_STRIP)
            }
            CapStyle.ROUND -> {
                val segments  = ceil(circleSegments(getScale(positionMatrix) * width /2) / 2f).toInt()
                val segmentAngle = PI / segments
                val c = cos(segmentAngle)
                val s = sin(segmentAngle)
                val rotationMatrix = Matrix2f(c, -s, s, c)
                val position = Vector2f(n0, n1)
                vaoBuilder.begin()
                for (ii in 0 .. segments) {
                    vaoBuilder.vertex(positionMatrix, x0 + position.x, y0 + position.y).color(color).next()
                    position.mul(rotationMatrix)
                }
                position.set(-n0, -n1)
                for (ii in 0 .. segments) {
                    vaoBuilder.vertex(positionMatrix, x1 + position.x, y1 + position.y).color(color).next()
                    position.mul(rotationMatrix)
                }
                vaoBuilder.generateIndices(VAOBuilder2D.Mode.TRIANGLE_FAN)
            }
        }
        return addDrawCall(RenderCall(range, RenderCall.ColorMode.COLOR))
    }

    override fun rect(x: Float, y: Float, width: Float, height: Float, color: Int): RenderCall {
        val positionMatrix = matrices.peek()
        vaoBuilder.begin()
        val x1 = x + width; val y1 = y+height

        vaoBuilder.vertex(positionMatrix,  x,  y).color(color).texture(0f, 0f).next()
        vaoBuilder.vertex(positionMatrix,  x, y1).color(color).texture(0f, 1f).next()
        vaoBuilder.vertex(positionMatrix, x1, y1).color(color).texture(1f, 1f).next()
        vaoBuilder.vertex(positionMatrix, x1,  y).color(color).texture(0f, 0f).next()

        val range = vaoBuilder.generateIndices(VAOBuilder2D.Mode.QUADS)
        return addDrawCall(RenderCall(range, RenderCall.ColorMode.COLOR))
    }

    override fun roundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Int): RenderCall {
        val positionMatrix = matrices.peek()

        val uv0 = Vector2f(0f, 0f)
        val uv1 = Vector2f(1f, 1f)
        val r0 = Vector2f(x,y)
        val dimensions = Vector2f(width, height)
        val tex = Vector2f()

        vaoBuilder.begin()
        iterateRoundedRect(positionMatrix, x, y, x+width, y+height, radius) { position, _, _ ->
            interpolateRectangleTex(r0, dimensions, position, uv0, uv1, tex)
            vaoBuilder.vertex(positionMatrix, position).color(color).texture(tex).next()
        }
        val range = vaoBuilder.generateIndices(VAOBuilder2D.Mode.TRIANGLE_FAN)
        return addDrawCall(RenderCall(range, RenderCall.ColorMode.COLOR))
    }

    override fun roundedRect(x: Float, y: Float, width: Float, height: Float, radii: Vector4f, color: Int): RenderCall {
        val positionMatrix = matrices.peek()

        val uv0 = Vector2f(0f, 0f)
        val uv1 = Vector2f(1f, 1f)
        val r0 = Vector2f(x,y)
        val dimensions = Vector2f(width, height)
        val tex = Vector2f()

        vaoBuilder.begin()
        iterateRoundedRect(matrices.peek(), x, y, x+width, y+height, radii) { position, _, _ ->
            interpolateRectangleTex(r0, dimensions, position, uv0, uv1, tex)
            vaoBuilder.vertex(positionMatrix, position).color(color).texture(tex).next()
        }
        val range = vaoBuilder.generateIndices(VAOBuilder2D.Mode.TRIANGLE_FAN)
        return addDrawCall(RenderCall(range, RenderCall.ColorMode.COLOR))
    }

    override fun image(image: Image, x: Float, y: Float, width: Float, height: Float, imageX: Float, imageY: Float, imageWidth: Float, imageHeight: Float, alpha: Float): RenderCall {
        val (u0, v0, u1, v1) = getTextureUVs(image, imageX, imageY, imageWidth, imageHeight)
        val x1 = x + width; val y1 = y+height; val a = (alpha * 255).toInt()
        val positionMatrix = matrices.peek()
        vaoBuilder.begin()

        vaoBuilder.vertex(positionMatrix,  x,  y).alpha(a).texture(u0, v0).next()
        vaoBuilder.vertex(positionMatrix,  x, y1).alpha(a).texture(u0, v1).next()
        vaoBuilder.vertex(positionMatrix, x1, y1).alpha(a).texture(u1, v1).next()
        vaoBuilder.vertex(positionMatrix, x1,  y).alpha(a).texture(u1, v0).next()

        val range = vaoBuilder.generateIndices(VAOBuilder2D.Mode.QUADS)
        return addDrawCall(RenderCall(range, RenderCall.ColorMode.TEXTURE_ALPHA, image.glID))
    }

    override fun roundedImage(image: Image, x: Float, y: Float, width: Float, height: Float, radius: Float, imageX: Float, imageY: Float, imageWidth: Float, imageHeight: Float, alpha: Float): RenderCall {
        val texCoords = getTextureUVs(image, imageX, imageY, imageWidth, imageHeight)
        val uv0 = texCoords.uv0
        val uv1 = texCoords.uv1
        val r0 = Vector2f(x,y)
        val dimensions = Vector2f(width, height)
        val tex = Vector2f()

        val a = (alpha * 255).toInt()
        val positionMatrix = matrices.peek()
        vaoBuilder.begin()

        iterateRoundedRect(positionMatrix, x, y, x + width, y+height, radius) { position, _, _ ->
            interpolateRectangleTex(r0, dimensions, position, uv0, uv1, tex)
            vaoBuilder.vertex(positionMatrix, position).alpha(a).texture(tex).next()
        }
        val range = vaoBuilder.generateIndices(VAOBuilder2D.Mode.TRIANGLE_FAN)
        return addDrawCall(RenderCall(range, RenderCall.ColorMode.TEXTURE_ALPHA, image.glID))
    }

    override fun chromaBorder(x: Float, y: Float, width: Float, height: Float, lineWidth: Float, radius: Float, color: Int): RenderCall {
        return border(x, y, width, height, lineWidth, radius, color).setColorMode(RenderCall.ColorMode.CHROMA_ALPHA)
    }

    override fun border(x: Float, y: Float, width: Float, height: Float, lineWidth: Float, color: Int): RenderCall {
        val positionMatrix = matrices.peek()
        val x1 = x + width; val y1 = y+height
        val hw = lineWidth / 2
        vaoBuilder.begin()
        // top left
        vaoBuilder.vertex(positionMatrix,  x+hw,  y+hw).color(color).texture(0f, 0f).next()
        vaoBuilder.vertex(positionMatrix,  x-hw,  y-hw).color(color).texture(1f, 0f).next()
        // bottom left
        vaoBuilder.vertex(positionMatrix,  x+hw, y1-hw).color(color).texture(0f, 0f).next()
        vaoBuilder.vertex(positionMatrix,  x-hw, y1+hw).color(color).texture(1f, 0f).next()
        // bottom right
        vaoBuilder.vertex(positionMatrix, x1-hw, y1-hw).color(color).texture(0f, 0f).next()
        vaoBuilder.vertex(positionMatrix, x1+hw, y1+hw).color(color).texture(1f, 0f).next()
        // top right
        vaoBuilder.vertex(positionMatrix, x1-hw,  y+hw).color(color).texture(0f, 0f).next()
        vaoBuilder.vertex(positionMatrix, x1+hw,  y-hw).color(color).texture(1f, 0f).next()
        // top left
        vaoBuilder.vertex(positionMatrix,  x+hw,  y+hw).color(color).texture(0f, 0f).next()
        vaoBuilder.vertex(positionMatrix,  x-hw,  y-hw).color(color).texture(1f, 0f).next()

        val range = vaoBuilder.generateIndices(VAOBuilder2D.Mode.TRIANGLE_STRIP)
        return addDrawCall(RenderCall(range, RenderCall.ColorMode.COLOR))
    }

    override fun border(x: Float, y: Float, width: Float, height: Float, lineWidth: Float, radius: Float, color: Int): RenderCall {
        if (radius == 0f) {
            return border(x, y, width, height, lineWidth, color)
        }
        val positionMatrix = matrices.peek()
        val hw = lineWidth / 2
        val midPoints: List<Vector2f> = if (hw > radius) {
            listOf(
                Vector2f(x,y).fma(hw, CORNER_OFFSETS[0]),
                Vector2f(x,y+height).fma(hw, CORNER_OFFSETS[1]),
                Vector2f(x+width,y+height).fma(hw, CORNER_OFFSETS[2]),
                Vector2f(x+width,y).fma(hw, CORNER_OFFSETS[3])
            )
        }else emptyList()
        var first = true; val firstPos = Vector2f(); val firstNormal = Vector2f()
        vaoBuilder.begin()
        // iterates over the outer points of the border.
        iterateRoundedRect(positionMatrix, x-hw, y-hw, x+width+hw, y+height+hw, radius + hw) { position, normal, corner ->
            if (first) {
                firstPos.set(position); firstNormal.set(normal); first = false
            }
            // inner point
            if (hw > radius) {
                vaoBuilder.vertex(positionMatrix, midPoints[corner]).color(color).texture(0f, 0f).next()
            } else {
                vaoBuilder.vertex(positionMatrix, Math.fma(normal.x, -lineWidth, position.x), Math.fma(normal.y, -lineWidth, position.y)).color(color).texture(0f, 0f).next()
            }
            // outer point
            vaoBuilder.vertex(positionMatrix, position).color(color).texture(1f, 0f).next()
        }
        // Close loop, by going back to first.
        if (hw > radius) {
            vaoBuilder.vertex(positionMatrix, midPoints[0]).color(color).texture(0f, 0f).next()
        } else {
            vaoBuilder.vertex(positionMatrix, Math.fma(firstNormal.x, -lineWidth, firstPos.x), Math.fma(firstNormal.y, -lineWidth, firstPos.y)).color(color).texture(0f, 0f).next()
        }
        vaoBuilder.vertex(positionMatrix, firstPos).color(color).texture(1f, 0f).next()
        val range = vaoBuilder.generateIndices(VAOBuilder2D.Mode.TRIANGLE_STRIP)
        return addDrawCall(RenderCall(range, RenderCall.ColorMode.COLOR))
    }

    override fun border(x: Float, y: Float, width: Float, height: Float, lineWidth: Float, radii: Vector4f?, color: Int): RenderCall {
        if (radii == null || radii.x == 0f && radii.y == 0f && radii.z == 0f && radii.w == 0f) {
            return border(x, y, width, height, lineWidth, color)
        }
        val positionMatrix = matrices.peek()
        val hw = lineWidth / 2
        val midPoints: List<Vector2f> = listOf(
                    Vector2f(x,y).fma(hw, CORNER_OFFSETS[0]),
                    Vector2f(x,y+height).fma(hw, CORNER_OFFSETS[1]),
                    Vector2f(x+width,y+height).fma(hw, CORNER_OFFSETS[2]),
                    Vector2f(x+width,y).fma(hw, CORNER_OFFSETS[3])
            )

        var first = true; val firstPos = Vector2f(); val firstNormal = Vector2f()
        vaoBuilder.begin()
        iterateRoundedRect(positionMatrix, x-hw, y-hw, x+width+hw, y+height+hw,  Vector4f(radii).add(hw, hw, hw, hw)) { position, normal, corner ->
            if (first) {
                firstPos.set(position); firstNormal.set(normal); first = false
            }
            if (hw > radii[corner]) {
                vaoBuilder.vertex(positionMatrix, midPoints[corner]).color(color).texture(0f, 0f).next()
            } else {
                vaoBuilder.vertex(positionMatrix, Math.fma(normal.x, -lineWidth, position.x), Math.fma(normal.y, -lineWidth, position.y)).color(color).texture(0f, 0f).next()
            }
            vaoBuilder.vertex(positionMatrix, position).color(color).texture(1f, 0f).next()
        }
        // Close loop, by going back to first.
        if (hw > radii[0]) {
            vaoBuilder.vertex(positionMatrix, midPoints[0]).color(color).texture(0f, 0f).next()
        } else {
            vaoBuilder.vertex(positionMatrix, Math.fma(firstNormal.x, -lineWidth, firstPos.x), Math.fma(firstNormal.y, -lineWidth, firstPos.y)).color(color).texture(0f, 0f).next()
        }
        vaoBuilder.vertex(positionMatrix, firstPos).color(color).texture(1f, 0f).next()
        val range = vaoBuilder.generateIndices(VAOBuilder2D.Mode.TRIANGLE_STRIP)
        return addDrawCall(RenderCall(range, RenderCall.ColorMode.COLOR))
    }

    override fun circle(x: Float, y: Float, radius: Float, color: Int): RenderCall {
        val segments = circleSegments(getScale(matrices.peek()) * radius)
        val segmentAngle = TWO_PI / segments
        val c = cos(segmentAngle)
        val s = sin(segmentAngle)
        val rotationMatrix = Matrix2f(c, -s, s, c)
        val position = Vector2f(radius, 0f)

        val uv0 = Vector2f(0f, 0f)
        val uv1 = Vector2f(1f, 1f)
        val r0 = Vector2f(-radius,-radius)
        val dimensions = Vector2f(2*radius, 2*radius)
        val tex = Vector2f()

        push()
        translate(x,y)
        val posMat = matrices.peek()
        vaoBuilder.begin()
        for (ii in 0 until segments) {
            interpolateRectangleTex(r0, dimensions, position, uv0, uv1, tex)
            vaoBuilder.vertex(posMat, position).color(color).texture(tex).next()
            position.mul(rotationMatrix)
        }
        pop()
        val range = vaoBuilder.generateIndices(VAOBuilder2D.Mode.TRIANGLE_FAN)
        return addDrawCall(RenderCall(range, RenderCall.ColorMode.COLOR))
    }

    override fun ellipse(x: Float, y: Float, a: Vector2f, b: Float, color: Int): RenderCall {
        push()
        translate(x,y)
        rotateRadians(atan2(a.y, a.x))
        scale(a.length(), b)

        val call = circle(0f,0f, 1f, color)

        pop()
        return call
    }

    override fun scissor(x: Float, y: Float, width: Float, height: Float) {
        scissorBox = getAbsoluteBoundingBox(x, y, width, height)
    }

    override fun endScissor() {
        scissorBox = null
    }

    override fun pauseScissor() {
        pausedScissorBox = scissorBox
        scissorBox = null
    }

    override fun resumeScissor() {
        scissorBox = pausedScissorBox
        pausedScissorBox = null
    }

    /**
     * Interpolates texture coordinates for the given [position] inside the rectangle defined by its origin [r0] with
     * the given [dimensions]. [uv0] is assumed to be the texture coordiante at [r0] and [uv1] the texture coordinate at
     * [r0] + [dimensions]. The result is written into [dest] and returned.
     */
    private fun interpolateRectangleTex(r0: Vector2f, dimensions: Vector2f, position: Vector2f, uv0: Vector2f, uv1: Vector2f, dest: Vector2f): Vector2f {
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

    /**
     * Iterates the positions for all vertices around a rounded rectangle with the given dimensions.
     * The iteration starts in the top left corner and goes counter-clock-wise.
     * @param positionMatrix The current local coordinate system.
     * @param x0 Top-left corner x coordinate. Expected to be < [x1].
     * @param y0 Top-left corner y coordinate. Expected to be < [y1].
     * @param x1 Bottom-right corner x coordinate. Expected to be < [y1].
     * @param y1 Bottom-right corner y coordinate. Expected to be < [y1].
     * @param radius The radius for all 4 corners. Should exceed neither ([y1]-[y0])/2 nor ([x1]-[x0])/2.
     * @param vertexGenerator This function is expected to generate the actual vertices.
     * It gets passed the position in the coordinates defined by [positionMatrix] as well as the normal pointing outwards.
     * **Take care not to mutate the normal, or it will mess up following vertices**.
     * The position may be mutated.
     */
    private fun iterateRoundedRect(positionMatrix: Matrix3x2f, x0: Float, y0: Float, x1: Float, y1: Float, radius: Float, vertexGenerator: (position: Vector2f, normal: Vector2f, corner: Int) -> Unit) {
        val midPoints: List<Vector2f> = listOf(
                Vector2f(x0,y0).fma(radius, CORNER_OFFSETS[0]),
                Vector2f(x0,y1).fma(radius, CORNER_OFFSETS[1]),
                Vector2f(x1,y1).fma(radius, CORNER_OFFSETS[2]),
                Vector2f(x1,y0).fma(radius, CORNER_OFFSETS[3])
        )

        val size = getScale(positionMatrix) * radius
        val segments = ceil(circleSegments(size) / 4f).toInt()

        val directions = if (segments  == 0) listOf(
                Vector2f( -ONE_OVER_SQRT_2, -ONE_OVER_SQRT_2),
                Vector2f(-ONE_OVER_SQRT_2,  ONE_OVER_SQRT_2),
                Vector2f( ONE_OVER_SQRT_2,  ONE_OVER_SQRT_2),
                Vector2f( ONE_OVER_SQRT_2,  -ONE_OVER_SQRT_2),
        )
        else listOf(
                Vector2f( 0f, -1f),
                Vector2f(-1f,  0f),
                Vector2f( 0f,  1f),
                Vector2f( 1f,  0f),
        )

        // Rotation matrix
        val segmentAngle = PI_HALF / segments
        val c = cos(segmentAngle)
        val s = sin(segmentAngle)
        val rotationMatrix = Matrix2f(c, -s, s, c)
        val position = Vector2f(); val direction = Vector2f(); val midPoint = Vector2f()

        for (corner in 0..3) {
            direction.set(directions[corner])
            midPoint.set(midPoints[corner])
            for (ii in 0 ..segments){
                position.set(
                    Math.fma(direction.x, radius, midPoint.x),
                    Math.fma(direction.y, radius, midPoint.y)
                )
                vertexGenerator(position, direction, corner)
                direction.mul(rotationMatrix)
            }
        }
    }

    /**
     * Iterates the positions for all vertices around a rounded rectangle with the given dimensions.
     * The iteration starts in the top left corner and goes counter-clock-wise.
     * @param positionMatrix The current local coordinate system.
     * @param x0 Top-left corner x coordinate. Expected to be < [x1].
     * @param y0 Top-left corner y coordinate. Expected to be < [y1].
     * @param x1 Bottom-right corner x coordinate. Expected to be < [y1].
     * @param y1 Bottom-right corner y coordinate. Expected to be < [y1].
     * @param radii The radii for all 4 corners in the order top-left, bottom-left, bottom-right, top-right.
     * Should exceed neither ([y1]-[y0])/2 nor ([x1]-[x0])/2.
     * @param vertexGenerator This function is expected to generate the actual vertices.
     * It gets passed the position in the coordinates defined by [positionMatrix] as well as the normal pointing outwards.
     * **Take care not to mutate normal, or it will mess up following vertices**.
     * The position may be mutated.
     */
    private fun iterateRoundedRect(positionMatrix: Matrix3x2f, x0: Float, y0: Float, x1: Float, y1: Float, radii: Vector4f, vertexGenerator: (position: Vector2f, normal: Vector2f, corner: Int) -> Unit) {
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
        val direction = Vector2f(); val position = Vector2f(); val midPoint = Vector2f()
        var segments: Int; var radius: Float

        for (corner in 0..3) {
            segments = ceil(circleSegments(radii[corner] * scale) / 4f).toInt()
            direction.set(if (segments == 0) DIAGONALS[corner] else ORTHOGONALS[corner] )
            midPoint.set(midPoints[corner])
            radius = radii[corner]
            segmentAngle = PI_HALF / segments
            c = cos(segmentAngle)
            s = sin(segmentAngle)
            rotationMatrix = Matrix2f(c, -s, s, c)
            for (ii in 0 ..segments){
                position.set(
                    Math.fma(direction.x, radius, midPoint.x),
                    Math.fma(direction.y, radius, midPoint.y)
                )
                vertexGenerator(position, direction, corner)
                direction.mul(rotationMatrix)
            }
        }
    }

    /**
     * Returns the bounding box of the given rectangle in screen coordinates to be used with the Scissor test.
     *
     * The given input coordinates are assumed to span a rectangle in the current coordinate system.
     * The returned bounding box is axis aligned with the window coordinate system.
     *
     */
    private fun getAbsoluteBoundingBox(x: Float, y: Float, width: Float, height: Float): BoundingBox {
        val mat = matrices.peek()
        val scissorTransform = Matrix3x2f(1f, 0f, 0f, -1f, 0f, mainBuffer.height.toFloat())
        val transform = Matrix3x2f(mat).mulLocal(scissorTransform)

        val corner = Vector2f()

        corner.set(x, y).mulPosition(transform)
        val boundingBox = BoundingBox(corner.x, corner.y, corner.x, corner.y)

        listOf(0 to 1, 1 to 1, 1 to 0).forEach {
            corner.set(x+width*it.first, y + height * it.second).mulPosition(transform)

            if (corner.x < boundingBox.xmin) boundingBox.xmin = corner.x
            else if (corner.x > boundingBox.xmax) boundingBox.xmax = corner.x
            if (corner.y < boundingBox.ymin) boundingBox.ymin = corner.y
            else if (corner.y > boundingBox.ymax) boundingBox.ymax = corner.y
        }

        return boundingBox
    }

    /**
     * For the given local transform this returns the average (RMS) scale factor by which lengths will be distorted.
     * Use this to estimate the length in pixels of an object with arbitrary rotation on the screen.
     */
    internal fun getScale(posMat: Matrix4f) : Float {
        return sqrt((posMat.m00()*posMat.m00() + posMat.m10()*posMat.m10() + posMat.m01()*posMat.m01() + posMat.m11()*posMat.m11())*0.5f)
    }

    /**
     * For the given local transform this returns the average (RMS) scale factor by which lengths will be distorted.
     * Use this to estimate the length in pixels of an object with arbitrary rotation on the screen.
     */
    internal fun getScale(posMat: Matrix3x2f) : Float {
        return sqrt((posMat.m00()*posMat.m00() + posMat.m10()*posMat.m10() + posMat.m01()*posMat.m01() + posMat.m11()*posMat.m11())*0.5f)
    }

    /**
     * Converts the given dimensions in pixels to texture coordinates ranging from 0 to 1.
     */
    private fun getTextureUVs(image: Image, imageX: Float, imageY: Float, imageWidth: Float, imageHeight: Float) : TextureCoordinates {
        val u0 = imageX / image.width
        val u1 = u0 + imageWidth / image.width
        var v0 = imageY / image.height
        var v1 = v0 + imageHeight / image.height
        if (image.flags.contains(Image.Flags.FLIP_Y)) {
            v0 = 1-v0
            v1 = 1-v1
        }
        return TextureCoordinates(u0, v0, u1, v1)
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

    private object GLStateTracker {
        private var blend: Boolean = false
        private var cullFace: Boolean = false
        private var depthTest: Boolean = false
        private var activeTexture: Int = 0

        fun setupState() {
            cullFace = glGetBoolean(GL_CULL_FACE)
            blend = glGetBoolean(GL_BLEND)
            depthTest = glGetBoolean(GL_DEPTH_TEST)

            activeTexture = glGetInteger(GL_ACTIVE_TEXTURE)

            if (cullFace) glDisable(GL_CULL_FACE)
            if (!blend) glEnable(GL_BLEND)
            if (depthTest) glDisable(GL_DEPTH_TEST)
        }

        fun restoreState() {
            if (cullFace) glEnable(GL_CULL_FACE)
            if (!blend) glDisable(GL_BLEND)
            if (depthTest) glEnable(GL_DEPTH_TEST)

            glActiveTexture(activeTexture)
        }
    }
    private data class TextureCoordinates(val u0: Float, val v0: Float, val u1: Float, val v1: Float) {
        val uv0: Vector2f  get() = Vector2f(u0, v0)
        val uv1: Vector2f  get() = Vector2f(u1, v1)
    }

    private const val ONE_OVER_SQRT_2 = 0.7071068f
    private const val PI = (java.lang.Math.PI).toFloat()
    private const val PI_HALF = (0.5 * java.lang.Math.PI).toFloat()
    private const val TWO_PI = (2*java.lang.Math.PI).toFloat()
    private const val DEG_TO_RAD: Float = PI / 180f

    /**
     * Directions from rectangle corners to the center of the circle required for rounded corners.
     * In the order top-left(++), bottom-left(+-), bottom-right(--), top-right(-+).
     * These have the length sqrt(2).
     */
    private val CORNER_OFFSETS = listOf(
        Vector2f(1f, 1f),
        Vector2f(1f, -1f),
        Vector2f(-1f, -1f),
        Vector2f(-1f, 1f)
    )

    /**
     * Normalized vectors in the 4 diagonal directions in following order:
     * top-left(--), bottom-left(-+), bottom-right(++), top-right(+-).
     */
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
    internal const val RESOURCE_DOMAIN: String = "aurora"
    internal val logger: Logger = LoggerFactory.getLogger("aurora")
    internal val DEBUG: Boolean = System.getProperty("aurora.debug") == "true"
}