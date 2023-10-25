package floppacoding.mithras.shaders.uniforms.impl

import floppacoding.mithras.shaders.uniforms.UniformGL
import org.joml.Vector3f
import java.nio.FloatBuffer

class Uniform3f : UniformGL<Vector3f, FloatBuffer> {

    constructor(programID: Int, name: String, updater: () -> Vector3f) : super(programID, name, Type.FLOAT, 3, updater)
    constructor(programID: Int, name: String): super(programID, name, Type.FLOAT, 3)
    override fun writeNewValToBuffer(newValue: Vector3f) {
        this.buffer.put(0, newValue.x)
        this.buffer.put(1, newValue.y)
        this.buffer.put(2, newValue.z)
    }
}