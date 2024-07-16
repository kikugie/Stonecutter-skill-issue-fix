import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class HttpUtils(timeout: Duration = 30.seconds, val consumer: (Response) -> Throwable) {
    private val duration = timeout.toJavaDuration()
    val client = OkHttpClient.Builder()
        .connectTimeout(duration)
        .readTimeout(duration)
        .writeTimeout(duration)
        .addNetworkInterceptor { chain ->
            chain.proceed(
                chain.request()
                    .newBuilder()
                    .header(
                        "User-Agent",
                        "kikugie/stonecutter/${HttpUtils::class.java.`package`.implementationVersion}"
                    )
                    .build(),
            )
        }
        .build()
    val json = Json {
        ignoreUnknownKeys = true
    }

    inline fun <reified T> get(url: String, headers: Map<String, String> = emptyMap()): T =
        request(Request.Builder().url(url), headers)

    inline fun <reified T> request(requestBuilder: Request.Builder, headers: Map<String, String>): T {
        for ((name, value) in headers) {
            requestBuilder.header(name, value)
        }

        val request = requestBuilder.build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw consumer(response)
            val body = response.body!!.string().takeUnless(String::isBlank) ?: "\"\""
            return json.decodeFromString<T>(body)
        }
    }
}