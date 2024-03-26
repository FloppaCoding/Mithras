package floppacoding.oldui.color

import java.awt.Color

open class Color(hue: Float, saturation: Float, brightness: Float, alpha: Float = 1f) : IColor {

    constructor(hsb: FloatArray, alpha: Float = 1f) : this(hsb[0], hsb[1], hsb[2], alpha)

    constructor(r: Int, g: Int, b: Int, alpha: Float = 1f) : this(Color.RGBtoHSB(r, g, b, FloatArray(size = 3)), alpha)

    constructor(rgba: Int) : this(rgba.red, rgba.green, rgba.blue, alpha = rgba.alpha / 255f)

    var hue = hue
        set(value) {
            field = value
            needsUpdate = true
        }

    var saturation = saturation
        set(value) {
            field = value
            needsUpdate = true
        }

    var brightness = brightness
        set(value) {
            field = value
            needsUpdate = true
        }

    var alpha = alpha
        set(value) {
            field = value
            needsUpdate = true
        }

    private var needsUpdate: Boolean = true

    override var rgba: Int = 0
        get() {
            if (needsUpdate) {
                field = (Color.HSBtoRGB(hue, saturation, brightness) and 0X00FFFFFF) or ((alpha * 255).toInt() shl 24)
                needsUpdate = false
            }
            return field
        }
}

class Animating(private var color1: IColor, private var color2: IColor) : IColor {

    var current: Boolean = false

    override var rgba: Int = color1.rgba

    fun animate() { // implement actual animation
        current = !current
        rgba = if (current) color2.rgba else color1.rgba
    }

    fun animateTo(first: Boolean) {
        current = !first
        rgba = if (current) color2.rgba else color1.rgba
    }
}

interface IColor {
    val rgba: Int
}

inline val Int.red get() = this shr 16 and 0xFF
inline val Int.green get() = this shr 8 and 0xFF
inline val Int.blue get() = this and 0xFF
inline val Int.alpha get() = this shr 24 and 0xFF