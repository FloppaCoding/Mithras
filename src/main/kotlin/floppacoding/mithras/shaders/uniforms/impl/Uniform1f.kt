package floppacoding.mithras.shaders.uniforms.impl

import floppacoding.mithras.shaders.uniforms.UniformGL
import java.nio.FloatBuffer

class Uniform1f : UniformGL<Float, Float, FloatBuffer> {

    constructor(programID: Int, name: String, updater: () -> Float) : super(programID, name, Type.FLOAT, Shape.SCALAR, updater)
    constructor(programID: Int, name: String): super(programID, name, Type.FLOAT, Shape.SCALAR)

    override fun writeNewValToBuffer(newValue: Float) {
        this.buffer.put(0, newValue)
    }
}