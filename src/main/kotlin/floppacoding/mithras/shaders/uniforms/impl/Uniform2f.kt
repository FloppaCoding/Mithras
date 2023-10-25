package floppacoding.mithras.shaders.uniforms.impl

import floppacoding.mithras.shaders.uniforms.UniformGL
import org.joml.Vector2f
import java.nio.FloatBuffer

class Uniform2f : UniformGL<Vector2f, FloatBuffer> {
    constructor(programID: Int, name: String, updater: () -> Vector2f) : super(programID, name, Type.FLOAT, 2, updater)
    constructor(programID: Int, name: String): super(programID, name, Type.FLOAT, 2)

    override fun writeNewValToBuffer(newValue: Vector2f) {
        this.buffer.put(0, newValue.x)
        this.buffer.put(1, newValue.y)
    }
}