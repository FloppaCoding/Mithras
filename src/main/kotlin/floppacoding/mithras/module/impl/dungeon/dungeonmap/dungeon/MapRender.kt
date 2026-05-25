package floppacoding.mithras.module.impl.dungeon.dungeonmap.dungeon

import floppacoding.aurora.core.TextAlign
import floppacoding.aurora.core.images.Image
import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.module.impl.dungeon.DungeonMap
import floppacoding.mithras.module.impl.dungeon.MapRooms
import floppacoding.mithras.module.impl.dungeon.dungeonmap.core.*
import floppacoding.mithras.module.impl.dungeon.dungeonmap.utils.MapUtils
import floppacoding.mithras.module.impl.dungeon.dungeonmap.utils.MapUtils.roomSize
import floppacoding.mithras.ui.hud.EditHudGUI
import floppacoding.mithras.ui.hud.HudElement
import floppacoding.mithras.utils.Extensions.equalsOneOf
import floppacoding.mithras.utils.Extensions.withAlpha
import floppacoding.mithras.utils.LocationManager.inDungeons
import floppacoding.mithras.utils.inventory.InventoryUtils.isHoldingInMainHand
import floppacoding.mithras.utils.inventory.SkyblockItem
import floppacoding.mithras.utils.render.ImageManager
import net.minecraft.client.gui.DrawContext
import net.minecraft.entity.player.PlayerModelPart
import java.awt.Color

object MapRender: HudElement(
    DungeonMap.xHud,
    DungeonMap.yHud,
    128f,
    138f,
    DungeonMap.mapScale
){

    override fun renderHud(context: DrawContext) {

        if (!inDungeons) return
        if (DungeonMap.hideInBoss.enabled && Dungeon.inBoss) return
        // Background
        renderer.roundedRect(
            0.0f,
            0.0f,
            128.0f,
            if (DungeonMap.showRunInformation.enabled) 142.0f else 128.0f,
            4f,
            DungeonMap.mapBackground.value.rgb
        )
        // Border
        val borderFunction = if (DungeonMap.chromaBorder.enabled) renderer::chromaBorder else renderer::border
        borderFunction(
            0.0f,
            0.0f,
            128.0f,
            if (DungeonMap.showRunInformation.enabled) 142.0f else 128.0f,
            DungeonMap.mapBorderWidth.value,
            4f,
            DungeonMap.mapBorder.value.rgb
        )
        // Run Information
        if (mc.currentScreen !is EditHudGUI) {
            if (DungeonMap.showRunInformation.enabled) {
                renderRunInformation()
            }
        }
        // Scissor
        renderer.push()
        renderer.scissor(0f, 0f, width, 128f)
        // Spinny map
        if (DungeonMap.spinnyMap.enabled || DungeonMap.centerOnPlayer.enabled) {
            renderer.translate(64.0f, 64.0f)
            if (DungeonMap.spinnyMap.enabled) renderer.rotate(-mc.player!!.headYaw + 180f)
        }
        // Room scale
        renderer.scale(DungeonMap.roomScale.value, DungeonMap.roomScale.value)
        // Centering
        if (DungeonMap.centerOnPlayer.enabled) {
            renderer.translate(
                -((mc.player!!.x - Dungeon.START_X + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.first - 2),
                -((mc.player!!.z - Dungeon.START_Z + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.second - 2)
            )
        }else if (DungeonMap.spinnyMap.enabled){
            renderer.translate(-64.0f, -64.0f)
        }

        renderRooms()

        if (mc.currentScreen !is EditHudGUI) {
            renderCheckmarkAndText()
            renderPlayerHeads()
        }

        renderer.endScissor()
        renderer.pop()
    }

    private fun renderRooms() {
        renderer.push()
        renderer.translate(MapUtils.startCorner.first.toFloat(), MapUtils.startCorner.second.toFloat(),)

        val connectorSize = roomSize shr 2

        for (x in 0..10) {
            for (y in 0..10) {
                val tile = Dungeon.getDungeonTile(x, y) ?: continue
                if (tile.state == RoomState.UNDISCOVERED && !tile.visited) continue

                val xOffset = (x shr 1) * (roomSize + connectorSize)
                val yOffset = (y shr 1) * (roomSize + connectorSize)

                val xEven = x and 1 == 0
                val yEven = y and 1 == 0

                val color = if (MapRooms.darkenUndiscovered.enabled && !tile.state.revealed) {
                    tile.color.run {
                        Color(
                            (red   * (1 - MapRooms.mapDarkenPercent.value)).toInt(),
                            (green * (1 - MapRooms.mapDarkenPercent.value)).toInt(),
                            (blue  * (1 - MapRooms.mapDarkenPercent.value)).toInt(),
                            (alpha * MapRooms.mapRoomTransparency.value).toInt()
                        )
                    }
                } else tile.color.run { withAlpha((alpha * MapRooms.mapRoomTransparency.value).toInt()) }

                when {
                    xEven && yEven -> if (tile is Room) { // rooms
                        renderer.roundedRect(
                            xOffset.toFloat(),
                            yOffset.toFloat(),
                            roomSize.toFloat(),
                            roomSize.toFloat(),
                            2f,
                            color.rgb
                        )
                    }
                    !xEven && !yEven -> { // the spot at the corner in between rooms. has to be filled for 2x2. When it is empty tile is null and this point will not be reached
                        // Box covers the room to the top left again but is bigger than the room to fill the spaces in between.
                        // bigger than roomSize+connectorSize to cover rounded corners of neighbouring room!
                        renderer.roundedRect(
                            xOffset.toFloat(),
                            yOffset.toFloat(),
                            (roomSize + connectorSize*2).toFloat(),
                            (roomSize + connectorSize*2).toFloat(),
                            2f,
                            color.rgb
                        )
                    }
                    else -> drawRoomConnector(
                        xOffset,
                        yOffset,
                        connectorSize,
                        tile is Door,
                        !xEven,
                        color
                    )
                }
            }
        }
        renderer.pop()
    }

    /**
     * Draws all the information for the room which is displayed over the solid color tiles.
     * This includes: Checkmarks, the question mark for unexplored rooms, room names and secret count.
     */
    private fun renderCheckmarkAndText() {
        renderer.push()
        renderer.translate(MapUtils.startCorner.first.toFloat(), MapUtils.startCorner.second.toFloat())

        val connectorSize = roomSize shr 2
        val showCheckmarks = MapRooms.mapCheckmark.value != MapRooms.CheckmarkMode.NONE && MapRooms.mapRoomSecrets.value != MapRooms.SecretsMode.REPLACE_CHECKMARK

        for (x in 0..10 step 2) {
            for (y in 0..10 step 2) {

                val room = Dungeon.getDungeonTile<Room>(x, y) ?: continue

                // filter whether the information should be visible.
                if (room.state == RoomState.UNDISCOVERED && !room.visited) continue

                val xOffset = (x shr 1) * (roomSize + connectorSize)
                val yOffset = (y shr 1) * (roomSize + connectorSize)
                // Render the checkmark
                if (showCheckmarks) {
                    if (room.isUnique || (room.state == RoomState.QUESTION_MARK)) {

                        getCheckmark(room)?.let {
                            renderer.image(it, xOffset+2f, yOffset+2f, roomSize-4f, roomSize-4f)
                        }
                    }
                }
                //Render the name and secrets
                if (room.isUnique) {

                    val name = mutableListOf<String>()
                    val showName = run {
                        (MapRooms.mapRoomNames.value!= MapRooms.RoomNameMode.NONE && room.data.type == RoomType.PUZZLE
                                || (MapRooms.mapRoomNames.value == MapRooms.RoomNameMode.ALL && room.data.type.equalsOneOf(
                                    RoomType.NORMAL,
                                    RoomType.RARE,
                                    RoomType.CHAMPION,
                                    RoomType.TRAP
                                ) && room.visited))
                                && (room.state.revealed || room.visited)
                    } && !room.data.name.startsWith("Unknown")

                    if (showName) {
                        name.addAll(room.data.name.split(" "))
                    }
                    // Room secrets if visible.
                    getRoomSecerts(room)?.let {
                        name.add(it)
                    }

                    val color = if (MapRooms.mapColorText.enabled) when (room.state) {
                        RoomState.GREEN -> Color(85, 255, 85).rgb
                        RoomState.CLEARED, RoomState.FAILED -> -1
                        else -> Color(170, 170, 170).rgb
                    } else -1

                    // Offset + half of roomsize
                    renderer.text(name.joinToString(separator = " "),
                        xOffset.toFloat() + roomSize *0.4f, yOffset.toFloat() + roomSize*0.1f, color,
                        renderer.defaultFontHeight*DungeonMap.textScale.value,
                        textAlign = TextAlign.CENTER_TOP,
                        splitWidth = roomSize.toFloat() * 1.3f
                    )
                    // TODO make this split lines or just a string
                }
            }
        }
        renderer.pop()
    }

    /**
     * Returns the resource location for the rooms Checkmark.
     * This is for white and green checkmarks, the red cross that is shown for failed puzzles and the question mark
     * for unexplored rooms.
     */
    private fun getCheckmark(room: Room): Image? {
        return when (MapRooms.mapCheckmark.value) {
            MapRooms.CheckmarkMode.DEFAULT -> when (room.state) {
                RoomState.CLEARED -> ImageManager.DEFAULT_WHITE
                RoomState.GREEN   -> ImageManager.DEFAULT_GREEN
                RoomState.FAILED  -> ImageManager.DEFAULT_CROSS
                RoomState.QUESTION_MARK -> {
                    if (!room.visited)
                        ImageManager.DEFAULT_QUESTION
                    else null
                }
                else -> null
            }
            MapRooms.CheckmarkMode.NEU -> when (room.state) {
                RoomState.CLEARED -> ImageManager.NEU_WHITE
                RoomState.GREEN   -> ImageManager.NEU_GREEN
                RoomState.FAILED  -> ImageManager.NEU_CROSS
                RoomState.QUESTION_MARK -> {
                    if (!room.visited)
                        ImageManager.NEU_QUESTION
                    else null
                }
                else -> null
            }
            else -> null
        }
    }

    /**
     * Checks whether secrets should be visible for the given room and returns a String according to the settings
     * containing information about the rooms secrets or null if nothing should be shown.
     */
    private fun getRoomSecerts(room: Room): String? {
        if (MapRooms.mapRoomSecrets.value == MapRooms.SecretsMode.OFF ) return null
        val shouldShowSecrets = if (room.state == RoomState.QUESTION_MARK) {
            false
        } else when(room.data.type) {
            RoomType.NORMAL, RoomType.RARE, RoomType.TRAP -> true
            RoomType.PUZZLE -> room.data.name.contains(puzzleWithSecretsRegex)
            else -> false
        }
        if (shouldShowSecrets) {
            val maxSecrets = if (room.visited)
                room.data.maxSecrets?.toString() ?: "?"
            else "?"
            return if (DungeonMap.trackSecrets.enabled)
                "${room.data.currentSecrets}/${maxSecrets}"
            else
                maxSecrets
        }
        return null
    }

    private fun renderPlayerHeads() {
        // Try catch in case the dungeonTeammates get updated in a coroutine.
        try {
            for (player in Dungeon.dungeonTeammates) {
                drawPlayerHead(player)
            }
        }catch (_: ConcurrentModificationException) {}
    }

    private fun drawRoomConnector(x: Int, y: Int, doorWidth: Int, doorway: Boolean, vertical: Boolean, color: Color) {
        val doorwayOffset = if (roomSize == 16) 5 else 6
        val width = if (doorway) 6 else roomSize
        val height = if (doorway) doorWidth else 2*doorWidth
        var x1 = if (vertical) x + roomSize else x
        var y1 = if (vertical) y else y + roomSize
        if (doorway) {
            if (vertical) y1 += doorwayOffset else x1 += doorwayOffset
        }else  {
            if (vertical) x1 -= doorWidth / 2 else y1 -= doorWidth / 2
        }
        renderer.rect(
            x1.toFloat(), y1.toFloat(),
            (if (vertical) height else width).toFloat(),
            (if (vertical) width else height).toFloat(),
            color.rgb
        )
    }

    /**
     * Renders information about the current run underneath the map.
     */
    private fun renderRunInformation() {
        renderer.push()
        renderer.translate(0f, 128f)
        renderer.scale(0.66f, 0.66f)
        val totalSecrets = RunInformation.totalSecrets ?: "?"

        renderer.text("Secrets: ${RunInformation.secretCount}/${totalSecrets}", 5f, 0f, -1)
        renderer.text("Crypts: ${RunInformation.cryptsCount}", 85f, 0f, -1)
        renderer.text("Deaths: ${RunInformation.deathCount}", 140f, 0f, -1)
        // Second Line
        renderer.text("Score: ${RunInformation.score}", 5f, renderer.defaultFontHeight + 1 , -1)
        renderer.pop()
    }

    /**
     * Renders the player heads for funny map. Has the scaling directly integrated.
     */
    fun drawPlayerHead(player: DungeonPlayer) {
        if (player.dead || player.player == null) return
        renderer.push()
        try {
            if (player.player == mc.player) {
                renderer.translate(
                    (mc.player!!.x - Dungeon.START_X + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.first - 2,
                    (mc.player!!.z - Dungeon.START_Z + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.second - 2
                )
            } else {
                renderer.translate(player.mapX, player.mapZ)
            }

            if (DungeonMap.playerNameMode.value == DungeonMap.NameMode.ALWAYS || DungeonMap.playerNameMode.value == DungeonMap.NameMode.HOLDING_LEAP
                 && mc.player.isHoldingInMainHand(SkyblockItem.SPIRIT_LEAP, SkyblockItem.INFINILEAP)
            ) {
                renderer.push()
//                renderer.scale(0.8f, 0.8f)
                if (DungeonMap.spinnyMap.enabled) renderer.rotate(mc.player!!.headYaw + 180f)
                renderer.text(
                    player.name, 0f, 10f * DungeonMap.playerHeadScale.value, -1,
                    renderer.defaultFontHeight*DungeonMap.textScale.value,
                    textAlign = TextAlign.CENTER_TOP,
                )
                renderer.pop()
            }
            if (player.player == mc.player) {
                renderer.rotate(mc.player!!.headYaw + 180f)
            } else {
                renderer.rotate(player.yaw + 180f)
            }
            renderer.scale(DungeonMap.playerHeadScale.value, DungeonMap.playerHeadScale.value)
            renderer.border(-6.0f, -6.0f, 12.0f, 12.0f, 2.0f, 1f, Color(0, 0, 0, 255).rgb)
            val skinImage  = player.skinImage ?: return
            renderer.roundedImage(skinImage,-6f, -6f, 12f, 12f, 1f, 8f, 8f, 8f, 8f)
            if (player.player.isModelPartVisible(PlayerModelPart.HAT)) {
                renderer.roundedImage(skinImage,-6f, -6f, 12f, 12f, 2f, 40f, 8f, 8f, 8f)
            }
        } catch (_: Exception) {
        }
        renderer.pop()
    }

    private val puzzleWithSecretsRegex = Regex("Higher|Blaze|Tic")
}
