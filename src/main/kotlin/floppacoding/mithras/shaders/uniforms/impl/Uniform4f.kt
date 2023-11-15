package floppacoding.mithras.shaders.uniforms.impl

import floppacoding.mithras.shaders.uniforms.UniformGL
import org.joml.Vector4f
import java.nio.FloatBuffer

class Uniform4f : UniformGL<Vector4f, Float, FloatBuffer> {
    constructor(programID: Int, name: String, updater: () -> Vector4f) : super(programID, name, Type.FLOAT, Shape.VEC4, updater)
    constructor(programID: Int, name: String): super(programID, name, Type.FLOAT, Shape.VEC4)
    override fun writeNewValToBuffer(newValue: Vector4f): Boolean {
        val comparison = Vector4f(0, buffer)
        if(newValue == comparison) return false
        newValue.get(0, buffer)
        return true
    }
}