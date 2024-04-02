package floppacoding.ui.constraints.measurements

import floppacoding.ui.animation.Animation
import floppacoding.ui.animation.Animations
import floppacoding.ui.constraints.Constraint
import floppacoding.ui.constraints.Measurement
import floppacoding.ui.constraints.Type
import floppacoding.ui.elements.Element

class Animatable(var from: Constraint, var to: Constraint): Measurement {

    constructor(from: Constraint, to: Constraint, swapIf: Boolean) : this(from, to) {
        if (swapIf) {
            swap()
        }
    }

    private var animation: Animation? = null

    var current: Float = 0f

    var before: Float? = null // is null

    override fun get(element: Element, type: Type): Float {
        if (animation != null) {
            val progress = animation!!.get()
            val from = before ?: from.get(element, type)
            current = from + (to.get(element, type) - from) * progress

            if (animation!!.finished) {
                animation = null
                before = null
                swap()
            }
            return current
        }
        return from.get(element, type)
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

    fun swapIf(value: Boolean): Animatable {
        if (value) swap()
        return this
    }

    class Raw(start: Float) : Measurement {

        private var current: Float = start

        private var animation: Animation? = null

        fun animate(to: Float, duration: Float, type: Animations = Animations.Linear) {
            if (duration != 0f) animation = Animation(duration, type, animation?.get() ?: current, to) else current = to
        }

        fun to(to: Float) = if (animation != null) animation!!.to = to else current = to

        override fun get(element: Element, type: Type): Float {
            if (animation != null) {
                val result = animation!!.get()
                if (animation!!.finished) {
                    animation = null
                    current = result
                }
                return result
            }
            return current
        }
    }
}
