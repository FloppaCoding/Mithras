package floppacoding.mithras.commands.impl

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import floppacoding.mithras.Mithras
import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.commands.CmdSource
import floppacoding.mithras.commands.Command
import floppacoding.mithras.utils.ChatUtils
import floppacoding.mithras.utils.Extensions.format
import floppacoding.mithras.utils.inventory.ItemPrice.formatPrice
import floppacoding.mithras.utils.inventory.Shard
import net.minecraft.text.MutableText
import net.minecraft.util.Identifier

object AttributeTree: Command() {
    override val builder: LiteralArgumentBuilder<CmdSource> = command("attributeTree") {
        literal("generateFusions") { execute {
            val startTime = System.currentTimeMillis()
            generateFusions()
            val elapsedTime = System.currentTimeMillis() - startTime
            ChatUtils.chatMessage("Fusions generated in $elapsedTime ms")
        }}
        literal("findProfits") { execute {
            val startTime = System.currentTimeMillis()
            findProfits()
            val elapsedTime = System.currentTimeMillis() - startTime
            ChatUtils.chatMessage("Profits found in $elapsedTime ms")
            var hoverText: MutableText; var fusion: Shard.Fusion
            for (indexedFusion in profitableFusions.withIndex()) {
                if(indexedFusion.index > 5) break
                fusion = indexedFusion.value
                val message = "${fusion.input1} x${fusion.input1.craftAmount} + ${fusion.input2} x${fusion.input2.craftAmount} -> ${fusion.result} x${fusion.amount}; profit: ${fusion.profit.formatPrice()}; gain: ${fusion.gain?.times(100)?.format(1) } %"
                val hover = "${fusion.input1} cost: ${fusion.input1.buyPrice.formatPrice()}, ${fusion.input2} cost: ${fusion.input2.buyPrice.formatPrice()}, total cost: ${fusion.cost.formatPrice()}, sell value: ${fusion.sellValue.formatPrice()}"
                hoverText = ChatUtils.createHoverableText(message, hover)
                ChatUtils.modMessage(hoverText)
            }
        }}

    }

    private lateinit var shards : LinkedHashMap<String, Shard>
    private val recipes: HashMap<Shard, HashMap<Shard, List<Pair<Shard, Int>>>> = HashMap(29872)
    //TODO maybe get rid of this and instead iterate through shards and their fusions respectively
    private val fusions: MutableList<Shard.Fusion> = mutableListOf()
    private val profitableFusions: MutableList<Shard.Fusion> = mutableListOf()


    private fun findProfits() {
        profitableFusions.clear()
        fusions.filterTo(profitableFusions) { (it.profit ?: -1.0) > 0 }.sortBy { fusion -> fusion.profit?.let { - it } }
    }

    private fun generateBestFusions(maxLayers: Int = 10) {
        sortShardFusions()

        for (fusion in profitableFusions) {

        }

    }

    private fun generateFusions() {
        val resource = mc.resourceManager.getResource(Identifier.of(Mithras.RESOURCE_DOMAIN, "skyblock_items/shard_fusions.json"))
        val stream = resource.get().inputStream

        val craftOptions : List<String> = Gson().fromJson(
            stream.bufferedReader(),
            object : TypeToken<List<String>>() {}.type
        )


        var input1: Shard; var input2: Shard; val results: MutableList<Pair<Shard, Int>> = mutableListOf();
        var parts: List<String>; var unique: Boolean; var fusion: Shard.Fusion

        recipes.clear()
        fusions.clear()
        for (option in craftOptions) {
            parts = option.split(";")

            input1 = getShardById(parts.getOrNull(0)) ?: continue
            input2 = getShardById(parts.getOrNull(1)) ?: continue

            unique = recipes[input1]?.get(input2) == null && recipes[input2]?.get(input1) == null
            if (!unique) {continue}

            parts = parts.getOrNull(2)?.split(",") ?: continue
            results.clear()
            for (part in parts) {
                results.add(Pair(getShardById(part.substringBefore(" x")) ?: continue,  part.substringAfter(" x").toIntOrNull() ?: 0 ))
            }
            recipes.getOrPut(input1) { HashMap(174) }[input2] = results
            if (input2 != input1) recipes.getOrPut(input2) { HashMap(174) }[input1] = results
            results.forEach {
                fusion = Shard.Fusion(input1, input2, it.first, it.second)
                it.first.fusions.add(fusion)
                fusions.add(fusion)
            }
        }
        sortShardFusions()
    }

    private fun sortShardFusions(respectReptile: Boolean = true) {
        val sortBy = if (respectReptile) Shard.Fusion::reptileCostPerShard else Shard.Fusion::costPerShard
        for (shard in shards) {
            shard.value.fusions.sortBy { fusion -> sortBy.get(fusion)?.times(-1) ?: -1.0 }
        }
    }

    private fun getShardById(id: String?): Shard? {
        return shards[id]
    }

    fun loadShards()  {
        val resource = mc.resourceManager.getResource(Identifier.of(Mithras.RESOURCE_DOMAIN, "skyblock_items/shards.json"))
        val stream = resource.get().inputStream
        val shardList = Gson().fromJson<List<Shard>>(
            stream.bufferedReader(),
            object : TypeToken<List<Shard>>() {}.type
        )

        shards = LinkedHashMap(shardList.size)
        shardList.forEach { shards[it.shardID] = it }
    }


}