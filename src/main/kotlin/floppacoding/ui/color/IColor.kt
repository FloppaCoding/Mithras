package floppacoding.ui.color

import java.awt.Color

interface IColor {
    val rgba: Int
}

open class Color(hue: Float, saturation: Float, brightness: Float, alpha: Float = 1f) : IColor, Cloneable {

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

    inline val red
        get() = this.rgba shr 16 and 0xFF

    inline val green
        get() = this.rgba shr 8 and 0xFF

    inline val blue
        get() = this.rgba and 0xFF

    inline val a
        get() = this.rgba shr 24 and 0xFF

    public override fun clone() = Color(hue, saturation, brightness, alpha)
}

inline val Int.red
    get() = this shr 16 and 0xFF

inline val Int.green
    get() = this shr 8 and 0xFF

inline val Int.blue
    get() = this and 0xFF

inline val Int.alpha
    get() = this shr 24 and 0xFF