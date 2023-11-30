package floppacoding.aurora.core.shader.uniforms.impl

import floppacoding.aurora.core.shader.uniforms.UniformGL
import org.joml.Vector3f
import java.nio.FloatBuffer

class Uniform3f : UniformGL<Vector3f, Float, FloatBuffer> {

    constructor(programID: Int, name: String, updater: () -> Vector3f) : super(programID, name, Type.FLOAT, Shape.VEC3, updater)
    constructor(programID: Int, name: String): super(programID, name, Type.FLOAT, Shape.VEC3)
    override fun writeNewValToBuffer(newValue: Vector3f): Boolean {
        val comparison = Vector3f(0, buffer)
        if(newValue == comparison) return false
        newValue.get(0, buffer)
        return true
    }
}