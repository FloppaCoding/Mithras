package floppacoding.aurora.core.shader.uniforms

interface Uniform: AutoCloseable {

    /**
     * Updates the value of this uniform to the GPU.
     */
    fun update()
    /**
     * Updates this Uniform for the specified program.
     * This has to be run if the program this uniform belongs to is replaced (reloaded).
     */
    fun updateID(programID: Int)
}