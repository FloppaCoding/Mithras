package floppacoding.mithras.commands.impl

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import floppacoding.mithras.Mithras
import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.commands.CmdSource
import floppacoding.mithras.commands.Command
import floppacoding.mithras.mixin.PlayerSkinAccessor
import floppacoding.mithras.module.impl.dungeon.dungeonmap.core.Room
import floppacoding.mithras.module.impl.dungeon.dungeonmap.dungeon.Dungeon
import floppacoding.mithras.module.impl.dungeon.dungeonmap.dungeon.RunInformation
import floppacoding.mithras.module.impl.dungeon.dungeonmap.utils.MapUtils
import floppacoding.mithras.ui.nanovg.NVGImageManager
import floppacoding.mithras.utils.ChatUtils
import floppacoding.mithras.utils.LocationManager
import floppacoding.mithras.utils.ScoreboardUtils
import floppacoding.mithras.utils.TabListUtils
import floppacoding.mithras.utils.inventory.ItemUtils.formattedLore
import floppacoding.mithras.utils.inventory.ItemUtils.lore
import floppacoding.mithras.utils.inventory.ItemUtils.skyblockRarity
import floppacoding.mithras.utils.inventory.NBTStringWriter
import net.minecraft.client.texture.PlayerSkinTexture
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files

object DebugCommand : Command {
    override val builder: LiteralArgumentBuilder<CmdSource> =
        command("mdebug") {
            literal("where") {
                execute { ChatUtils.chatMessage(
                    "in Dungeon: ${LocationManager.inDungeons}, on Hypixel: ${LocationManager.onHypixel}, " +
                            "in skyblock: ${LocationManager.inSkyblock}"
                ) }
            }
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
                        val objective = scoreboard.getObjectiveForSlot(1) ?: return@execute
                        var scores = scoreboard.getAllPlayerScores(objective)
                        scores = scores.filter {
                            it?.playerName?.startsWith("#") == false
                        }.let {
                            if (it.size > 15) it.drop(15) else it
                        }
                        scores.forEach {
                            ChatUtils.chatMessage(it.playerName)
                        }
                    }
                }
            }
            literal("tablist") {
                execute {
                    ChatUtils.chatMessage("Printing tab list to logs.")
                    val tablist = TabListUtils.tabList
                    tablist.forEach {
                        Mithras.logger.info("${it.second}; skin path: ${it.first.skinTexture.path}")
                    }
                }
            }
            literal("dungeon") {
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
                            ChatUtils.chatMessage("${it.name}; is fake: ${it.fakeEntity}; is the player: ${it.player == mc.player}; skin path: ${it.player.skinTexture.path}")
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
            }
            literal("loadskin") {
                execute {
                    val skin = mc.networkHandler?.getPlayerListEntry(mc.player?.uuid)?.skinTexture ?: return@execute
                    try {
                        val skinImage =  NVGImageManager.createImage(skin)
                        ChatUtils.chatMessage(skinImage.id.toString())
                    }catch (e: IOException) {
                        ChatUtils.chatMessage("failed creating image")
                    }

                }
            }
            literal("checkskin") {
                string("name") {
                    execute { context ->
                        val tabEntries = TabListUtils.tabList
                        val player = tabEntries.find { it.second.contains(StringArgumentType.getString(context, "name")) }
                        if (player == null) {
                            ChatUtils.chatMessage("No player matching \"${StringArgumentType.getString(context, "name")}\" found.")
                            return@execute
                        }
                        val name = (player.first.displayName?.string ?: "null") + " - " + player.second
                        var texture = player.first.skinTexture
                        if (texture == null) {
                            ChatUtils.chatMessage("Skin texture not found for ${name}!")
                            return@execute
                        }
                        var resource = mc.resourceManager.getResource(texture)
                        if (resource.isEmpty) {
                            ChatUtils.chatMessage("No resource present for ${name}, path:  ${texture.namespace}:${texture.path}. Trying to reload.")
                            texture = mc.skinProvider.loadSkin(player.first.profile)
                            resource = mc.resourceManager.getResource(texture)
                            if (resource.isEmpty) {
                                ChatUtils.chatMessage("Reloading resource failed for ${texture.namespace}:${texture.path}. Trying to create from texture.")
//                                val profileTexture = mc.sessionService.getTextures(player.first.profile, false)
//                                    .get(MinecraftProfileTexture.Type.SKIN) as MinecraftProfileTexture
//                                val string = Hashing.sha1().hashUnencodedChars(profileTexture.hash).toString()
//                                val identifier = Identifier("skins/$string")
                                val newTexture = mc.textureManager.getTexture(texture)
                                val cacheFile = ((newTexture as PlayerSkinTexture) as PlayerSkinAccessor).cacheFile
                                ChatUtils.chatMessage("Cache file ${if (cacheFile == null) "does not exist." else "exists."}")
                                if (cacheFile != null) {
                                    ChatUtils.chatMessage("path: ${cacheFile.path}")
                                    ChatUtils.chatMessage("absolute path: ${cacheFile.absolutePath}")
                                    try {
                                        var stream: InputStream? = null
                                        if (cacheFile.exists() && cacheFile.isFile()) {
                                            stream = Files.newInputStream(cacheFile.toPath())
                                            ChatUtils.chatMessage("created input stream")
                                        }else {
                                            ChatUtils.chatMessage("cache file is not file ?!")
                                        }
                                        stream?.close()
                                    }catch (_: Exception){
                                        ChatUtils.chatMessage("Error loading resource")
                                    }
                                }
                                return@execute
                            }

                        }
                        ChatUtils.chatMessage("Resource present for ${texture.path}")
                        resource.get().inputStream.close()
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
            literal("item") {
                literal("heldnbt") {
                    execute {
                        val stack = mc.player?.inventory?.mainHandStack
                        if (stack == null) {
                            ChatUtils.chatMessage("No item in hand!")
                            return@execute
                        }
                        val nbtString = NBTStringWriter.creatNbtString(stack)
                        mc.keyboard.clipboard = nbtString
                        ChatUtils.chatMessage("Copied held item nbt data to clipboard.")
                    }
                }
                literal("lore") {
                    execute {
                        val stack = mc.player?.inventory?.mainHandStack
                        if (stack == null) {
                            ChatUtils.chatMessage("No item in hand!")
                            return@execute
                        }
                        val lore = stack.lore
                        mc.keyboard.clipboard = lore.joinToString(System.lineSeparator())
                        ChatUtils.chatMessage("Copied held item lore to clipboard.")
                    }
                }
                literal("formatted-lore") {
                    execute {
                        val stack = mc.player?.inventory?.mainHandStack
                        if (stack == null) {
                            ChatUtils.chatMessage("No item in hand!")
                            return@execute
                        }
                        val lore = stack.formattedLore
                        mc.keyboard.clipboard = lore.joinToString(System.lineSeparator())
                        ChatUtils.chatMessage("Copied held item lore to clipboard.")
                    }
                }
                literal("rarity") {
                    execute {
                        val stack = mc.player?.inventory?.mainHandStack
                        if (stack == null) {
                            ChatUtils.chatMessage("No item in hand!")
                            return@execute
                        }
                        val rarity = stack.skyblockRarity
                        ChatUtils.chatMessage("Held item rarity is: ${rarity.name}.")
                    }
                }
            }
            literal("test") {
                execute { Mithras.logger.info("Test info") }
            }
        }
}