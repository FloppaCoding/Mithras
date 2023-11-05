package floppacoding.mithras.shaders.uniforms.impl

import floppacoding.mithras.shaders.uniforms.UniformGL
import org.joml.Matrix2f
import java.nio.FloatBuffer

class UniformMatrix2f : UniformGL<Matrix2f, Float, FloatBuffer> {
    constructor(programID: Int, name: String, updater: () -> Matrix2f) : super(programID, name, Type.FLOAT, Shape.MAT2, updater)
    constructor(programID: Int, name: String): super(programID, name, Type.FLOAT, Shape.MAT2)
    override fun writeNewValToBuffer(newValue: Matrix2f) {
        newValue.get(0, buffer)
    }
}