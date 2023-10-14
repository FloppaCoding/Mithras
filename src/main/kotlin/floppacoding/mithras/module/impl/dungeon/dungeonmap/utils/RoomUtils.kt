package floppacoding.mithras.module.impl.dungeon.dungeonmap.utils

import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import floppacoding.mithras.Mithras
import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.module.impl.dungeon.dungeonmap.core.Room
import floppacoding.mithras.module.impl.dungeon.dungeonmap.core.RoomConfigData
import floppacoding.mithras.module.impl.dungeon.dungeonmap.core.RoomData
import floppacoding.mithras.module.impl.dungeon.dungeonmap.core.RoomType
import floppacoding.mithras.module.impl.dungeon.dungeonmap.dungeon.ConfigRoom
import floppacoding.mithras.utils.ScoreboardUtils
import net.minecraft.util.Identifier

/**
 * A collection of methods for dungeon room specific information.
 *
 * These include coordinate transformations and obtaining the correct data from config files.
 *
 * @author Aton
 */
object RoomUtils {
    val roomList: Set<RoomConfigData> = try {
        val resource = mc.resourceManager.getResource(Identifier(Mithras.RESOURCE_DOMAIN, "dungeonmap/rooms.json"))
        val stream = resource.get().inputStream
        Gson().fromJson(
            stream.bufferedReader(),
            object : TypeToken<Set<RoomConfigData>>() {}.type
        )
    } catch (e: JsonSyntaxException) {
        Mithras.logger.error("Error parsing Mithras  room data.")
        setOf()
    } catch (e: JsonIOException) {
        Mithras.logger.error("Error reading Mithras room data.")
        setOf()
    }

    fun isInRoom(room: ConfigRoom) : Boolean {
        return isInRoom(room.configData)
    }

    fun isInRoom(configData: RoomConfigData?) : Boolean {
        val id = getRoomScoreboardID() ?: return false

        return configData?.scoreboardIDs?.contains(id) == true
    }

    fun getRoomScoreboardID(): String? {
        val id = try {
            ScoreboardUtils.sidebarLines.last().trim().split(" ").last()
        } catch (_: NoSuchElementException) {
            null
        }
        return if(id?.contains(",") == true) id else null
    }

    fun instanceDummyRoom(x: Int, z: Int): Room {
        return Room(x, z, RoomData())
    }


    fun instanceBossRoom(floor: Int): Room {
        return Room(0,0, RoomData("Boss $floor", RoomType.BOSS)).apply { data.rotation = 0 }
    }

    fun instanceRegionRoom(region: String): Room {
        return Room(0,0, RoomData(region, RoomType.REGION))
    }
}