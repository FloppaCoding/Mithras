package floppacoding.mithras.module.impl.dungeon.dungeonmap.core

import floppacoding.mithras.module.impl.dungeon.dungeonmap.dungeon.Dungeon
import java.awt.Color

abstract class Tile(val x: Int, val z: Int) {
    var state = RoomState.UNDISCOVERED
    var visited = false
    var scanned = true
    abstract val color: Color

    /**
     * Row in the duneonList.
     */
    val row
        get() = (z - Dungeon.START_Z) shr 4
    /**
     * Column in the dungeonList
     */
    val column
        get() = (x - Dungeon.START_X) shr 4
}
