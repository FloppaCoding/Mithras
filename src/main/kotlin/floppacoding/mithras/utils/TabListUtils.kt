package floppacoding.mithras.utils

import com.google.common.collect.ComparisonChain
import floppacoding.mithras.Mithras.mc
import net.minecraft.client.network.PlayerListEntry
import net.minecraft.world.GameMode

object TabListUtils {

    private val tabListOrder = Comparator<PlayerListEntry> { o1, o2 ->
        if (o1 == null) return@Comparator -1
        if (o2 == null) return@Comparator 0
        return@Comparator ComparisonChain.start().compareTrueFirst(
            o1.gameMode != GameMode.SPECTATOR,
            o2.gameMode != GameMode.SPECTATOR
        ).compare(
            o1.scoreboardTeam?.displayName?.string ?: "",
            o2.scoreboardTeam?.displayName?.string ?: ""
        ).compare(o1.profile.name, o2.profile.name).result()
    }

    // TODO cache this value maybe?
    // Alternatively mc.inGameHud.playerListHud.collectPlayerEntries() can be used with an accessor mixin.
    /**
     * Returns un formatted tab list.
     */
    val tabList: List<Pair<PlayerListEntry, String>>
        get() = (mc.player?.networkHandler?.listedPlayerListEntries?.sortedWith(tabListOrder) ?: emptyList())
            .map { Pair(it, mc.inGameHud.playerListHud.getPlayerName(it).string) }

}