package floppacoding.mithras.utils.network


/**
 * Utilities for checking the lowest bin prices of items in skyblock.
 * So far it uses a third party web service to retrieve this data.
 *
 * @author Aton
 */
object LowestBinAPI {
    private val lbinValues: MutableMap<String, Long> = mutableMapOf()
    private const val URL = "https://api.skytils.gg/api/auctions/lowestbins"

    fun getPrice(id: String): Double? = lbinValues[id]?.toDouble()

    // TODO should this be a suspend fun? should this throw an error when the request failed?
    //  introduce handling for failed request.
    suspend fun loadData() {
        val response = MithrasHttpClient.fetchJson(URL) ?: return
        try {
            lbinValues.putAll(response.keySet().associateWith { response.getAsJsonPrimitive(it).asLong })
        }catch (_: Exception) { }
    }
}