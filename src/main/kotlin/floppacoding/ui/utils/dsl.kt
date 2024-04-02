package floppacoding.ui.utils

import floppacoding.ui.animation.Animations
import floppacoding.ui.color.AnimatedColor
import floppacoding.ui.color.IColor
import floppacoding.ui.constraints.Constraint
import floppacoding.ui.constraints.measurements.Animatable
import floppacoding.ui.constraints.measurements.Pixel
import floppacoding.ui.elements.Element
import floppacoding.ui.events.onClick
import floppacoding.ui.events.onMouseMove
import floppacoding.ui.events.onRelease
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


fun <E : Element> E.draggable(acceptsEvent: Boolean = false, target: Element = this): E {
    var px: Pixel
    var py: Pixel
    target.constraints.apply {
        px = when (x) {
            is Pixel -> x as Pixel
            else -> Pixel(0f)
        }
        py = when (y) {
            is Pixel -> y as Pixel
            else -> Pixel(0f)
        }
    }
    var pressed = false
    var x = 0f
    var y = 0f
    onClick(0) {
        pressed = true
        x = ui.mouseX - this@draggable.x
        y = ui.mouseY - this@draggable.y
        acceptsEvent
    }
    onMouseMove {
        if (pressed) {
            px.pixels = ui.mouseX - x
            py.pixels = ui.mouseY - y
        }
        acceptsEvent
    }
    onRelease(0) {
        pressed = false
    }
    return this
}

fun <E : Element> E.focuses(): E {
    onClick(0) {
        ui.focus(this@focuses)
        true
    }
    return this
}