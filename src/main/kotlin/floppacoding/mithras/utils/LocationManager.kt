package floppacoding.mithras.utils

import floppacoding.mithras.Mithras
import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.events.AreaChangeEvent
import floppacoding.mithras.events.ClientTickEvent
import floppacoding.mithras.events.ConnectionEvent
import floppacoding.mithras.events.WorldChangeEvent
import floppacoding.mithras.mixin.ClientCommonNetworkHandlerAccessor
import meteordevelopment.orbit.EventHandler
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.scoreboard.ScoreboardDisplaySlot
import net.minecraft.util.Formatting

object LocationManager {

    var onHypixel: Boolean = false
        private set
    var inSkyblock: Boolean = false
        private set
    var inDungeons = false
        get() = inSkyblock && field
        private set

    /**
     * The area, which the player is currently in.
     *
     * To check whether the player is in a specific area, use [inArea].
     */
    var currentArea: SkyblockArea? = null
        private set(value) {
            if (value != field) {
                Mithras.EVENT_BUS.post(AreaChangeEvent(field, value))
                field = value
            }
        }

    /**
     * Keeps track of elapsed ticks, gets reset at 20
     */
    private var tickRamp = 0

    fun inArea(area: SkyblockArea): Boolean = currentArea === area

    @EventHandler
    fun onTick(event: ClientTickEvent) {
        if (event.phase != ClientTickEvent.Phase.START) return
        tickRamp++

        if (tickRamp % 20 == 0) {
            if (mc.player != null) {

                if (!inSkyblock) {
                    inSkyblock = onHypixel && mc.world?.scoreboard?.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR)
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
                if (inSkyblock && currentArea == null) {
                    currentArea = getArea()
                }

            }
            tickRamp = 0
        }
    }

    @EventHandler
    fun onDisconnect(event: ConnectionEvent.Disconnect) {
        onHypixel = false
        inSkyblock = false
        inDungeons = false
        currentArea = null
    }

    @EventHandler
    fun onWorldChange(@Suppress("UNUSED_PARAMETER") event: WorldChangeEvent) {
        inDungeons = false
        inSkyblock = false
        currentArea = null
        tickRamp = 18
    }

    /**
     * Taken from [SBC](https://github.com/Harry282/Skyblock-Client/blob/main/src/main/kotlin/skyblockclient/utils/LocationUtils.kt)
     */
    @EventHandler
    fun onConnect(event: ConnectionEvent.Join) {
        onHypixel = mc.runCatching {
            (((mc.player?.networkHandler as? ClientCommonNetworkHandlerAccessor)?.brand?.lowercase()?.contains("hypixel")
                ?: mc.currentServerEntry?.address?.lowercase()?.contains("hypixel")) == true)
        }.getOrDefault(false)
    }


    /**
     * Returns the current area from the tab list info.
     * If no info can be found return null.
     */
    private fun getArea(): SkyblockArea? {
        if (!inSkyblock) return null
        val netHandlerPlayClient: ClientPlayNetworkHandler = mc.player?.networkHandler ?: return null
        val list = netHandlerPlayClient.playerList ?: return null
        var area: String? = null
        var owner: String? = null
        for (entry in list) {
            //  "Area: Hub"
            val areaText = Formatting.strip(entry?.displayName?.string) ?: continue
            if (areaText.startsWith("Area: ")) {
                area = areaText.substringAfter("Area: ")
                if (!area.contains("Private Island")) break
            }
            if (areaText.contains("Owner:")){
                owner = areaText.substringAfter("Owner: ")
                break
            }
        }
        // TODO tab list does not show owner like this anymore therefore owner will always be null, probably can just remove it, or maybe check for own island is sufficient
        if (area == null) return null
        if (area.contains("Private Island")) {
            return SkyblockArea.PrivateIsland(owner)
        }
        return SkyblockArea.entries.find { it.areaName == area } ?: SkyblockArea.Unknown(area)
    }
}