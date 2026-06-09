package floppacoding.mithras.utils.inventory

class Gemstone(val type: String, val quality: Quality) : SkyblockItem("${quality.name}_${type}_GEM", ItemAttribute.GEMSTONE) {

    enum class Quality {
        ROUGH,
        FLAWED,
        FINE,
        FLAWLESS,
        PERFECT;
    }
}