package floppacoding.mithras.utils

sealed class SkyblockArea private constructor(val areaName: String) {

    object Galatea:             SkyblockArea("Galatea")
    object TheBarn:             SkyblockArea("The Barn")
    object Hub:                 SkyblockArea("Hub")
    object DungeonHub:          SkyblockArea("Dungeon Hub")
    object TheFarmingIslands:   SkyblockArea("The Farming Islands")
    object ThePark:             SkyblockArea("Park")
    object GoldMine:            SkyblockArea("Gold Mine")
    object DeepCaverns:         SkyblockArea("Deep Caverns")
    object DwarvenMines:        SkyblockArea("Dwarven Mines")
    object CrystalHollows:      SkyblockArea("Crystal Hollows")
    object SpidersDen:          SkyblockArea("Spider's Den")
    object TheEnd:              SkyblockArea("The End")
    object CrimsonIsle:         SkyblockArea("Crimson Isle")
    object Garden:              SkyblockArea("Garden")
    object JerrysWorkshop:      SkyblockArea("Jerry's Workshop")
    class PrivateIsland(val owner: String?):    SkyblockArea("Private Island")
    class Unknown(areaName: String):            SkyblockArea(areaName)

    override fun equals(other: Any?): Boolean {return other === this}

    companion object {
        @JvmStatic val GALATEA = Galatea
        @JvmStatic val THE_BARN = TheBarn
        @JvmStatic val HUB = Hub
        @JvmStatic val DUNGEON_HUB = DungeonHub
        @JvmStatic val THE_FARMING_ISLANDS = TheFarmingIslands
        @JvmStatic val THE_PARK = ThePark
        @JvmStatic val GOLDMINE = GoldMine
        @JvmStatic val DEEP_CAVERNS = DeepCaverns
        @JvmStatic val DWARVEN_MINES = DwarvenMines
        @JvmStatic val CRYSTAL_HOLLOWS = CrystalHollows
        @JvmStatic val SPIDERS_DEN = SpidersDen
        @JvmStatic val THE_END = TheEnd
        @JvmStatic val CRIMSON_ISLE = CrimsonIsle
        @JvmStatic val GARDEN = Garden
        @JvmStatic val JERRYS_WORKSHOP = JerrysWorkshop

        @JvmStatic val entries: List<SkyblockArea> = SkyblockArea::class.sealedSubclasses.mapNotNull { it.objectInstance }
    }

    /**
     * Returns true if an island only supports modern minecraft versions
     */
    fun isIslandModern(): Boolean {
        // if new islands come out, add to here,
        // or  alternatively, could make an open/abstract function for this?
        return this === Galatea || this === ThePark
    }
}