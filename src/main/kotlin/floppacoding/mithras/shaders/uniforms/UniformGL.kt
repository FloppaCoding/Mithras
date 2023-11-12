package floppacoding.mithras.shaders.uniforms

import org.lwjgl.opengl.GL46
import org.lwjgl.system.MemoryUtil
import java.nio.Buffer
import java.nio.DoubleBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer

/**
 * A class for interfacing with open gl uniforms.
 *
 * Uniforms are used to communicate variables with shaders at runtime.
 * This class offers some utilities to use that functionality.
 *
 * @author Aton
 */
abstract class UniformGL<in T : Any, N: Number, K: Buffer> (
    programID: Int,
    val name: String,
    val type: Type<K, N>,
    val shape: Shape,
) : Uniform {

    constructor(programID: Int, name: String, type: Type<K, N>, shape: Shape, updater: () -> T) : this(programID, name, type, shape) {
        this.updater = updater
        dirty = true
    }

    /**
     * Reference to the impl. Used somewhat similar to a pointer.
     */
    protected var uniformID: Int = GL46.glGetUniformLocation(programID, name)

    /**
     * This is the underlying buffer used for storing the state of the uniform as well as for communicating with the
     * native methods.
     *
     * The buffer is initialized as all zeros.
     * The buffer uses off-heap memory which has to be handled manually.
     * It does not get freed by the garbage collector.
     */
    protected val buffer: K = type.provideBuffer(shape.count)

    /**
     * Determines whether the value should be updated to the GPU.
     */
    private var dirty = false

    /**
     * Provides a new value for the uniform whenever [update] is invoked.
     */
    private var updater: (() -> T)? = null

    var transpose = false

    /**
     * Update the value of this uniform.
     *
     * If the Uniform is initialized with an [updater] this will have no effect.
     */
    fun updateValue(newValue: T) {
        writeNewValToBuffer(newValue)
        dirty = true
    }

    /**
     * Update the value of this uniform.
     *
     * If the Uniform is initialized with an [updater] this will have no effect.
     */
    fun updateValues(vararg newValues: N) {
        type.updateData(shape, buffer, newValues)
        dirty = true
    }

    /**
     * Handles the translation of the value to the underlying buffer.
     */
    protected abstract fun writeNewValToBuffer(newValue: T)

    /**
     * Updates the [uniformID] of this Uniform for the specified program.
     * This has to be run if the program this uniform belongs to is replaced (reloaded).
     */
    override fun updateID(programID: Int) {
        uniformID = GL46.glGetUniformLocation(programID, name)
        dirty = true
    }

    /**
     * Updates the value of this uniform to the GPU.
     *
     * If [dirty] is set to false no action is taken.
     *
     * If set this will invoke [updater] to determine the value.
     *
     * Otherwise, the last value set by [updateValue] is used.
     * In case that was never run, the default value is all zeroes.
     * This is consistent with the default value assigned to uniforms by OpenGL.
     */
    override fun update() {
        if (!dirty) return
        if (updater != null) {
            writeNewValToBuffer(updater!!())
        }
        uploadData()
        if (updater == null) dirty = false
    }

    private fun uploadData() {
        buffer.rewind()
        type.uploadData(this.uniformID, shape, buffer, transpose)
    }

    override fun close() {
        MemoryUtil.memFree(buffer)
    }

    /**
     * Determines the underlying buffer used for the uniform.
     */
    abstract class Type<K: Buffer, N: Number> {
        private object FloatInternal : Type<FloatBuffer, Float>() {
            override fun provideBuffer(count: Int): FloatBuffer {
                return MemoryUtil.memCallocFloat(count)
            }

            override fun updateData(shape: Shape, buffer: FloatBuffer, newValues: Array<out Float>) {
                buffer.put(0, newValues.toFloatArray(), 0, shape.count)
            }

            override fun uploadData(uniformID: Int, shape: Shape, buffer: FloatBuffer, transpose: Boolean) {
                when(shape) {
                    Shape.SCALAR -> GL46.glUniform1fv(uniformID, buffer)
                    Shape.VEC2   -> GL46.glUniform2fv(uniformID, buffer)
                    Shape.VEC3   -> GL46.glUniform3fv(uniformID, buffer)
                    Shape.VEC4   -> GL46.glUniform4fv(uniformID, buffer)
                    Shape.MAT2   -> GL46.glUniformMatrix2fv  (uniformID, transpose, buffer)
                    Shape.MAT3   -> GL46.glUniformMatrix3fv  (uniformID, transpose, buffer)
                    Shape.MAT4   -> GL46.glUniformMatrix4fv  (uniformID, transpose, buffer)
                    Shape.MAT3x2 -> GL46.glUniformMatrix3x2fv(uniformID, transpose, buffer)
                    Shape.MAT2x3 -> GL46.glUniformMatrix2x3fv(uniformID, transpose, buffer)
                    Shape.MAT4x2 -> GL46.glUniformMatrix4x2fv(uniformID, transpose, buffer)
                    Shape.MAT2x4 -> GL46.glUniformMatrix2x4fv(uniformID, transpose, buffer)
                    Shape.MAT4x3 -> GL46.glUniformMatrix4x3fv(uniformID, transpose, buffer)
                    Shape.MAT3x4 -> GL46.glUniformMatrix3x4fv(uniformID, transpose, buffer)
                }
            }
        }

        private object IntInternal : Type<IntBuffer, Int>() {
            override fun provideBuffer(count: Int): IntBuffer {
                return MemoryUtil.memCallocInt(count)
            }

            override fun updateData(shape: Shape, buffer: IntBuffer, newValues: Array<out Int>) {
                buffer.put(0, newValues.toIntArray(), 0, shape.count)
            }

            override fun uploadData(uniformID: Int, shape: Shape, buffer: IntBuffer, transpose: Boolean) {
                when(shape) {
                    Shape.SCALAR -> GL46.glUniform1iv(uniformID, buffer)
                    Shape.VEC2   -> GL46.glUniform2iv(uniformID, buffer)
                    Shape.VEC3   -> GL46.glUniform3iv(uniformID, buffer)
                    Shape.VEC4   -> GL46.glUniform4iv(uniformID, buffer)
                    else -> {}
                }
            }
        }

        private object DoubleInternal : Type<DoubleBuffer, Double>() {
            override fun provideBuffer(count: Int): DoubleBuffer {
                return MemoryUtil.memCallocDouble(count)
            }

            override fun updateData(shape: Shape, buffer: DoubleBuffer, newValues: Array<out Double>) {
                buffer.put(0, newValues.toDoubleArray(), 0, shape.count)
            }

            override fun uploadData(uniformID: Int, shape: Shape, buffer: DoubleBuffer, transpose: Boolean) {
                when(shape) {
                    Shape.SCALAR -> GL46.glUniform1dv(uniformID, buffer)
                    Shape.VEC2   -> GL46.glUniform2dv(uniformID, buffer)
                    Shape.VEC3   -> GL46.glUniform3dv(uniformID, buffer)
                    Shape.VEC4   -> GL46.glUniform4dv(uniformID, buffer)
                    Shape.MAT2   -> GL46.glUniformMatrix2dv  (uniformID, transpose, buffer)
                    Shape.MAT3   -> GL46.glUniformMatrix3dv  (uniformID, transpose, buffer)
                    Shape.MAT4   -> GL46.glUniformMatrix4dv  (uniformID, transpose, buffer)
                    Shape.MAT3x2 -> GL46.glUniformMatrix3x2dv(uniformID, transpose, buffer)
                    Shape.MAT2x3 -> GL46.glUniformMatrix2x3dv(uniformID, transpose, buffer)
                    Shape.MAT4x2 -> GL46.glUniformMatrix4x2dv(uniformID, transpose, buffer)
                    Shape.MAT2x4 -> GL46.glUniformMatrix2x4dv(uniformID, transpose, buffer)
                    Shape.MAT4x3 -> GL46.glUniformMatrix4x3dv(uniformID, transpose, buffer)
                    Shape.MAT3x4 -> GL46.glUniformMatrix3x4dv(uniformID, transpose, buffer)
                }
            }
        }

        abstract fun provideBuffer(count: Int) : K

        abstract fun updateData(shape: Shape, buffer: K, newValues: Array<out N>)

        abstract fun uploadData(uniformID: Int, shape: Shape, buffer: K, transpose: Boolean)

        companion object {
            @JvmField
            val FLOAT: Type<FloatBuffer, Float> = FloatInternal

            @JvmField
            val INT: Type<IntBuffer, Int> = IntInternal

            @JvmField
            val DOUBLE: Type<DoubleBuffer, Double> = DoubleInternal
        }
    }

    enum class Shape(val count: Int) {
        SCALAR(1),
        VEC2(2),
        VEC3(3),
        VEC4(4),
        MAT2(4),
        MAT3(9),
        MAT4(16),
        MAT2x3(6),
        MAT3x2(6),
        MAT4x2(8),
        MAT2x4(8),
        MAT4x3(12),
        MAT3x4(12),
    }
}

inline fun <T, V, K, reified U: UniformGL<T, V, K>> U.withValue(newValue: T) : U {
    this.updateValue(newValue)
    return this
}

inline fun <T, V, K, reified U: UniformGL<T, V, K>> U.withValues(vararg newValues: V) : U {
    this.updateValues(*newValues)
    return this
}