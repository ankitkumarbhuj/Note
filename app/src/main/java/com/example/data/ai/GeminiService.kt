package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: String, // "user" or "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    companion object {
        private const val TAG = "GeminiService"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        
        // Models per guidelines
        const val MODEL_CHAT = "gemini-3.8-flash"
        const val MODEL_FAST = "gemini-3.1-flash-lite-preview"
        const val MODEL_COMPLEX = "gemini-4-argon"
    }

    private fun getApiKey(): String {
        val key = BuildConfig.GEMINI_API_KEY
        if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is placeholder or empty. Check Secrets panel.")
        }
        return key
    }

    suspend fun generateNoteEnhancement(
        prompt: String,
        noteContent: String,
        model: String = MODEL_CHAT
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey()
            val url = "$BASE_URL/$model:generateContent?key=$apiKey"

            val systemInstruction = """
                You are PrivaAI, an elite executive business consultant and confidential private advisor embedded in 0 Note.
                Provide clear, polished, structured responses. Never include meta commentary or introductory fluff unless requested.
                Focus on high-impact executive tone, strategic value, clarity, and precision.
            """.trimIndent()

            val fullPrompt = "$prompt\n\n--- NOTE CONTENT ---\n$noteContent"

            val requestJson = JSONObject().apply {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemInstruction) })
                    })
                })
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", fullPrompt) })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody(mediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val errorMsg = parseErrorMessage(bodyString)
                    Log.e(TAG, "Gemini request failed: $errorMsg (code: ${response.code})")
                    return@withContext Result.failure(Exception(errorMsg))
                }

                val responseJson = JSONObject(bodyString)
                val text = responseJson.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text", "") ?: ""

                if (text.isBlank()) {
                    Result.failure(Exception("Empty response from AI"))
                } else {
                    Result.success(text.trim())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in generateNoteEnhancement", e)
            Result.failure(e)
        }
    }

    suspend fun sendMultiTurnChat(
        history: List<ChatMessage>,
        newMessage: String,
        model: String = MODEL_CHAT
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey()
            val url = "$BASE_URL/$model:generateContent?key=$apiKey"

            val systemInstruction = """
                You are PrivaAI, the intelligent assistant for the 0 Note app.
                You help users organize, brainstorm, analyze, and craft both Business Notes (meeting memos, project specs, client strategy, financial reviews, OKRs)
                and Private Notes (personal reflections, journals, goals, private ideas).
                Maintain context across turns. Be helpful, concise, strategic, and professional.
                Support formatting in clear bullet points or numbered lists where appropriate.
            """.trimIndent()

            val contentsArray = JSONArray()

            // Append previous turns
            for (msg in history.takeLast(10)) {
                contentsArray.put(JSONObject().apply {
                    put("role", if (msg.role == "user") "user" else "model")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", msg.content) })
                    })
                })
            }

            // Append current user message
            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", newMessage) })
                })
            })

            val requestJson = JSONObject().apply {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemInstruction) })
                    })
                })
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody(mediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val errorMsg = parseErrorMessage(bodyString)
                    Log.e(TAG, "Gemini chat failed: $errorMsg (code: ${response.code})")
                    return@withContext Result.failure(Exception(errorMsg))
                }

                val responseJson = JSONObject(bodyString)
                val text = responseJson.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text", "") ?: ""

                if (text.isBlank()) {
                    Result.failure(Exception("No response received from AI"))
                } else {
                    Result.success(text.trim())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in sendMultiTurnChat", e)
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(bodyString: String): String {
        return try {
            val json = JSONObject(bodyString)
            json.optJSONObject("error")?.optString("message") ?: bodyString
        } catch (_: Exception) {
            bodyString.ifBlank { "Unknown network error" }
        }
    }
}
