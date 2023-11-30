package floppacoding.aurora.core.shader.uniforms.impl

import floppacoding.aurora.core.shader.uniforms.UniformGL
import org.joml.Matrix2f
import java.nio.FloatBuffer

class UniformMatrix2f : UniformGL<Matrix2f, Float, FloatBuffer> {
    constructor(programID: Int, name: String, updater: () -> Matrix2f) : super(programID, name, Type.FLOAT, Shape.MAT2, updater)
    constructor(programID: Int, name: String): super(programID, name, Type.FLOAT, Shape.MAT2)
    override fun writeNewValToBuffer(newValue: Matrix2f): Boolean {
        buffer.position(0)
        val comparison = Matrix2f(buffer)
        if(newValue == comparison) return true
        newValue.get(0, buffer)
        return false
    }
}