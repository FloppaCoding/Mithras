package floppacoding.ui

import floppacoding.aurora.core.Renderer2D
import floppacoding.ui.elements.Constraints
import floppacoding.ui.elements.Pixel
import floppacoding.ui.elements.impl.Group

class UIV2(val renderer2D: Renderer2D) {

    val main: Group = Group(Constraints(Pixel(0f), Pixel(0f), Pixel(1920f), Pixel(1080f))).also { it.ui = this }

    fun render() {
        renderer2D.beginFrame()
        main.render()
        renderer2D.endFrame()
    }
}