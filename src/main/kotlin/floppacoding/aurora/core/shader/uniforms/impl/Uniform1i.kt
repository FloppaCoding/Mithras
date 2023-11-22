package floppacoding.aurora.core.shader.uniforms.impl

import floppacoding.aurora.core.shader.uniforms.UniformGL
import java.nio.IntBuffer

open class Uniform1i : UniformGL<Int, Int, IntBuffer> {
    constructor(programID: Int, name: String, updater: () -> Int) : super(programID, name, Type.INT, Shape.SCALAR, updater)
    constructor(programID: Int, name: String): super(programID, name, Type.INT, Shape.SCALAR)

    override fun writeNewValToBuffer(newValue: Int): Boolean {
        if (this.buffer.get(0) == newValue) return false
        this.buffer.put(0, newValue)
        return true
    }
}