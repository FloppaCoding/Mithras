package floppacoding.mithras.utils.network

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import floppacoding.mithras.Mithras
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils

/**
 * A http client for fetching information from the web.
 *
 * @author Aton
 */
object MithrasHttpClient {
    val httpClient: CloseableHttpClient = HttpClientBuilder.create().setUserAgent("Mithras/${Mithras.MOD_VERSION}").build()

    // TODO should this be suspeneded? Should this throw an error when the request failed?
    //  finalize and document error handling.
    @Suppress("RedundantSuspendModifier")
    suspend fun fetchJson(url: String): JsonObject? {
        return try {
            val response = httpClient.execute(HttpGet(url))
            val json = EntityUtils.toString(response.entity, Charsets.UTF_8)
            JsonParser.parseString(json).asJsonObject
        }catch (_: Exception) {
            null
        }
    }
}