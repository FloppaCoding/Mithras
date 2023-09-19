package floppacoding.mithras.module.impl.dungeon.dungeonmap.core

import floppacoding.mithras.module.impl.dungeon.MapRooms
import floppacoding.mithras.module.impl.dungeon.dungeonmap.dungeon.Dungeon
import java.awt.Color

class Room(x: Int, z: Int, var data: RoomData) : Tile(x, z) {

    constructor(x: Int, z: Int, configData: RoomConfigData) : this(x, z, RoomData(configData = configData))

    /**
     * Core of this tile.
     */
    var core: Int? = null
    /**
     * The ID currently visible on the scoreboard.
     *
     * Due to delay issues, this is will not always be the correct ID for the room.
     * This is mostly meant as a buffer so that modules don't all have to retrieve this value individually.
     *
     * See Also: [data.configData.scoreboardIDs][RoomConfigData.scoreboardIDs].
     */
    var scoreboardID: String? = null
    var isSeparator = false

    /**
     * Marks this tile as "unique".
     * Unique is only relevant for rooms consisting of multiple tiles. All 1x1 rooms are unique by default.
     * For rooms with multiple tiles the first Tile in [Dungeon.dungeonList] will be the unique one.
     * This corresponds to the most west and north tile of a room. (west prioritized over north)
     * The unique tile is the one which has the checkmark on the map.
     */
    var isUnique: Boolean = data.type != RoomType.NORMAL

    /**
     * Delegate to [data.rotation][RoomData.rotation].
     */
    val rotation by data::rotation

    override val color: Color
        get() = if (this.state == RoomState.QUESTION_MARK && !visited)
            MapRooms.colorUnexplored.value
        else when (data.type) {
            RoomType.UNKNOWN ->   MapRooms.colorUnexplored.value
            RoomType.BLOOD ->     MapRooms.colorBlood.value
            RoomType.CHAMPION ->  MapRooms.colorMiniboss.value
            RoomType.ENTRANCE ->  MapRooms.colorEntrance.value
            RoomType.FAIRY ->     MapRooms.colorFairy.value
            RoomType.PUZZLE ->    MapRooms.colorPuzzle.value
            RoomType.RARE ->      MapRooms.colorRare.value
            RoomType.TRAP ->      MapRooms.colorTrap.value
            else -> if (data.hasMimic) MapRooms.colorRoomMimic.value else MapRooms.colorRoom.value
        }
}
