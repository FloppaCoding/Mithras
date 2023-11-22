package floppacoding.aurora.core.shader.uniforms.impl

import floppacoding.aurora.core.shader.uniforms.UniformGL
import org.joml.Matrix3f
import java.nio.FloatBuffer

class UniformMatrix3f : UniformGL<Matrix3f, Float, FloatBuffer> {
    constructor(programID: Int, name: String, updater: () -> Matrix3f) : super(programID, name, Type.FLOAT, Shape.MAT3, updater)
    constructor(programID: Int, name: String): super(programID, name, Type.FLOAT, Shape.MAT3)
    override fun writeNewValToBuffer(newValue: Matrix3f): Boolean {
        val comparison = Matrix3f(buffer.position(0))
        if(newValue == comparison) return false
        newValue.get(0, buffer)
        return true
    }
}