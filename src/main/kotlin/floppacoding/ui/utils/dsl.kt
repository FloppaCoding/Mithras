package floppacoding.ui.utils

import floppacoding.ui.animation.Animations
import floppacoding.ui.color.AnimatedColor
import floppacoding.ui.color.IColor
import floppacoding.ui.constraints.Constraint
import floppacoding.ui.constraints.measurements.Animatable
import org.joml.Vector4f

fun radii(tl: Number = 0f, tr: Number = 0f, bl: Number = 0f, br: Number = 0f) = Vector4f(tl.toFloat(), bl.toFloat(), br.toFloat(), tr.toFloat())

fun radii(all: Number): Vector4f {
    val value = all.toFloat()
    return Vector4f(value, value, value, value)
}

/**
 * DSL for checking if a [color][IColor] is [animatable][AnimatedColor] and animating it.
 *
 * @param duration The time it takes to complete the animation (in nanoseconds)
 * @param type The type of animation to use. (By default it is Linear)
 */
fun IColor.animate(duration: Number, type: Animations = Animations.Linear) {
    if (this is AnimatedColor) animate(duration.toFloat(), type)
}

fun Constraint.animate(duration: Number, type: Animations = Animations.Linear) {
    if (this is Animatable) animate(duration.toFloat(), type)
}

val Number.seconds
    get() = this.toFloat() * 1_000_000_000