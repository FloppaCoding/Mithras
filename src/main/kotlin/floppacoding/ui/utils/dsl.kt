package floppacoding.ui.utils

import floppacoding.ui.animation.Animations
import floppacoding.ui.color.AnimatedColor
import floppacoding.ui.color.IColor
import floppacoding.ui.constraints.Constraints
import floppacoding.ui.constraints.Position
import floppacoding.ui.constraints.Size
import floppacoding.ui.constraints.measurements.Pixel
import floppacoding.ui.constraints.measurements.Undefined
import org.joml.Vector4f

fun radii(tl: Number = 0f, tr: Number = 0f, bl: Number = 0f, br: Number = 0f) = Vector4f(tl.toFloat(), bl.toFloat(), br.toFloat(), tr.toFloat())

/**
 * DSL for checking if a [color][IColor] is [animatable][AnimatedColor] and animating it.
 *
 * @param duration The time it takes to complete the animation (in nanoseconds)
 * @param type The type of animation to use. (By default it is Linear)
 */
fun IColor.animate(duration: Number, type: Animations = Animations.Linear) {
    if (this is AnimatedColor) animate(duration.toFloat(), type)
}

fun at(x: Position, y: Position) = Constraints(x, y, Undefined, Undefined)

fun size(width: Size, height: Size) = Constraints(Undefined, Undefined, width, height)

val Number.px
    get() = Pixel(this.toFloat())

val Number.seconds
    get() = this.toFloat() * 1_000_000_000