package floppacoding.mithras.shaders.uniforms.impl

import floppacoding.mithras.shaders.uniforms.UniformGL
import org.joml.Vector2f
import java.nio.FloatBuffer

class Uniform2f : UniformGL<Vector2f, Float, FloatBuffer> {
    constructor(programID: Int, name: String, updater: () -> Vector2f) : super(programID, name, Type.FLOAT, Shape.VEC2, updater)
    constructor(programID: Int, name: String): super(programID, name, Type.FLOAT, Shape.VEC2)

    override fun writeNewValToBuffer(newValue: Vector2f): Boolean {
        val comparison = Vector2f(0, buffer)
        if(newValue == comparison) return false
        newValue.get(0, buffer)
        return true
    }
}