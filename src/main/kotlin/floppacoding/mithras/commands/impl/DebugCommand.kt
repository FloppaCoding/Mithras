package floppacoding.mithras.commands.impl

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import floppacoding.aurora.core.Aurora
import floppacoding.mithras.Mithras
import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.commands.CmdSource
import floppacoding.mithras.commands.Command
import floppacoding.mithras.module.impl.dungeon.dungeonmap.core.Room
import floppacoding.mithras.module.impl.dungeon.dungeonmap.dungeon.ConfigRoom
import floppacoding.mithras.module.impl.dungeon.dungeonmap.dungeon.Dungeon
import floppacoding.mithras.module.impl.dungeon.dungeonmap.dungeon.DungeonScan
import floppacoding.mithras.module.impl.dungeon.dungeonmap.dungeon.RunInformation
import floppacoding.mithras.module.impl.dungeon.dungeonmap.utils.MapUtils
import floppacoding.mithras.module.impl.dungeon.dungeonmap.utils.RoomUtils
import floppacoding.mithras.shaders.Shaders
import floppacoding.mithras.ui.other.Test
import floppacoding.mithras.ui.other.Test2
import floppacoding.mithras.ui.other.Test3
import floppacoding.mithras.utils.*
import floppacoding.mithras.utils.inventory.NBTStringWriter
import floppacoding.mithras.utils.network.BazaarAPI
import floppacoding.mithras.utils.network.LowestBinAPI
import kotlinx.coroutines.launch
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.Entity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.decoration.ItemFrameEntity
import net.minecraft.item.FilledMapItem
import net.minecraft.scoreboard.ScoreboardDisplaySlot
import net.minecraft.text.HoverEvent
import net.minecraft.text.HoverEvent.ShowEntity
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.math.Box
import java.awt.Font
import kotlin.experimental.and

object DebugCommand : Command() {
    override val builder: LiteralArgumentBuilder<CmdSource> =
        command("mdebug") {
            literal("data") {
                literal("scoreboard") {
                    execute {
                        ChatUtils.chatMessage("Printing scorebaord to logs.")
                        ScoreboardUtils.sidebarLines.forEach {
                            Mithras.logger.info(it + "; cleaned:" + ScoreboardUtils.cleanSB(it) )
                        }
                    }
                    literal("1") {
                        execute {
                            val scoreboard = mc.world?.scoreboard ?: return@execute
                            val objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR) ?: return@execute
                            var scores = scoreboard.getScoreboardEntries(objective)
                            scores = scores.filter {
                                it?.hidden() == false
                            }.let {
                                if (it.size > 15) it.drop(15) else it
                            }
                            scores.forEach {
                                ChatUtils.chatMessage(it.name())
                            }
                        }
                    }
                }
                literal("tablist") {
                    execute {
                        ChatUtils.chatMessage("Printing tab list to logs.")
                        val tablist = TabListUtils.tabList
                        tablist.forEach {
                            Mithras.logger.info("${it.second}; skin path: ${it.first.skinTextures.texture.path}")
                        }
                    }
                }
                literal("where") {
                    execute { ChatUtils.chatMessage(
                        "in Dungeon: ${LocationManager.inDungeons}, on Hypixel: ${LocationManager.onHypixel}, " +
                                "in skyblock: ${LocationManager.inSkyblock}"
                    ) }
                }
            }
            literal("dungeon") {
                literal("currentRoom") {
                    execute {
                        val room = Dungeon.currentRoom
                        if (room == null) {
                            ChatUtils.chatMessage("Not in Room.")
                            return@execute
                        }
                        ChatUtils.chatMessage("${room.data.name}: ${room.x}, ${room.z}, rotation: ${room.rotation}")
                    }
                }
                literal("rotation") {
                    execute {
                        val room = Dungeon.currentRoom
                        if (room == null) {
                            ChatUtils.chatMessage("Not in Room.")
                            return@execute
                        }
                        val rotation = DungeonScan.getAbsoluteRoomRotation(room)
                        if (rotation == null) {
                            ChatUtils.chatMessage("Not rotation found.")
                            return@execute
                        }
                        ChatUtils.chatMessage("$rotation")
                    }
                }
                literal("floor") {
                    execute {
                        ChatUtils.chatMessage("Currently in floor: ${RunInformation.currentFloor}")
                    }
                }
                literal("rooms") {
                    execute {
                        ChatUtils.chatMessage("Printing list of rooms to logs.")
                        Dungeon.getDungeonTileList<Room>().forEach {
                            Mithras.logger.info("${it.data.name} at ${it.x}, ${it.z}; is Unique: ${it.isUnique}; state: ${it.state}")
                        }
                    }
                }
                literal("hasrunstarted") {
                    execute { ChatUtils.chatMessage(Dungeon.hasRunStarted.toString()) }
                }
                literal("calibrated") {
                    execute { ChatUtils.chatMessage(MapUtils.calibrated.toString()) }
                }
                literal("getFloor") {
                    execute {
                        var temp: RunInformation.Floor? = null
                        ScoreboardUtils.sidebarLines.forEach {
                            val line = ScoreboardUtils.cleanSB(it)
                            when {
                                line.contains("The Catacombs (") -> {
                                    ChatUtils.chatMessage(line)
                                    temp = try {
                                        RunInformation.Floor.valueOf(line.substringAfter("(").substringBefore(")"))
                                    }catch (_ : IllegalArgumentException) { null }
                                }
                            }
                        }
                        ChatUtils.chatMessage(temp?.name?: "null")
                    }
                }
                literal("tabinfo") {
                    execute {
                        val tabEntries = Dungeon.getDungeonTabList() ?: run {
                            ChatUtils.chatMessage(if (!LocationManager.inDungeons) "Not in Dungeon." else "Tab list does not match expected format.")
                            return@execute
                        }
                        var readingPuzzles = false
                        tabEntries.forEach { pair ->
                            val text = pair.second
                            when {
                                readingPuzzles -> {
                                    val matcher = RunInformation.puzzlePattern.find(text) ?: return@forEach Unit.also {
                                        ChatUtils.chatMessage("Stopped reading puzzles on line: \"$text\".")
                                        readingPuzzles = false
                                    }
                                    matcher.groups["puzzle"]?.value?.let { name ->
                                        val state: Boolean? = when (matcher.groups["state"]?.value) {
                                            "✔" -> true
                                            "✖" -> false
                                            else -> null
                                        }
                                        ChatUtils.chatMessage("Detected puzzle state \"$state\" for puzzle: \"$name\"")
                                    }
                                }
                                text.contains("Deaths: ") -> {
                                    val matcher = RunInformation.deathsPattern.find(text) ?: return@forEach Unit.also {
                                        ChatUtils.chatMessage("§eError§r: Expected death count but line \"$text\" did not match.")
                                    }
                                    ChatUtils.chatMessage("Detected death count: ${matcher.groups["deaths"]?.value?.toIntOrNull()}")
                                }
                                text.contains("Secrets Found: ") -> {
                                    if (text.contains("%")) {
                                        val matcher = RunInformation.secretsFoundPercentagePattern.find(text) ?: return@forEach Unit.also {
                                            ChatUtils.chatMessage("§eError§r: Expected secret percentage but line \"$text\" did not match.")
                                        }
                                        ChatUtils.chatMessage("Detected secret percentage ${matcher.groups["percentage"]?.value?.toDoubleOrNull()}%.")
                                    } else {
                                        val matcher = RunInformation.secretsFoundPattern.find(text) ?: return@forEach Unit.also {
                                            ChatUtils.chatMessage("§eError§r: Expected secret count but line \"$text\" did not match.")
                                        }
                                        ChatUtils.chatMessage("Detected secret count ${matcher.groups["secrets"]?.value?.toIntOrNull()}.")
                                    }
                                }
                                text.contains("Crypts: ") -> {
                                    val matcher = RunInformation.cryptsPattern.find(text) ?: return@forEach Unit.also {
                                        ChatUtils.chatMessage("§eError§r: Expected crypt count but line \"$text\" did not match.")
                                    }
                                    ChatUtils.chatMessage("Detected crypt count ${matcher.groups["crypts"]?.value?.toIntOrNull()}.")
                                }
                                text.contains("Puzzles: ") -> {
                                    ChatUtils.chatMessage("Starting to read puzzles.")
                                    readingPuzzles = true
                                }
                            }
                        }
                    }
                }
                literal("teammates") {
                    execute {
                        Dungeon.dungeonTeammates.forEach {
                            ChatUtils.chatMessage("${it.name}; is fake: ${it.fakeEntity}; is the player: ${it.player == mc.player}; skin path: ${it.player.skinTextures.texture.path}")
                        }
                    }
                }
                literal("reload-skins") {
                    execute {
                        Dungeon.dungeonTeammates.forEach {
                            it.loadSkinImage()
                        }
                    }
                }
                literal("mapcolors") {
                    execute {
                        val colors = MapUtils.getMapData()?.colors ?: return@execute Unit.also { ChatUtils.chatMessage("Could not get map info.") }
                        ChatUtils.chatMessage("Printing map data to logs.")
                        for (row in 0..127) {
                            Mithras.logger.info(colors.copyOfRange(row*128, (row+1)*128).joinToString(",","row $row:: ") { it.toString() })
                        }
                    }
                }
                literal("roomId") {
                    execute{
                        ChatUtils.chatMessage(RoomUtils.getRoomScoreboardID()?: "null")
                    }
                }
                literal("inroom") {
                    string("roomName") {
                        execute {context ->
                            val roomName = context.getString("roomName")
                            val configData = RoomUtils.roomList.find { it.name == roomName }
                            if (configData == null) {
                                ChatUtils.chatMessage("Room not found in config.")
                                return@execute
                            }
                            val inRoom = RoomUtils.isInRoom(configData)
                            ChatUtils.chatMessage(inRoom.toString())
                        }
                    }
                }
                literal("gettttframes") {
                    execute {
                        val room = Dungeon.currentRoom
                        if (room == null) {
                            ChatUtils.chatMessage("Not in room.")
                            return@execute
                        }
                        if (!RoomUtils.isInRoom(ConfigRoom.TIC_TAC_TOE)) {
                            ChatUtils.chatMessage("Not in TTT.")
                            return@execute
                        }

                        val tttSerachBox = Box(room.x - 12.0, 69.0, room.z - 12.0, room.x + 12.0, 80.0, room.z + 12.0)

                        val frames = mc.world!!.getEntitiesByClass(ItemFrameEntity::class.java, tttSerachBox) filter@{
                            return@filter true
                        }
                        frames.forEach {
                            val realPos = it.blockPos
                            val itemFrameHeldStack = it.heldItemStack
                            val mapData = FilledMapItem.getMapState(itemFrameHeldStack, mc.world)
                            val colorInt: Int? =
                                if (mapData != null) (mapData.colors[8256] and 255.toByte()).toInt() else null
                            val blockBehind = realPos.offset(it.horizontalFacing.opposite, 1)
                            ChatUtils.chatMessage("${it.x}, ${it.y}, ${it.z}, realpos: $realPos, heldStack: $itemFrameHeldStack, color: $colorInt, blockBehind: $blockBehind")
                        }
                    }
                }
            }
            literal("player") {
                literal("loadskin") {
                    execute {
//                        val skin = mc.networkHandler?.getPlayerListEntry(mc.player?.uuid)?.skinTexture ?: return@execute
//                        try {
//                            val skinImage =  ImageManager.createImage(skin)
//                            ChatUtils.chatMessage(skinImage.glID.toString())
//                        }catch (e: IOException) {
//                            ChatUtils.chatMessage("failed creating image")
//                        }

                    }
                }
                literal("checkskin") {
                    string("name") {
                        execute { context ->
//                            val tabEntries = TabListUtils.tabList
//                            val player = tabEntries.find { it.second.contains(StringArgumentType.getString(context, "name")) }
//                            if (player == null) {
//                                ChatUtils.chatMessage("No player matching \"${StringArgumentType.getString(context, "name")}\" found.")
//                                return@execute
//                            }
//                            val name = (player.first.displayName?.string ?: "null") + " - " + player.second
//                            var texture = player.first.skinTexture
//                            if (texture == null) {
//                                ChatUtils.chatMessage("Skin texture not found for ${name}!")
//                                return@execute
//                            }
//                            var resource = mc.resourceManager.getResource(texture)
//                            if (resource.isEmpty) {
//                                ChatUtils.chatMessage("No resource present for ${name}, path:  ${texture.namespace}:${texture.path}. Trying to reload.")
//                                texture = mc.skinProvider.loadSkin(player.first.profile)
//                                resource = mc.resourceManager.getResource(texture)
//                                if (resource.isEmpty) {
//                                    ChatUtils.chatMessage("Reloading resource failed for ${texture.namespace}:${texture.path}. Trying to create from texture.")
////                                val profileTexture = mc.sessionService.getTextures(player.first.profile, false)
////                                    .get(MinecraftProfileTexture.Type.SKIN) as MinecraftProfileTexture
////                                val string = Hashing.sha1().hashUnencodedChars(profileTexture.hash).toString()
////                                val identifier = Identifier("skins/$string")
//                                    val newTexture = mc.textureManager.getTexture(texture)
//                                    val cacheFile = ((newTexture as PlayerSkinTexture) as PlayerSkinAccessor).cacheFile
//                                    ChatUtils.chatMessage("Cache file ${if (cacheFile == null) "does not exist." else "exists."}")
//                                    if (cacheFile != null) {
//                                        ChatUtils.chatMessage("path: ${cacheFile.path}")
//                                        ChatUtils.chatMessage("absolute path: ${cacheFile.absolutePath}")
//                                        try {
//                                            var stream: InputStream? = null
//                                            if (cacheFile.exists() && cacheFile.isFile()) {
//                                                stream = Files.newInputStream(cacheFile.toPath())
//                                                ChatUtils.chatMessage("created input stream")
//                                            }else {
//                                                ChatUtils.chatMessage("cache file is not file ?!")
//                                            }
//                                            stream?.close()
//                                        }catch (_: Exception){
//                                            ChatUtils.chatMessage("Error loading resource")
//                                        }
//                                    }
//                                    return@execute
//                                }
//
//                            }
//                            ChatUtils.chatMessage("Resource present for ${texture.path}")
//                            resource.get().inputStream.close()
                        }
                    }
                }
                literal("gameprofile") {
                    string("name") {
                        execute { context ->
                            val tabEntries = TabListUtils.tabList
                            val player = tabEntries.find { it.second.contains(StringArgumentType.getString(context, "name")) }
                            if (player == null) {
                                ChatUtils.chatMessage("No player matching \"${StringArgumentType.getString(context, "name")}\" found.")
                                return@execute
                            }
                            val profile = player.first.profile
                            val properties = profile.properties
                            ChatUtils.chatMessage("Printing properties to logs.")
                            properties.forEach { name, property ->
                                Mithras.logger.info("$name; name: ${property.name}, value: ${property.value}, signature: ${property.signature}")
                            }

                        }
                    }
                }
            }
            literal("item") {
//                literal("heldnbt") {
//                    execute {
//                        val stack = mc.player?.inventory?.mainHandStack
//                        if (stack == null) {
//                            ChatUtils.chatMessage("No item in hand!")
//                            return@execute
//                        }
//                        val nbtString = NBTStringWriter.creatNbtString(stack)
//                        mc.keyboard.clipboard = nbtString
//                        ChatUtils.chatMessage("Copied held item nbt data to clipboard.")
//                    }
//                }
//                literal("value") {
//                    execute {
//                        val stack = mc.player?.inventory?.mainHandStack
//                        if (stack == null) {
//                            ChatUtils.chatMessage("No item in hand!")
//                            return@execute
//                        }
//                        val price = ItemValueCalculator.getStackValue(stack)
//                        ChatUtils.modMessage(price.createHoverableText())
//                    }
//                }
//                literal("lore") {
//                    execute {
//                        val stack = mc.player?.inventory?.mainHandStack
//                        if (stack == null) {
//                            ChatUtils.chatMessage("No item in hand!")
//                            return@execute
//                        }
//                        val lore = stack.lore
//                        mc.keyboard.clipboard = lore.joinToString(System.lineSeparator())
//                        ChatUtils.chatMessage("Copied held item lore to clipboard.")
//                    }
//                }
//                literal("formatted-lore") {
//                    execute {
//                        val stack = mc.player?.inventory?.mainHandStack
//                        if (stack == null) {
//                            ChatUtils.chatMessage("No item in hand!")
//                            return@execute
//                        }
//                        val lore = stack.formattedLore
//                        mc.keyboard.clipboard = lore.joinToString(System.lineSeparator())
//                        ChatUtils.chatMessage("Copied held item lore to clipboard.")
//                    }
//                }
//                literal("rarity") {
//                    execute {
//                        val stack = mc.player?.inventory?.mainHandStack
//                        if (stack == null) {
//                            ChatUtils.chatMessage("No item in hand!")
//                            return@execute
//                        }
//                        val rarity = stack.skyblockRarity
//                        ChatUtils.chatMessage("Held item rarity is: ${rarity.name}.")
//                    }
//                }
                literal("translationKey") {
                    execute {
                        val item = mc.player?.mainHandStack?.item
                        if (item == null) {
                            ChatUtils.chatMessage("Not holding any item!")
                            return@execute
                        }
                        ChatUtils.chatMessage(item.translationKey)
                    }
                }
            }
            literal("world") {
                literal("block") {
                    blockPos("pos"){
                        execute {
                            val pos = it.getBlockPos("pos")
                            val state = mc.world!!.getBlockState(pos)
                            ChatUtils.chatMessage(ChatUtils.literalText("${ChatUtils.RED}Block${ChatUtils.RESET} at [${ChatUtils.YELLOW}${pos.x}${ChatUtils.RESET},${ChatUtils.YELLOW}${pos.y}${ChatUtils.RESET},${ChatUtils.YELLOW}${pos.z}${ChatUtils.RESET}]: ").append(state.block.name))
                            ChatUtils.chatMessage("${ChatUtils.RED}Class${ChatUtils.RESET}: ${ChatUtils.GRAY}${state.block::class.java}")
                            ChatUtils.chatMessage("${ChatUtils.RED}State${ChatUtils.RESET}: ${ChatUtils.GRAY}$state")
                            val blockEntity = mc.world!!.getBlockEntity(pos) ?: return@execute
                            ChatUtils.chatMessage("Block has ${ChatUtils.RED}Block Entity${ChatUtils.RESET} of type: ${ChatUtils.GRAY}${BlockEntityType.getId(blockEntity.type)}${ChatUtils.RESET}, class: ${ChatUtils.GRAY}${blockEntity::class.java}")
                            val nbt = blockEntity.createNbt(mc.world!!.registryManager)
                            val nbtString = NBTStringWriter.creatNbtString(nbt)
                            mc.keyboard.clipboard = nbtString
                            ChatUtils.chatMessage("Copied Block Entity NBT to clipboard")
                        }
                    }
                }
                literal("armorstands") {
                    double("range") {
                        execute {
                            val range = it.getDouble("range")

                            val box = it.source.player.boundingBox.expand(range)

                            mc.world?.getEntitiesByClass(ArmorStandEntity::class.java, box) { entity ->
                                entity.hasCustomName()
                            }?.forEach { entity ->
                                ChatUtils.chatMessage(entity.name)
                            }
                        }
                    }
                }
                literal("entities") {
                    double("range") {
                        execute {
                            val range = it.getDouble("range")

                            val box = it.source.player.boundingBox.expand(range)

                            mc.world?.getEntitiesByClass(Entity::class.java, box) { true }?.forEach { entity ->
                                ChatUtils.chatMessage(MutableText.of(entity.name.content).append(", position: ").append(entity.pos.toString())
                                    .append(", type: ").append(entity.type.name.string).append(", class: ").append(entity::class.simpleName))
                            }
                        }
                    }
                }
            }
            literal("render") {
                literal("displayPerformance") {
                    execute {
                        Mithras.clickGUI.displayPerformance = !Mithras.clickGUI.displayPerformance
                    }
                }
                literal("screen3") {
                    execute {
                        Extensions.setScreen(Test3)
                    }
                }
                literal("screen2") {
                    execute {
                        Extensions.setScreen(Test2)
                    }
                }
                literal("screen") {
                    execute {
                        Extensions.setScreen(Test)
                    }
                }
                literal("chat") {
                    execute {
                        val text = Text.literal("12")
                        val hoverText = Text.literal("Hover §ctext\nline two")
                        val hoverEvent = ShowEntity(HoverEvent.EntityContent(mc.player!!.type, mc.player!!.uuid, mc.player!!.name))
                        text.style = text.style.withHoverEvent(hoverEvent).withInsertion("insertion")
                        ChatUtils.chatMessage(text)
                        mc.player?.mainHandStack?.let{ChatUtils.modMessage(it.toHoverableText()) }
                        mc.player?.let { it.displayName?.let { it1 -> ChatUtils.modMessage(it1) } }
                    }
                }
                literal("reloadShader") {
                    execute { mc.send{
                        try {
                            Shaders.reloadShaders()
                        }catch (e: Exception) {
                            ChatUtils.chatMessage(e.message ?: "Reloading shader failed.")
                            Mithras.logger.debug("Reloading shader failed.", e)
                        }
                    }}
                }
                literal("msaaSamples") {
                    integer("samples") {
                        execute {
                            Aurora.setMSAASamples(it.getInteger("samples"))
                        }
                    }
                }
                literal("toggleMsaa") {
                    execute {
                        Aurora.useMSAA(!Aurora.useMSAA)
                        ChatUtils.chatMessage("MSAA ${if (Aurora.useMSAA) "enabled" else "disabled"}")
                    }
                }
                literal("shapes") {
                    integer("shapes") {
                        execute {
                            Test2.shapes = it.getInteger("shapes")
                        }
                    }
                }
                execute {
                    val a = Font(Font.MONOSPACED, Font.PLAIN, 16)
                }
            }
            literal("api") {
                literal("bazaar") {
                    execute {
                        Mithras.scope.launch { BazaarAPI.loadData() }
                    }
                }
                literal("lbin") {
                    execute {
                        Mithras.scope.launch { LowestBinAPI.loadData() }
                    }
                }
            }
        }
}