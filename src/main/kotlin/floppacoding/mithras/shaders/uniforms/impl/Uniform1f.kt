package floppacoding.mithras.shaders.uniforms.impl

import floppacoding.mithras.shaders.uniforms.UniformGL
import java.nio.FloatBuffer

class Uniform1f : UniformGL<Float, FloatBuffer> {

    constructor(programID: Int, name: String, updater: () -> Float) : super(programID, name, Type.FLOAT, 1, updater)
    constructor(programID: Int, name: String): super(programID, name, Type.FLOAT, 1)

    override fun writeNewValToBuffer(newValue: Float) {
        this.buffer.put(0, newValue)
    }
}