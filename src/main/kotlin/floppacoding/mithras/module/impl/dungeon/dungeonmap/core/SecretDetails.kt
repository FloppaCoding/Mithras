package floppacoding.mithras.module.impl.dungeon.dungeonmap.core

import com.google.gson.annotations.SerializedName

// This class is a bit inconvenient right now.
// That is because I cba to rename all the occurrences of it in the rooms.json.
class SecretDetails (
    @SerializedName("wither")
    val witherEssences: Int,
    @SerializedName("redstone_key")
    val redstoneKeys: Int,
    @SerializedName("bat")
    val bats: Int,
    @SerializedName("item")
    val items: Int,
    @SerializedName("chest")
    val chests: Int
)
