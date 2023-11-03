package floppacoding.mithras.shaders.uniforms.impl

class Sampler : Uniform1i {
    constructor(programID: Int, name: String, updater: () -> Int) : super(programID, name, updater)
    constructor(programID: Int, name: String) : super(programID, name)
}