package floppacoding.mithras.module.impl.dungeon.dungeonmap.core

import floppacoding.aurora.core.images.Image
import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.module.impl.dungeon.dungeonmap.dungeon.Dungeon
import floppacoding.mithras.module.impl.dungeon.dungeonmap.utils.MapUtils
import floppacoding.mithras.module.impl.dungeon.dungeonmap.utils.MapUtils.mapX
import floppacoding.mithras.module.impl.dungeon.dungeonmap.utils.MapUtils.mapZ
import floppacoding.mithras.module.impl.dungeon.dungeonmap.utils.MapUtils.yaw
import floppacoding.mithras.utils.render.ImageManager
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.item.map.MapDecoration

/**
 * Class to store information about dungeon teammates.
 * The Player will also be handled in this way as a teammate.
 *
 * This class also contains methods to update and track teammate data.
 * [updatePlayerAndRoom] handles updating the found secrets on the map.
 *
 * @author Aton
 */
class DungeonPlayer(
    var player: AbstractClientPlayerEntity, var name: String,
                    /**
                     * True when the field player is not the correct entity corresponding to this Player.
                     */
    var fakeEntity: Boolean = false
) {
    var mapX = 0.0
    var mapZ = 0.0
    var yaw = 0f
    var icon = ""
    var dead = false
    var deaths = 0
    var secretsAtRunStart: Int? = null


    // TODO add abstraction here for general image.
    var skinImage: Image? = try {
        ImageManager.createSkinImage(player.skin.body.id())  } catch (e: ClassCastException){null}
        get() {
            if (field == null) try{
                ImageManager.createSkinImage(player.skin.body.id())
            }catch (_: ClassCastException) {
            }
            return field
        }
        private set

    /**
     * Stores the index of the room the player is currently in within the [Dungeon.dungeonList].
     * This index will still point to the correct tile even when the tile is overwritten by the scan.
     * Get the corresponding Room from [currentRoom].
     *
     * There is no check what kind of tile this is.
     * Will also hold an index when no room is loaded in for the tile yet.
     *
     * This index is calculated as column * 11 + row.
     */
    private var currentRoomIndex: Int? = null

    /**
     * Gets the room this DungeonPlayer is currently in.
     * Does not include boss room.
     * This method is meant to be used to track the position of the dungeon teammates and not the Player.
     *
     * Not to be confused with [Dungeon.currentRoom].
     */
    val currentRoom: Room?
        get() = (currentRoomIndex?.let{ Dungeon.getDungeonTileList()[it]} as? Room)

    /**
     * Maps the index of the tile in [Dungeon.dungeonList] to the count of ticks the player spent in that Tile.
     * The key -1 is used for time spent dead in clear.
     * @see currentRoomIndex
     */
    val visitedTileTimes: MutableMap<Int, Int> = mutableMapOf()

    private var lastSecretCheck: SecretCheck = SecretCheck(null, null, null, 0L)
    private var pending: Boolean = false

/*    init {
        if (!fakeEntity && (PartyTracker.enabled || DungeonMap.trackSecrets.enabled)) {
            scope.launch(Dispatchers.IO) { secretsAtRunStart = fetchTotalSecretsFromApi() }
        }
    }*/

    fun loadSkinImage() {
        try {
            skinImage = ImageManager.createSkinImage(player.skin.body.id())
        }catch (_: ClassCastException){}
    }

    /**
     * Updates the teammates position and the secrets in the room they are in.
     */
    fun updatePlayerAndRoom(decor: Map<String, MapDecoration>?) {
        // Update the position in the world
        val player = mc.world?.players?.find { it.name.string == this.name }
        // when the player is in render distance, use that data instead of the map item
        if (player != null) {
            // check whether the player is in the map; probably not needed
            if ( player.x > -200 && player.x < -10 && player.z > -200 && player.z < -10) {
                this.mapX = (player.x - Dungeon.START_X + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.first - 2
                this.mapZ = (player.z - Dungeon.START_Z + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.second - 2
                this.yaw = player.headYaw
            }
        }else {
            //if no data from the map item is present go to the next player
            if (decor != null) {
                decor.entries.find { (iconName, _) -> iconName == this.icon }?.let { (_, icon) ->
                    this.mapX = icon.mapX.toDouble()
                    this.mapZ = icon.mapZ.toDouble()
                    this.yaw = icon.yaw
                }
            }
        }

        // Update the current room and info about it.
//        val newIndex = getCurrentRoomIndex()
//        val oldRoom = currentRoom
//        val shouldUpdateSecrets = DungeonMap.trackSecrets.enabled &&  Dungeon.hasRunStarted &&  !pending &&
//                (System.currentTimeMillis() > lastSecretCheck.timeMS + 5000
//                        || ( newIndex != currentRoomIndex && oldRoom?.data?.name != (newIndex?.let{ Dungeon.getDungeonTileList()[it]} as? Room)?.data?.name ))
//        currentRoomIndex = newIndex
//        updateVisitedTileTimes()
//        if (shouldUpdateSecrets ) {
//            updateRoomSecrets(oldRoom)
//        }
    }

/*
    */
/**
     * Updates the secrets within [oldRoom] from the total collected secrets if possible.
     * @param oldRoom the room the player was in previously.
     *//*

    private fun updateRoomSecrets(oldRoom: Room?) {
        scope.launch(Dispatchers.IO) {
            if (oldRoom == null && currentRoom == null) return@launch
            val oldSecretCheck = lastSecretCheck
            pending = true
            val newSecrets = fetchTotalSecretsFromApi(oldRoom)
            pending = false
            if (oldRoom != null && oldRoom.data.name == oldSecretCheck.newRoom?.data?.name && newSecrets != null && oldSecretCheck.secrets != null) {
                val difference = newSecrets - oldSecretCheck.secrets
                oldRoom.data.currentSecrets += difference
            }
        }
    }

    */
/**
     * Increments the tick count this player spent in the current Tile in [visitedTileTimes].
     * Dead time is counted with index -1.
     *//*

    private fun updateVisitedTileTimes() {
        val index = if (dead) -1 else currentRoomIndex ?: return
        visitedTileTimes[index] = (visitedTileTimes[index] ?: 0) + 1
    }
*/

/*
    */
/**
     * Return the index of the room the player is currently in within the [Dungeon.dungeonList].
     * This index will still point to the correct tile even when the tile is overwritten by the scan.
     *
     * There is no check what kind of tile this is.
     * Will also return an index when no room is loaded in for the tile yet.
     *//*

    @JvmName("getCurrentRoomIndexFromCoordinates")
    private fun getCurrentRoomIndex(): Int? {
        if (Dungeon.inBoss) return null
        // Note the shr 5 ( / 32 ) instead of the usual shr 4 here. This ensures that only rooms can be pointed to.
        // But also means that the x and z values here are half of the column and row.
        val x = (((mapX + 2 - MapUtils.startCorner.first) / MapUtils.coordMultiplier ).toInt() shr 5)
        val z = (((mapZ + 2 - MapUtils.startCorner.second) / MapUtils.coordMultiplier).toInt() shr 5)
        if (x<0 || x > 5 || z < 0 || z > 5) return null
        return x * 22 + z * 2
    }
*/

    /**
     * Class to store the data from the last time the secret count was checked for this player from the api.
     */
    private data class SecretCheck(
        val secrets: Int?,
        /** The room the player is currently in.*/
        val newRoom: Room?,
        /** When changing room, this is the room the player was in last. Otherwise null. */
        val oldRoom: Room?,
        val timeMS: Long
    )
}
