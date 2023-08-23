package floppacoding.mithras.utils

import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.events.ClientTickEvent
import floppacoding.mithras.events.ConnectionEvent
import floppacoding.mithras.events.WorldChangeEvent
import floppacoding.mithras.module.impl.dungeon.dungeonmap.core.Room
import floppacoding.mithras.module.impl.dungeon.dungeonmap.utils.RoomUtils
import meteordevelopment.orbit.EventHandler
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.util.Formatting

object LocationManager {

    var onHypixel: Boolean = false
    var inSkyblock: Boolean = false
    var inDungeons = false
        get() = inSkyblock && field
    var currentRegionPair: Pair<Room, Int>? = null

    /**
     * Keeps track of elapsed ticks, gets reset at 20
     */
    private var tickRamp = 0

    @EventHandler
    fun onTick(event: ClientTickEvent) {
        if (event.phase != ClientTickEvent.Phase.START) return
        tickRamp++

        if (tickRamp % 20 == 0) {
            if (mc.player != null) {

                if (!inSkyblock) {
                    inSkyblock = onHypixel && mc.world?.scoreboard?.getObjectiveForSlot(1)
                        ?.let { ScoreboardUtils.cleanSB(it.displayName.string).contains("SKYBLOCK") } ?: false
                }

                // If alr known that in dungeons don't update the value. It does get reset to false on world change.
                if (!inDungeons) {
                    inDungeons = inSkyblock && ScoreboardUtils.sidebarLines.any {
                        ScoreboardUtils.cleanSB(it).run {
                            (contains("The Catacombs") && !contains("Queue")) || contains("Dungeon Cleared:")
                        }
                    }
                }
            }
            tickRamp = 0
        }
        val newRegion = getArea()
        if (currentRegionPair?.first?.data?.name != newRegion){
            currentRegionPair = newRegion?.let { Pair( RoomUtils.instanceRegionRoom(it) , 0) }
        }
    }

    @EventHandler
    fun onDisconnect(event: ConnectionEvent.Disconnect) {
        onHypixel = false
        inSkyblock = false
        inDungeons = false
    }

    @EventHandler
    fun onWorldChange(@Suppress("UNUSED_PARAMETER") event: WorldChangeEvent) {
        inDungeons = false
        inSkyblock = false
        currentRegionPair = null
        tickRamp = 18
    }

    /**
     * Taken from [SBC](https://github.com/Harry282/Skyblock-Client/blob/main/src/main/kotlin/skyblockclient/utils/LocationUtils.kt)
     */
    @EventHandler
    fun onConnect(event: ConnectionEvent.Join) {
        onHypixel = mc.runCatching {
            ((mc.player?.serverBrand?.lowercase()?.contains("hypixel")
                ?: mc.currentServerEntry?.address?.lowercase()?.contains("hypixel")) == true)
        }.getOrDefault(false)
    }


    /**
     * Returns the current area from the tab list info.
     * If no info can be found return null.
     */
    private fun getArea(): String? {
        if (!inSkyblock) return null
        val netHandlerPlayClient: ClientPlayNetworkHandler = mc.player?.networkHandler ?: return null
        val list = netHandlerPlayClient.playerList ?: return null
        var area: String? = null
        var extraInfo: String? = null
        for (entry in list) {
            //  "Area: Hub"
            val areaText = Formatting.strip(entry?.displayName?.string) ?: continue
            if (areaText.startsWith("Area: ")) {
                area = areaText.substringAfter("Area: ")
                if (!area.contains("Private Island")) break
            }
            if (areaText.contains("Owner:")){
                extraInfo = areaText.substringAfter("Owner:")
            }

        }
        return if (area == null)
            null
        else
            area + (extraInfo ?: "")
    }
}