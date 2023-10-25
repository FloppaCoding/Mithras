package floppacoding.mithras.shaders.uniforms.impl

import floppacoding.mithras.shaders.uniforms.UniformGL
import org.joml.Matrix4f
import java.nio.FloatBuffer

class UniformMatrix4f : UniformGL<Matrix4f, FloatBuffer> {
    constructor(programID: Int, name: String, updater: () -> Matrix4f) : super(programID, name, Type.FLOAT, 16, updater)
    constructor(programID: Int, name: String): super(programID, name, Type.FLOAT, 16)
    override fun writeNewValToBuffer(newValue: Matrix4f) {
        newValue.get(0, buffer)
    }
}