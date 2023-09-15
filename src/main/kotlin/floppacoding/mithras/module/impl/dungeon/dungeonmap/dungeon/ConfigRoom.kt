package floppacoding.mithras.module.impl.dungeon.dungeonmap.dungeon

import floppacoding.mithras.Mithras
import floppacoding.mithras.module.impl.dungeon.dungeonmap.core.RoomConfigData
import floppacoding.mithras.module.impl.dungeon.dungeonmap.utils.RoomUtils

/**
 * An enum for accessing the config data of relevant rooms.
 */
enum class ConfigRoom(name: String) {
    THREE_WEIRDOS("Three Weirdos");

    val configData : RoomConfigData?

    init{
        configData = RoomUtils.roomList.find { it.name == name }
        if (configData == null)
            Mithras.logger.error("No room data found for $name.")
    }
}