package floppacoding.mithras.shaders.uniforms.impl

import floppacoding.mithras.shaders.uniforms.UniformGL
import java.nio.IntBuffer

open class Uniform1i : UniformGL<Int, IntBuffer> {
    constructor(programID: Int, name: String, updater: () -> Int) : super(programID, name, Type.INT, 1, updater)
    constructor(programID: Int, name: String): super(programID, name, Type.INT, 1)

    override fun writeNewValToBuffer(newValue: Int) {
        this.buffer.put(0, newValue)
    }
}