package floppacoding.mithras.utils.inventory

class Gemstone(val type: String, val quality: Quality) {

    val itemID = "${quality.name}_${type}_GEM"

    enum class Quality {
        ROUGH,
        FLAWED,
        FINE,
        FLAWLESS,
        PERFECT;
    }
}