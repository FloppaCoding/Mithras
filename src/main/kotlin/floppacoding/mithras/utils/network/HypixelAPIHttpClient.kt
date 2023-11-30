package floppacoding.mithras.utils.network

import net.hypixel.api.http.HypixelHttpClient
import net.hypixel.api.http.HypixelHttpResponse
import net.hypixel.api.http.RateLimit
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import java.io.IOException
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * An alternative implementation to the apache http client provided by the api.
 * This allows for updating the api key later on
 * @author Aton
 */
class HypixelAPIHttpClient(private var apiKey: UUID) : HypixelHttpClient {
    private val executorService: ExecutorService = Executors.newCachedThreadPool()
    private val httpClient: HttpClient

    init {
        httpClient = HttpClientBuilder.create().setUserAgent(HypixelHttpClient.DEFAULT_USER_AGENT).build()
    }

    constructor(apiKey: String) : this(createUUIDfromString(apiKey))

    fun updateAPIKey(apiKey: UUID) {
        this.apiKey = apiKey
    }

    fun updateAPIKey(apiKey: String) {
        updateAPIKey(createUUIDfromString(apiKey))
    }

    override fun makeRequest(url: String): CompletableFuture<HypixelHttpResponse> {
        return CompletableFuture.supplyAsync({
            try {
                val response = httpClient.execute(HttpGet(url))
                return@supplyAsync HypixelHttpResponse(
                    response.statusLine.statusCode,
                    EntityUtils.toString(response.entity, "UTF-8"),
                    null
                )
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }, executorService)
    }

    override fun makeAuthenticatedRequest(url: String): CompletableFuture<HypixelHttpResponse> {
        return CompletableFuture.supplyAsync({
            val request = HttpGet(url)
            request.addHeader("API-Key", apiKey.toString())
            try {
                val response = httpClient.execute(request)
                return@supplyAsync HypixelHttpResponse(
                    response.statusLine.statusCode,
                    EntityUtils.toString(response.entity, "UTF-8"),
                    createRateLimitResponse(response)
                )
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }, executorService)
    }

    private fun createRateLimitResponse(response: HttpResponse): RateLimit? {
        if (response.statusLine.statusCode != 200) {
            return null
        }
        val limit = response.getFirstHeader("RateLimit-Limit").value.toInt()
        val remaining = response.getFirstHeader("RateLimit-Remaining").value.toInt()
        val reset = response.getFirstHeader("RateLimit-Reset").value.toInt()
        return RateLimit(limit, remaining, reset)
    }

    override fun shutdown() {
        executorService.shutdown()
    }

    companion object {
        private fun createUUIDfromString(key: String): UUID {
            return try {
                UUID.fromString(key)
            }catch (_: IllegalArgumentException) {
                UUID.fromString("64bd424e-ccb0-42ed-8b66-6e42a135afb4")
            }
        }
    }
}