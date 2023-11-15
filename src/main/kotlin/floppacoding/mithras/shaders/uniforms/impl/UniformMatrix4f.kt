package floppacoding.mithras.shaders.uniforms.impl

import floppacoding.mithras.shaders.uniforms.UniformGL
import org.joml.Matrix4f
import java.nio.FloatBuffer

class UniformMatrix4f : UniformGL<Matrix4f, Float, FloatBuffer> {
    constructor(programID: Int, name: String, updater: () -> Matrix4f) : super(programID, name, Type.FLOAT, Shape.MAT4, updater)
    constructor(programID: Int, name: String): super(programID, name, Type.FLOAT, Shape.MAT4)
    override fun writeNewValToBuffer(newValue: Matrix4f): Boolean {
        val comparison = Matrix4f(buffer.position(0))
        if(newValue == comparison) return false
        newValue.get(0, buffer)
        return true
    }
}