package floppacoding.ui.utils

import floppacoding.ui.UI
import floppacoding.ui.animation.Animations
import floppacoding.ui.color.Color
import floppacoding.ui.constraints.Constraint
import floppacoding.ui.constraints.measurements.Animatable
import floppacoding.ui.constraints.measurements.Pixel
import floppacoding.ui.constraints.minus
import floppacoding.ui.elements.Element
import floppacoding.ui.events.Mouse
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
fun Color.animate(duration: Number, type: Animations = Animations.Linear) {
    if (this is Color.Animated) animate(duration.toFloat(), type)
}

fun Constraint.animate(duration: Number, type: Animations = Animations.Linear) {
    if (this is Animatable) animate(duration.toFloat(), type)
}

// todo: make color functions for all color types
fun color(r: Int, g: Int, b: Int, alpha: Float = 1f): Color.RGB = Color.RGB(r, g, b, alpha)

fun color(from: Color, to: Color, swap: Boolean = false): Color.Animated = Color.Animated(from, to, swap)

//
//fun color(r: Int, g: Int, b: Int, alpha: Float = 1f): Color.RGB = Color.RGB(r, g, b, alpha)
//
//fun color(r: Int, g: Int, b: Int, alpha: Float = 1f): Color.RGB = Color.RGB(r, g, b, alpha)

val Number.seconds
    get() = this.toFloat() * 1_000_000_000


// todo: cleanup
fun <E : Element> E.draggable(acceptsEvent: Boolean = true, target: Element = this): E {
    var px: Pixel
    var py: Pixel
    target.constraints.apply {
        px = when (x) {
            is Pixel -> x as Pixel
            else -> {
                UI.logger.warning(
                    "Draggable ${this@draggable::class.java} original X constraint wasn't Pixel, " +
                         "instead it was ${this::class.simpleName}, this usually leads to unexpected behaviour"
                )
                Pixel(0f)
            }
        }
        py = when (y) {
            is Pixel -> y as Pixel
            else -> {
                UI.logger.warning(
                    "Draggable ${this@draggable::class.java} original Y constraint wasn't Pixel, " +
                         "instead it was ${this::class.simpleName}, this usually leads to unexpected behaviour"
                )
                Pixel(0f)
            }
        }
    }
    var pressed = false
    var x = 0f
    var y = 0f
    onClick(0) {
        pressed = true
        x = ui.mx - this@draggable.x
        y = ui.my - this@draggable.y
        acceptsEvent
    }
    onMouseMove {
        if (pressed) {
            px.pixels = ui.mx - x
            py.pixels = ui.my - y
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

fun <E : Element> E.scrollable(duration: Float, target: Element, min: Float = 0f): E {
    var s = 0f
    val anim = Animatable.Raw(0f)
    target.constraints.apply {
        y = (y - anim)
    }
    registerEvent(Mouse.Scrolled(0f)) {
        s = (s - (this as Mouse.Scrolled).amount * 16).coerceIn(min, target.height)
        anim.animate(s, duration)
        true
    }
    return this
}