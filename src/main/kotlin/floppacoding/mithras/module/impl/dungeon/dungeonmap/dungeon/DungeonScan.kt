package floppacoding.mithras.module.impl.dungeon.dungeonmap.dungeon

import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.module.impl.dungeon.dungeonmap.core.Room
import floppacoding.mithras.module.impl.dungeon.dungeonmap.core.RoomType
import floppacoding.mithras.module.impl.dungeon.dungeonmap.core.Tile
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Heightmap
import kotlin.math.min

/**
 * Scans the rooms rotations.
 *
 * @author Aton
 */
object DungeonScan {
    fun scanRoomRotations() {
        Dungeon.getDungeonTileList<Room>().forEach { room ->
            if (room.data.type == RoomType.BOSS) room.data.rotation = 0
            if (room.isUnique) {
                // scan the room rotation
                if (room.rotation == null) {
                    room.data.rotation = getAbsoluteRoomRotation(room)
                }
            }
        }
    }

    //TODO fix this, it does not work!

    /**
     * Returns the rotation of the given room based on the structure on top of the room.
     * It looks for a lapis Block in the corner of the roof of the room. If none is found it looks for the blue clay structure.
     * Rotations:
     * When the block is in the south east corner (15,15) -> 0 rotation.
     * When the block is in the south west corner (-15,15) -> 90 rotation.
     * When the block is in the north west corner (-15,-15) -> 180 rotation.
     * When the block is in the north east corner (15,-15) -> 270 rotation.
     */
    fun getAbsoluteRoomRotation(room: Tile): Int? {
        if(room !is Room) return null

        // At least one of the entrance rooms can be shifted by one block so the detection fails
        // Custom detection here for entrance room
        if (room.data.type == RoomType.ENTRANCE) {
            listOf(
                0 to -7,
                7 to 0,
                0 to 7,
                -7 to 0
            ).withIndex().forEach { (index, pair) ->
                if (mc.world!!.getBlockState(BlockPos(room.x + pair.first, 70 , room.z + pair.second)).block == Blocks.AIR) {
                    return index * 90
                }
            }

        }

        val tiles = Dungeon.getDungeonTileList<Room>().filter { it.data === room.data }
        // Check if the room is fully scanned already
        if (room.data.size != tiles.size) return null
        val corners = listOf(
            15 to 15,
            -15 to 15,
            -15 to -15,
            15 to -15
        )
        val roofY = getRoomRoofY(room)

        // First scan for the lapis Block
        for (tile in tiles) {
            corners.withIndex().forEach { (index, pair) ->
                if (!mc.world!!.isChunkLoaded((tile.x + pair.first) shr 4, (tile.z + pair.second) shr 4)) return null
                if ( mc.world!!.getBlockState(BlockPos(tile.x + pair.first, roofY, tile.z + pair.second)).block === Blocks.LAPIS_BLOCK ) {
                    return index * 90
                }
            }
        }

        // If no lapis Block was found scan for the blue clay structure which overrides it.
        // The color does not have to be checked, since the corners are either blue clay, redstone block or lapis block
        for (tile in tiles) {
            corners.withIndex().forEach { (index, pair) ->
                val pos = BlockPos(tile.x + pair.first, roofY, tile.z + pair.second)
                if (mc.world!!.getBlockState(pos).block === Blocks.BLUE_TERRACOTTA) {
                    // Here an additional check is needed to confirm that it is indeed the corner of the room. for 1x4 rooms this can be reacked without it being the room corner.
                    if (  ( mc.world!!.getBlockState(pos.south()).block === Blocks.BLUE_TERRACOTTA
                        ||  mc.world!!.getBlockState(pos.north()).block === Blocks.BLUE_TERRACOTTA)
                        && (mc.world!!.getBlockState(pos.west()).block  === Blocks.BLUE_TERRACOTTA
                         || mc.world!!.getBlockState(pos.east()).block  === Blocks.BLUE_TERRACOTTA)
                    ){
                        return index * 90
                    }
                }
            }
        }

        return null
    }

    private fun getRoomRoofY(room: Tile): Int {
        val chunk = mc.world!!.getChunk(room.x shr 4, room.z shr 4)
        // Here two checks are needed because at least of one room (Gold) there is a block on the roof in the center.
        val height1 = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE,room.x and 15, room.z and 15)
        val height2 = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE,room.x+1 and 15, room.z and 15)
        return min(height1, height2) -1
    }
}
