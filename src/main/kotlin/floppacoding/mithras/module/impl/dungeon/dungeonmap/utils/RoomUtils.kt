package floppacoding.mithras.module.impl.dungeon.dungeonmap.utils

import floppacoding.mithras.module.impl.dungeon.dungeonmap.core.RoomData
import floppacoding.mithras.module.impl.dungeon.dungeonmap.core.Room
import floppacoding.mithras.module.impl.dungeon.dungeonmap.core.RoomType

/**
 * A collection of methods for dungeon room specific information.
 *
 * These include coordinate transformations and obtaining the correct data from config files.
 *
 * @author Aton
 */
object RoomUtils {


    fun instanceBossRoom(floor: Int): Room {
        return Room(0,0, RoomData("Boss $floor", RoomType.BOSS))
    }

    fun instanceRegionRoom(region: String): Room {
        return Room(0,0, RoomData(region, RoomType.REGION))
    }
}