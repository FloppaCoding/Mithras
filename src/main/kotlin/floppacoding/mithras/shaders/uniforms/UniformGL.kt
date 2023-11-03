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
abstract class UniformGL<in T : Any, K: Buffer> protected constructor(
    programID: Int,
    val name: String,
    val type: Type<K>,
    // TODO maybe replace this with an enum to allow for 2x2 matrices and other stuff.
    val count: Int,
) : Uniform {

    constructor(programID: Int, name: String, type: Type<K>, count: Int, updater: () -> T) : this(programID, name, type, count) {
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
    protected val buffer: K = type.provideBuffer(count)

    /**
     * Determines whether the value should be updated to the GPU.
     */
    private var dirty = false

    /**
     * Provides a new value for the uniform whenever [update] is invoked.
     */
    private var updater: (() -> T)? = null

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
        type.uploadData(this.uniformID, count, buffer)
    }

    override fun close() {
        MemoryUtil.memFree(buffer)
    }

    /**
     * Determines the underlying buffer used for the uniform.
     */
    sealed class Type<K: Buffer> {
        data object FLOAT : Type<FloatBuffer>() {
            override fun provideBuffer(count: Int): FloatBuffer {
                return MemoryUtil.memCallocFloat(count)
            }

            override fun uploadData(uniformID: Int, count: Int, buffer: FloatBuffer) {
                when(count) {
                    1  -> GL46.glUniform1fv(uniformID, buffer)
                    2  -> GL46.glUniform2fv(uniformID, buffer)
                    3  -> GL46.glUniform3fv(uniformID, buffer)
                    4  -> GL46.glUniform4fv(uniformID, buffer)
                    9  -> GL46.glUniformMatrix3fv(uniformID, false, buffer)
                    16 -> GL46.glUniformMatrix4fv(uniformID, false, buffer)
                }
            }
        }

        data object INT : Type<IntBuffer>() {
            override fun provideBuffer(count: Int): IntBuffer {
                return MemoryUtil.memCallocInt(count)
            }

            override fun uploadData(uniformID: Int, count: Int, buffer: IntBuffer) {
                when(count) {
                    1  -> GL46.glUniform1iv(uniformID, buffer)
                    2  -> GL46.glUniform2iv(uniformID, buffer)
                    3  -> GL46.glUniform3iv(uniformID, buffer)
                    4  -> GL46.glUniform4iv(uniformID, buffer)
                }
            }
        }

        data object DOUBLE : Type<DoubleBuffer>() {
            override fun provideBuffer(count: Int): DoubleBuffer {
                return MemoryUtil.memCallocDouble(count)
            }

            override fun uploadData(uniformID: Int, count: Int, buffer: DoubleBuffer) {
                when(count) {
                    1  -> GL46.glUniform1dv(uniformID, buffer)
                    2  -> GL46.glUniform2dv(uniformID, buffer)
                    3  -> GL46.glUniform3dv(uniformID, buffer)
                    4  -> GL46.glUniform4dv(uniformID, buffer)
                    9  -> GL46.glUniformMatrix3dv(uniformID, false, buffer)
                    16 -> GL46.glUniformMatrix4dv(uniformID, false, buffer)
                }
            }
        }

        abstract fun provideBuffer(count: Int) : K

        abstract fun uploadData(uniformID: Int, count: Int, buffer: K)
    }
}

inline fun <T, K, reified U: UniformGL<T,K>> U.withValue(newValue: T) : U {
    this.updateValue(newValue)
    return this
}