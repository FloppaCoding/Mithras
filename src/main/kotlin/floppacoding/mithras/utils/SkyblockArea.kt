package floppacoding.mithras.utils

sealed class SkyblockArea private constructor(val areaName: String) {

    object Galatea: SkyblockArea("Galatea")
    class PrivateIsland(val owner: String?): SkyblockArea("Private Island")
    class Unknown(areaName: String): SkyblockArea(areaName)

    override fun equals(other: Any?): Boolean {return other === this}

    companion object {
        @JvmStatic val GALATEA = Galatea

        @JvmStatic val entries: List<SkyblockArea> = SkyblockArea::class.sealedSubclasses.mapNotNull { it.objectInstance }
    }
}
