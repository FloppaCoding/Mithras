package floppacoding.mithras.config

import floppacoding.mithras.Mithras.MOD_NAME
import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.module.impl.player.InventoryTweaks.storageConfig
import floppacoding.mithras.utils.inventory.NBTStringWriter
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.StringNbtReader
import java.io.File
import java.io.IOException
import kotlin.jvm.optionals.getOrDefault

class StorageConfig(path: File) : Config {
    private val configFile = File(path, "storage.json")

    /**
     * Indicates whether this config has ever been loaded.
     * Set to true when the config is loaded.
     */
    var loaded = false
    private set
    var dirty = false
    val inventories = List(27) { SimpleInventory(45) }


    init {
        try {
            if (!path.exists()) {
                path.mkdirs()
            }
            // create file if it doesn't exist
            configFile.createNewFile()
        } catch (e: Exception) {
            println("Error initializing $MOD_NAME storage config")
        }
    }

    override fun saveConfig() {
        try {
            val registries = mc.player!!.registryManager
            val nbtList = NbtList()
            for (inventory in inventories) {
                nbtList.add(inventory.toNbtList(registries))
            }
            val jsonString = NBTStringWriter.creatNbtString(nbtList)
            configFile.bufferedWriter().use {
                it.write(jsonString)
            }
        } catch (e: IOException) {
            println("Error saving $MOD_NAME storage config.")
        }
    }

    override fun loadConfig() {
        try {
            val jsonString = configFile.bufferedReader().use { it.readText() }
            if (jsonString == "") { return }
            val registries = mc.networkHandler!!.registryManager
            val parsedList = StringNbtReader.fromOps(NbtOps.INSTANCE).read(jsonString) as NbtList
            for (ii in inventories.indices) {
                val inventoryNbt = parsedList.getList(ii).get()
                inventories[ii].readNbtList(inventoryNbt, registries)

                inventories[ii].let { inventory ->
                    inventory.clear()
                    for (jj in 0..<inventoryNbt.size) {
                        inventory.setStack(jj, ItemStack.fromNbt(registries, inventoryNbt[jj]).getOrDefault(ItemStack.EMPTY))
                    }
                    storageConfig.dirty = true
                }
            }
            loaded = true
        }catch (e:Exception){
            println("Error loading $MOD_NAME storage config.")
        }
    }
}