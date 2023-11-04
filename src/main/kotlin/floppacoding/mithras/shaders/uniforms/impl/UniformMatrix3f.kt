package floppacoding.mithras.shaders.uniforms.impl

import floppacoding.mithras.shaders.uniforms.UniformGL
import org.joml.Matrix3f
import java.nio.FloatBuffer

class UniformMatrix3f : UniformGL<Matrix3f, FloatBuffer> {
    constructor(programID: Int, name: String, updater: () -> Matrix3f) : super(programID, name, Type.FLOAT, Shape.MAT3, updater)
    constructor(programID: Int, name: String): super(programID, name, Type.FLOAT, Shape.MAT3)
    override fun writeNewValToBuffer(newValue: Matrix3f) {
        newValue.get(0, buffer)
    }
}