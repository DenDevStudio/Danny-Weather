package com.example.util

import android.os.Build
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.time.Instant
import java.util.concurrent.TimeUnit

object SecurityWebhookReporter {

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .writeTimeout(8, TimeUnit.SECONDS)
            .build()
    }

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun reportFailedAdminLogin(
        attemptedLogin: String,
        webhookUrl: String = BuildConfig.ADMIN_ALERT_WEBHOOK_URL
    ): Boolean = withContext(Dispatchers.IO) {
        val targetUrl = webhookUrl.trim()
        if (targetUrl.isBlank() || (!targetUrl.startsWith("https://") && !targetUrl.startsWith("http://"))) {
            return@withContext false
        }

        return@withContext try {
            val payload = JSONObject().apply {
                put("event", "failed_admin_login")
                put("attemptedLogin", attemptedLogin.trim().take(80))
                put("appVersion", BuildConfig.VERSION_NAME)
                put("packageId", BuildConfig.APPLICATION_ID)
                put("deviceModel", "${Build.MANUFACTURER} ${Build.MODEL}")
                put("androidSdk", Build.VERSION.SDK_INT)
                put("timestampIso", Instant.now().toString())
            }

            val request = Request.Builder()
                .url(targetUrl)
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (_: Exception) {
            false
        }
    }
}
