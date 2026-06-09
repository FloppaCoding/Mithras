package floppacoding.mithras.utils.inventory

import com.google.gson.annotations.Expose
import floppacoding.mithras.utils.Extensions.capitalizeOnlyFirst
import floppacoding.mithras.utils.network.BazaarAPI

/**
 * A class representing the attribute shards in the game.
 *
 * @author Aton
 */
class Shard(
    @Expose
    val shardID: String,
    @Expose
    val craftAmount: Int,
    itemID: String,
    @Expose
    val reptile: Boolean
) : SkyblockItem(itemID, ItemAttribute.ATTRIBUTE_SHARD) {

    val buyPrice: Double?
        get() = BazaarAPI.getBuyPrice(itemID)
    val sellPrice:Double?
        get() = BazaarAPI.getSellPrice(itemID)

    val fusions: MutableList<Fusion> = mutableListOf()

    /**
     * Constructor for gson deserialization.
     */
    @Suppress("unused")
    private constructor() : this("dummy", -1, "dummy", false)


    override fun hashCode(): Int {
        return shardID.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is Shard && shardID == other.shardID
    }

    override fun toString(): String {
        return itemID.substringAfter("SHARD_").split("_").joinToString(" ") { it.capitalizeOnlyFirst() }
    }

    class Fusion(val input1: Shard, val input2: Shard, val result: Shard, val amount: Int) {
        val reptile = input1.reptile || input2.reptile
        val cost: Double?
            get() = input2.buyPrice?.times(input2.craftAmount)?.let { input1.buyPrice?.times(input1.craftAmount)?.plus(it) }
        val sellValue: Double?
            get() = result.sellPrice?.times(amount)
        var profit: Double?
        val profitPerShard: Double?
            get() = profit?.div(amount)
        var gain: Double?
        val reptileProfit: Double?
            get() = if (reptile) profit?.times(reptileFactor) else profit
        val reptileProfitPerShard: Double?
            get() = if (reptile) profitPerShard?.times(reptileFactor) else profitPerShard
        val reptileGain: Double?
            get() = if (reptile) gain?.times(reptileFactor) else gain
        val costPerShard: Double?
            get() = cost?.div(amount)
        val reptileCostPerShard: Double?
            get() = if (reptile) costPerShard?.times(reptileFactor) else costPerShard


        init {
//            profit = cost?.let { sellValue?.minus(it) }
            profit = cost?.let { result.buyPrice?.times(amount)?.minus(it) }
            gain = cost?.let { profit?.div(it) }
        }
    }

    companion object {
        var reptileFactor = 1.2
    }
}