package floppacoding.ui.constraints.measurements

import floppacoding.ui.animation.Animation
import floppacoding.ui.animation.Animations
import floppacoding.ui.constraints.Axis
import floppacoding.ui.constraints.Constraint
import floppacoding.ui.constraints.Measurement
import floppacoding.ui.elements.Element

class Animatable(var from: Constraint, var to: Constraint): Measurement {

    private var animation: Animation? = null

    var current: Float = 0f

    var before: Float? = null // is null

    override fun get(element: Element, axis: Axis): Float {
        if (animation != null) {
            val progress = animation!!.get()
            val from = before ?: from.get(element, axis)
            current = from + (to.get(element, axis) - from) * progress

            if (animation!!.finished) {
                animation = null
                before = null
                swap()
            }
            return current
        }
        return from.get(element, axis)
    }

    fun animate(duration: Float, type: Animations) {
        if (duration == 0f) {
            swap()
        } else {
            if (animation != null) {
                before = current
                swap()
                animation = Animation(duration * animation!!.get(), type)
            } else {
                animation = Animation(duration, type)
            }
        }
    }

    private fun swap() {
        val temp = to
        to = from
        from = temp
    }
}