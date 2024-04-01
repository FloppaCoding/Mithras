package floppacoding.ui.color

import floppacoding.ui.animation.Animation
import floppacoding.ui.animation.Animations

class AnimatedColor(private var color1: IColor, private var color2: IColor) : IColor {

    private var animation: Animation? = null

    var current: Int = color1.rgba
    var from: Int = color1.rgba

    // proof of concept
    override val rgba: Int
        get() {
            if (animation != null) {
                val progress = animation!!.get()
                val to = color2.rgba
                current = getRGBA(
                    (from.red + (to.red - from.red) * progress).toInt(),
                    (from.green + (to.green - from.green) * progress).toInt(),
                    (from.blue + (to.blue - from.blue) * progress).toInt(),
                    (from.alpha + (to.alpha - from.alpha) * progress).toInt()
                )
                if (animation!!.finished) {
                    animation = null
                    swap()
                }
                return current
            }
            return color1.rgba
        }

    fun animate(duration: Float = 0f, type: Animations) {
        if (duration == 0f) {
            swap()
            current = color1.rgba // here so it updates if you swap a color and want to animate it later
        } else {
            if (animation != null) {
                swap()
                animation = Animation(duration * animation!!.get(), type)
            } else {
                animation = Animation(duration, type)
            }
            from = current
        }
    }

    private fun swap() {
        val temp = color2
        color2 = color1
        color1 = temp
    }
}

fun getRGBA(red: Int, green: Int, blue: Int, alpha: Int): Int {
    return ((alpha shl 24) and 0xFF000000.toInt()) or ((red shl 16) and 0x00FF0000) or ((green shl 8) and 0x0000FF00) or (blue and 0x000000FF)
}