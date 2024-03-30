package floppacoding.ui

import floppacoding.aurora.core.Renderer2D
import floppacoding.ui.elements.Constraints
import floppacoding.ui.elements.impl.Group

class UIV2(val renderer2D: Renderer2D) {

    val main: Group = Group(Constraints(0.px, 0.px, 1920.px, 1080.px)).also {
        it.initialize(this)
    }

    fun initialize() {
//        main.initialize(this)
    }

    fun render() {
        renderer2D.beginFrame()
        main.render()
        renderer2D.endFrame()
    }
}