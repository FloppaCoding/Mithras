package floppacoding.mithras.module.impl.dungeon.dungeonmap.core

import com.google.gson.annotations.SerializedName

/**
 * Data for rooms retrieved from rooms.json.
 *
 * Not to be confused with [RoomData] which contains all data for the room.
 */
data class RoomConfigData(
    val name: String,
    val type: RoomType,
    val shape: RoomShape,
    val doors: DoorLayout?,
    val secrets: Int,
    val crypts: Int,
    @SerializedName("revive_stones")
    val reviveStones: Int,
    val journals: Int,
    @SerializedName("spiders")
    val hasLonelySpiders: Boolean,
    @SerializedName("secret_details")
    val secretDetails: SecretDetails?,
    @SerializedName("soul")
    val hasFairySoul: Boolean,
    @SerializedName("id")
    val scoreboardIDs: List<String>,
    val cores: List<Int>?,
    @SerializedName("secret_coords")
    val secretPositions: SecretPositions?
)
