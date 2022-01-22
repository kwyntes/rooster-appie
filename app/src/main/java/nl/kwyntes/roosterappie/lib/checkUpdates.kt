package nl.kwyntes.roosterappie.lib

import nl.kwyntes.roosterappie.BuildConfig
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.suspendCoroutine

suspend fun checkUpdates(): Pair<Boolean, String> {
    val client = OkHttpClient.Builder().build()

    val req = Request.Builder()
        .url("https://api.github.com/repos/kwyntes/rooster-appie/releases/latest")
        .build()

    val latestVersionString: String = suspendCoroutine { continuation ->
        client.newCall(req).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val bodyString = response.body?.string().orEmpty()
                response.body?.close()
                val json = JSONObject(bodyString)
                val versionString = if (json.has("tag_name"))
                    json.getString("tag_name").substring(1) // remove the leading 'v'
                else BuildConfig.VERSION_NAME
                continuation.resumeWith(Result.success(versionString))
            }

            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWith(Result.failure(e))
            }
        })
    }

    return (BuildConfig.VERSION_NAME != latestVersionString) to latestVersionString
}
