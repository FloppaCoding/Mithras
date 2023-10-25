package floppacoding.mithras.shaders.uniforms.impl

import floppacoding.mithras.shaders.uniforms.UniformGL
import org.joml.Vector4f
import java.nio.FloatBuffer

class Uniform4f : UniformGL<Vector4f, FloatBuffer> {
    constructor(programID: Int, name: String, updater: () -> Vector4f) : super(programID, name, Type.FLOAT, 4, updater)
    constructor(programID: Int, name: String): super(programID, name, Type.FLOAT, 4)
    override fun writeNewValToBuffer(newValue: Vector4f) {
        this.buffer.put(0, newValue.x)
        this.buffer.put(1, newValue.y)
        this.buffer.put(2, newValue.z)
        this.buffer.put(3, newValue.w)
    }
}