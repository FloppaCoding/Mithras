package floppacoding.mithras.utils.render

import floppacoding.mithras.Mithras
import floppacoding.mithras.utils.render.VAOBuilder2D.Companion.VERTEX_SIZE
import floppacoding.mithras.utils.render.VAOBuilder2D.Mode
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL45.*
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer
import java.nio.IntBuffer

/**
 * # Buffer builder for 2D rendering.
 *
 * The purpose of this class is to generate and buffer all vertex data for a frame.
 * It also provides functionality to upload the generated data to the gpu.
 *
 * ## General functionality
 * There are two underlying buffers accumulating data for your frame:
 *  - The vertex buffer, and
 *  - the index buffer.
 *
 * The vertex buffer stores all vertices required to draw the desired geometry.
 * The index buffer defines the shapes these vertices generate, by declaring in which order the vertices are to be visited.
 *
 * So to generate geometry you have to generate vertices as well the corresponding indices.
 *
 * ## General setup
 * Before you start generating geometry for a frame you have to clear the geometry still present from the last frame.
 * To do this invoke [reset].
 *
 * Before you can draw anything you must upload the data to the gpu trough [upload]
 *
 *
 * ## Vertices
 *
 * ### Vertex format
 * All vertices have the same attributes:
 *  - x,y position,
 *  - rgba color information
 *  - u,v texture coordinates.
 *
 * These can be set through [vertex], [color] and [texture].
 *
 * Even if color or texture coordinates are not used, the vertex contains them.
 * This may sound wasteful, however it allows for uploading all vertices at once, at the cost of not very significant
 * memory overhead.
 *
 * You also do not need to specify attribute information when you know that it is not used. In that case it remains
 * undefined.
 *
 * ### Generating the vertices
 *
 * A new vertex is created by specifying its position with [vertex].
 * Optionally you can then specify its color and texture coordinates with the respective methods.
 * The order in which you set the attributes does not matter.
 * To specify that you are done generating a vertex and want to move to the next one you always have to use [next].
 * For a cleaner syntax these commands can be chained.
 * Successive changes to the same attribute before [next] will overwrite the previously set value.
 *
 * The following examples all show ways of generating a vertex:
 *
 *      builder.vertex(100f, 50f).color(-1).texture(0f, 0f).next() // White vertex at position 100f, 50f with u=v=0f.
 *      builder.vertex(100f, 50f).texture(0f, 0f).color(-1).next() // Same as the above.
 *      builder.texture(0f, 0f).vertex(100f, 50f).color(-1).next() // Same as the above.
 *      builder.vertex(100f, 50f).color(-1).next() // White vertex at position 100f, 50f with undefined texture coordinates.
 *      builder.vertex(100f, 50f).next() // Vertex at position 100f, 50f with undefined color and texture coordinates.
 *
 * ## Indices
 *
 * The indices refer to the absolute index of the vertex since the first one.
 * They have to be generated such that three consecutive indices form a triangle of your geometry.
 *
 * You can specify them manually, but this class can also do it for you.
 *
 * ### Generating indices
 *
 * #### Automatic Generation
 * To automatically generate the indices you must first tell the builder which one is the first.
 * This is done by invoking [begin] **before** specifying your first vertex.
 * Then after you are done generating the vertices just call [generateIndices] with your desired [mode][Mode].
 *
 * #### Manual Generation
 * To manually set generate indices you can use [setIndices].
 * It interprets all passed arguments as being relative to the absolute index of your first vertex.
 * This also requires you to first invoke [begin] like explained above.
 *
 * Alternatively you can also specify the offset (absolute index of the first vertex) by using [setIndicesWithAbsoluteOffset].
 * For this you probably need the index of your first vertex.
 * This can be obtained by an invocation to [getVertexIndex] **before** specifying your first vertex.
 *
 * ## Example
 * The following example shows how you can generate a colored quad.
 *
 *      val x = 0f; val y = 0f; val width = 100f; val height = 100f; val color = -1
 *
 *      vaoBuilder.begin()
 *      vaoBuilder.vertex(x, y).color(color).next()
 *      vaoBuilder.vertex(x, y+height).color(color).next()
 *      vaoBuilder.vertex(x+width, y+height).color(color).next()
 *      vaoBuilder.vertex(x+width, y).color(color).next()
 *      val range = vaoBuilder.generateIndices(VAOBuilder2D.Mode.QUAD)
 *
 *
 * @param capacity The initial capacity of the underlying vertex and index buffers in bytes.
 * @param memoryIncrease The size bny which the buffers will be increased when memory runs out.
 *
 * @author Aton
 */
class VAOBuilder2D(capacity: Int, private val memoryIncrease: Int) {
    /**
     * Creates a [VAOBuilder2D] with default capacity.
     */
    constructor(): this(CAPACITY, CAPACITY_INCREASE)
    private var buffer: ByteBuffer = MemoryUtil.memAlloc(capacity)
    private var indexBuffer: IntBuffer = MemoryUtil.memAllocInt(capacity)

    /**
     * Reference to the corresponding Vertex Array Object.
     */
    private val vao: Int = glGenVertexArrays()
    /**
     * Reference to corresponding Vertex Buffer Object.
     */
    private val vbo: Int = glGenBuffers()
    /**
     * Reference to the corresponding Index Buffer Object.
     */
    private val indexBufferObject: Int = glGenBuffers()

    /**
     * Offset into the vertex buffer for the current vertex in bytes.
     * This should always be [vertexIndex]*[VERTEX_SIZE].
     */
    private var vertexOffset: Int = 0

    /**
     * The offset into the index buffer for the next index.
     * This is the same as the number of preceding indices in the buffer.
     */
    private var indexOffset: Int = 0

    /**
     * The index of the current vertex.
     */
    private var vertexIndex: Int = 0

    /**
     * Index of the first vertex in a shape.
     * Used for generating indices from relative indices.
     */
    private var firstVertex: Int = 0

    /**
     * Returns the absolute index of the current index.
     */
    fun getVertexIndex() : Int { return vertexIndex }

    /**
     * Tells the builder that you started generating geometry.
     * Required to later generate Indices.
     */
    fun begin() {
        firstVertex = vertexIndex
    }

    /**
     * Sets the specified indices relative to the index after the last invocation of [begin].
     * @return The absolute position of the indices in the underlying Index Buffer.
     */
    fun setIndices(vararg relativeIndices: Int): IntRange = setIndicesWithAbsoluteOffset(firstVertex, *relativeIndices)

    /**
     * Same as [setIndices] but lets you specify which offset to use.
     * @return The absolute position of the indices in the underlying Index Buffer.
     */
    fun setIndicesWithAbsoluteOffset(offset: Int, vararg indices: Int) : IntRange {
        for ( ii in indices.indices) {
            indices[ii] += offset
        }
        indexBuffer.put(indexOffset, indices)
        val range = indexOffset until indexOffset + indices.size
        indexOffset += indices.size
        return range
    }

    /**
     * Generates indices to draw all vertices since [begin] as triangles.
     * The vertices are interpreted in the specified [mode].
     */
    fun generateIndices(mode: Mode): IntRange = generateIndices(mode, firstVertex, vertexIndex - firstVertex)

    /**
     * Generates indices to draw [length] vertices since [start] as triangles.
     * The vertices are interpreted in the specified [mode].
     *
     * @see generateIndices
     */
    fun generateIndices(mode: Mode, start: Int, length: Int): IntRange {
        val indices: IntArray
        when(mode){
            Mode.TRIANGLES -> {
                val num = (length / 3) * 3
                indices = IntArray(num)
                for (ii in indices.indices) {
                    indices[ii] = start + ii
                }
            }
            Mode.QUAD -> {
                val quads = length / 4
                val num = quads * 6
                indices = IntArray(num)
                var pos: Int
                for (ii in 0 until quads) {
                    pos = ii * 6
                    indices[pos + 0] = start + pos + 0
                    indices[pos + 1] = start + pos + 1
                    indices[pos + 2] = start + pos + 2
                    indices[pos + 3] = start + pos + 2
                    indices[pos + 4] = start + pos + 3
                    indices[pos + 5] = start + pos + 0
                }
            }
            Mode.TRIANGLE_STRIP -> {
                if (length < 3) return start until start
                val triangles = length - 2
                val num = triangles * 3
                indices = IntArray(num)
                var pos: Int
                for (ii in 0 until triangles) {
                    pos = ii * 3
                    indices[pos + 0] = start + pos + 0
                    indices[pos + 1] = start + pos + 1
                    indices[pos + 2] = start + pos + 2
                }
            }
        }
        indexBuffer.put(indexOffset, indices)
        val range = indexOffset until indexOffset + indices.size
        indexOffset += indices.size
        return range
    }

    /**
     * Resets all data from the last frame.
     */
    fun reset() {
        vertexOffset = 0
        vertexIndex = 0
        indexOffset = 0
        buffer.limit(buffer.capacity())
        buffer.position(0) // probably not required but better safe than sorry.
        indexBuffer.position(0)
        indexBuffer.limit(indexBuffer.capacity())
    }

    /**
     * Sets the position of the current vertex in absolute coordinates.
     * These are not affected by any local transforms.
     */
    fun vertex(x: Float, y: Float): VAOBuilder2D {
        putFloat(0, x)
        putFloat(4, y)
        return this
    }

    /**
     * Sets the position the current vertex in the coordinate space defined by [transform].
     */
    fun vertex(transform: Matrix3f, x: Float, y: Float): VAOBuilder2D {
        val transformed: Vector3f = transform.transform(Vector3f(x, y, 1.0f))
        return vertex(transformed.x, transformed.y)
    }

    /**
     * Sets the position the current vertex in the coordinate space defined by [transform].
     * [transform] is interpreted as a transform for a 3-dimensional coordinates system where the z coordinate is ignored.
     */
    fun vertex(transform: Matrix4f, x: Float, y: Float): VAOBuilder2D {
        val transformed: Vector4f = transform.transform(Vector4f(x, y, 0f, 1.0f))
        return vertex(transformed.x, transformed.y)
    }

    /**
     * Sets the color of the current vertex.
     * [color] is assumed to be in the bit order argb.
     */
    fun color (color: Int): VAOBuilder2D {
        return color((color shr 16).toByte(), (color shr 8).toByte(), (color shr 0).toByte(), (color shr 24).toByte())
    }

    /**
     * Sets the color of the current vertex.
     */
    fun color(red: Int, green: Int, blue: Int, alpha: Int): VAOBuilder2D {
        return color(red.toByte(), green.toByte(), blue.toByte(), alpha.toByte())
    }

    /**
     * Sets the color of the current vertex.
     */
    fun color(red: Byte, green: Byte, blue: Byte, alpha: Byte): VAOBuilder2D {
        putByte( 8, red)
        putByte( 9, green)
        putByte(10, blue)
        putByte(11, alpha)
        return this
    }

    /**
     * Sets the texture coordinates of the current vertex.
     * The coordinates are clamped to the range [0..1].
     */
    fun texture(u: Float, v: Float) = texture(u.toNormalizedShort(), v.toNormalizedShort())

    /**
     * Sets the texture coordinates of the current vertex.
     * The short values are mapped to [0..1].
     */
    fun texture(u: UShort, v: UShort) = texture(u.toShort(), v.toShort())

    /**
     * Sets the texture coordinates of the current vertex.
     * The short values are interpreted as unsigned bytes and mapped to [0..1].
     */
    fun texture(u: Short, v: Short): VAOBuilder2D {
        putShort(12, u)
        putShort(14, v)
        return this
    }

    /**
     * Finishes the current vertex and moves to the next one.
     */
    fun next() {
        vertexOffset += VERTEX_SIZE
        vertexIndex++
    }

    /**
     * Uploads the data to the GPU.
     */
    fun upload() {
        buffer.position(0)
        buffer.limit(vertexOffset)
        indexBuffer.position(0)
        indexBuffer.limit(indexOffset)

        glBindVertexArray(vao)
        // Vertex Buffer
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_DYNAMIC_DRAW)
        // Position attribute
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, VERTEX_SIZE, 0L)
        // Color Attribute
        glEnableVertexAttribArray(1)
        glVertexAttribPointer(1, 4, GL_UNSIGNED_BYTE, true, VERTEX_SIZE, 8L)
        // Texture Attribute
        glEnableVertexAttribArray(2)
        glVertexAttribPointer(2, 2, GL_UNSIGNED_SHORT, true, VERTEX_SIZE, 12L)
        // Index Buffer
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_DYNAMIC_DRAW)

    }

    private fun growBuffer() {
        val currentCapacity = buffer.capacity()
        buffer = MemoryUtil.memRealloc(buffer, currentCapacity + memoryIncrease)
        Mithras.logger.warn("Needed to grow VAOBuilder2D buffer from $currentCapacity bytes to ${currentCapacity + memoryIncrease} bytes.")
    }

    private fun putFloat(offset: Int, value: Float) {
        try {
            buffer.putFloat(vertexOffset + offset, value)
        }catch (_: IndexOutOfBoundsException) {
            growBuffer()
            buffer.putFloat(vertexOffset + offset, value)
        }
    }

    private fun putByte(offset: Int, value: Byte) {
        try {
            buffer.put(vertexOffset + offset, value)
        }catch (_: IndexOutOfBoundsException){
            growBuffer()
            buffer.put(vertexOffset + offset, value)
        }
    }

    private fun putShort(offset: Int, value: Short) {
        try {
            buffer.putShort(vertexOffset + offset, value)
        }catch (_: IndexOutOfBoundsException){
            growBuffer()
            buffer.putShort(vertexOffset + offset, value)
        }
    }

    private fun putInt(offset: Int, value: Int) {
        try {
            buffer.putInt(vertexOffset + offset, value)
        }catch (_: IndexOutOfBoundsException) {
            growBuffer()
            buffer.putInt(vertexOffset + offset, value)
        }

    }

    private fun Float.toNormalizedShort(): Short {
        return (this.coerceIn(0f, 1f) * 25_535f).toInt().toShort()
    }

    /**
     * Mode in which the vertices are interpreted to generate indices.
     */
    enum class Mode{
        TRIANGLES,
        QUAD,
        TRIANGLE_STRIP,
    }

    companion object{
        private const val CAPACITY = 2_097_152 // = 2^21 B = 2 MB. At 16 B per vertex, this is enough for 2^17 = 131_072 vertices.
        private const val CAPACITY_INCREASE = 16_384 // = 2^14 B = 16 kB. This is enough for 1024 more vertices.
        private const val VERTEX_SIZE = 16 // Each element consists of 16 bytes: 2*4 Position, 4*1 Color, 2*2 Texture.
    }
}