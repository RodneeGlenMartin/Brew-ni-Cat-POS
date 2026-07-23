package com.example.cattasticpos.service

import android.content.Context
import android.util.Log
import com.example.cattasticpos.CattasticPosApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Owns the device's Supabase Auth session. Every cloud read/write (sync, realtime,
 * historical pull) must use an access token from here instead of the raw anon key —
 * the database's row-level security only trusts the authenticated POS user, so an
 * anon key extracted from a public APK grants nothing.
 *
 * The refresh token is persisted in SharedPreferences, so the store signs in once
 * and the session survives restarts. The only anon-key-only endpoint left is the
 * app_release update check, which stays readable so signed-out devices can still
 * receive updates.
 */
class SupabaseAuthManager(private val context: Context) {

    companion object {
        private const val TAG = "SupabaseAuthManager"
        private const val PREFS_NAME = "cattastic_auth"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EXPIRES_AT = "expires_at"
        private const val KEY_EMAIL = "email"
        // Refresh when less than this many seconds of validity remain.
        private const val EXPIRY_MARGIN_SECONDS = 120L
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val refreshMutex = Mutex()
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun isSignedIn(): Boolean = !prefs.getString(KEY_REFRESH_TOKEN, null).isNullOrBlank()

    fun signedInEmail(): String? = prefs.getString(KEY_EMAIL, null)

    fun signOut() {
        prefs.edit().clear().apply()
    }

    /**
     * Password sign-in. Returns null on success, or a human-readable error message.
     */
    suspend fun signIn(email: String, password: String): String? = withContext(Dispatchers.IO) {
        val (url, anonKey) = supabaseConfig() ?: return@withContext "Cloud sync is not configured on this device."
        try {
            val body = JSONObject().apply {
                put("email", email.trim())
                put("password", password)
            }
            val request = Request.Builder()
                .url("$url/auth/v1/token?grant_type=password")
                .post(body.toString().toRequestBody(JSON_MEDIA_TYPE))
                .header("apikey", anonKey)
                .header("Content-Type", "application/json")
                .build()
            client.newCall(request).execute().use { response ->
                val respBody = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    Log.w(TAG, "Sign-in failed: ${response.code} $respBody")
                    return@withContext parseAuthError(respBody) ?: "Sign-in failed (${response.code})."
                }
                storeSession(JSONObject(respBody), email.trim())
                Log.i(TAG, "Signed in as ${email.trim()}.")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sign-in error", e)
            "Could not reach the server. Check the internet connection."
        }
    }

    /**
     * Returns a currently-valid access token, refreshing it first when close to
     * expiry. Returns null when signed out or the refresh fails (callers should
     * skip cloud work and try again on their next cycle).
     */
    suspend fun getValidAccessToken(): String? = withContext(Dispatchers.IO) {
        refreshMutex.withLock {
            val access = prefs.getString(KEY_ACCESS_TOKEN, null)
            val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
            val nowSeconds = System.currentTimeMillis() / 1000
            if (!access.isNullOrBlank() && nowSeconds < expiresAt - EXPIRY_MARGIN_SECONDS) {
                return@withLock access
            }
            refreshSession()
        }
    }

    private suspend fun refreshSession(): String? {
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null) ?: return null
        val (url, anonKey) = supabaseConfig() ?: return null
        return try {
            val body = JSONObject().apply { put("refresh_token", refreshToken) }
            val request = Request.Builder()
                .url("$url/auth/v1/token?grant_type=refresh_token")
                .post(body.toString().toRequestBody(JSON_MEDIA_TYPE))
                .header("apikey", anonKey)
                .header("Content-Type", "application/json")
                .build()
            client.newCall(request).execute().use { response ->
                val respBody = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    Log.w(TAG, "Token refresh failed: ${response.code} $respBody")
                    // A definitively-revoked refresh token means the session is dead;
                    // clear it so the UI prompts for sign-in again. Transient errors
                    // (network, 5xx) keep the session for the next attempt.
                    if (response.code == 400 || response.code == 401) {
                        prefs.edit().remove(KEY_ACCESS_TOKEN).remove(KEY_REFRESH_TOKEN).remove(KEY_EXPIRES_AT).apply()
                    }
                    return null
                }
                val json = JSONObject(respBody)
                storeSession(json, prefs.getString(KEY_EMAIL, null) ?: "")
                json.optString("access_token").ifBlank { null }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Token refresh error", e)
            null
        }
    }

    private fun storeSession(json: JSONObject, email: String) {
        val accessToken = json.optString("access_token")
        val refreshToken = json.optString("refresh_token")
        val expiresIn = json.optLong("expires_in", 3600L)
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_EXPIRES_AT, System.currentTimeMillis() / 1000 + expiresIn)
            .putString(KEY_EMAIL, email)
            .apply()
    }

    private fun parseAuthError(body: String): String? = try {
        val json = JSONObject(body)
        val description = json.optString("error_description").ifBlank { json.optString("msg") }
        when {
            description.contains("Invalid login credentials", ignoreCase = true) ->
                "Wrong email or password."
            description.isNotBlank() -> description
            else -> null
        }
    } catch (e: Exception) {
        null
    }

    private suspend fun supabaseConfig(): Pair<String, String>? {
        val app = context.applicationContext as CattasticPosApp
        val config = app.container.database.appConfigDao().getAppConfigOnce() ?: return null
        val url = config.supabaseUrl.trim()
        val key = config.supabaseAnonKey.trim()
        return if (url.isEmpty() || key.isEmpty()) null else url to key
    }
}
