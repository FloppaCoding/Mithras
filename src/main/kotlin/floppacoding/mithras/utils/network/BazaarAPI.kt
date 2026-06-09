package floppacoding.mithras.utils.network


import floppacoding.mithras.Mithras
import kotlinx.coroutines.future.await
import net.hypixel.api.reply.skyblock.SkyBlockBazaarReply

/**
 * Utilities for interacting with the Hypixel Bazaar API.
 * Allows for checking item prices.
 *
 * @author Aton
 */
object BazaarAPI {

    var priceMode: PriceMode = PriceMode.BUY

    private var lastUpdated: Long = 0

    private val itemStats: MutableMap<String, SkyBlockBazaarReply.Product> = mutableMapOf()

    private val invalidProductPattern = Regex(":[\\d]$")

    fun getProduct(id: String): SkyBlockBazaarReply.Product? = itemStats[id]

    fun getSellPrice(id: String): Double? = itemStats[id]?.quickStatus?.sellPrice
    fun getBuyPrice(id: String): Double? = itemStats[id]?.quickStatus?.buyPrice

    fun getPrice(id: String): Double? {
        return when (priceMode) {
            PriceMode.BUY -> getBuyPrice(id)
            PriceMode.SELL -> getSellPrice(id)
        }
    }

    // TODO should this throw an error and be a suspended fun?
    //  -clear up the handling of failed request to schedule a new attempt
    @Throws(APIRequestException::class)
    suspend fun loadData() {
        val bazaarReply = Mithras.HYPIXEL_API.skyBlockBazaar.await()
        if (!bazaarReply.isSuccess) {
            throw APIRequestException("Failed loading Bazaar data.", APIRequestException(bazaarReply.cause))
        }
        itemStats.putAll(bazaarReply.products.filterKeys { !it.contains(invalidProductPattern) })
        lastUpdated = System.currentTimeMillis()
    }

    enum class PriceMode {
        SELL,
        BUY;
    }
}