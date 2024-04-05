package floppacoding.ui.color

import floppacoding.ui.animation.Animation
import floppacoding.ui.animation.Animations
import floppacoding.ui.utils.getRGBA

class AnimatedColor(from: IColor, to: IColor) : IColor {

    constructor(from: IColor, to: IColor, swapIf: Boolean) : this(from, to) {
        if (swapIf) {
            swap()
            current = color1.rgba
        }
    }

    private var color1: IColor = from
    private var color2: IColor = to

    private var animation: Animation? = null

    var current: Int = color1.rgba
    var from: Int = color1.rgba

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