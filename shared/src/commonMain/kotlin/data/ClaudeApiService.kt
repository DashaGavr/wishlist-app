package data

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/** Thrown when the Anthropic API responds with a non-2xx status. */
class ClaudeApiException(message: String) : Exception(message)

class ClaudeApiService(private val client: HttpClient) {

    suspend fun sendMessage(
        apiKey: String,
        systemPrompt: String,
        history: List<Pair<String, String>>
    ): String {
        val requestBody = buildJsonObject {
            put("model", MODEL)
            put("max_tokens", 1024)
            put("system", systemPrompt)
            putJsonArray("messages") {
                history.forEach { (role, content) ->
                    addJsonObject {
                        put("role", role)
                        put("content", content)
                    }
                }
            }
        }.toString()

        val response = client.post("https://api.anthropic.com/v1/messages") {
            header("x-api-key", apiKey)
            header("anthropic-version", "2023-06-01")
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        if (!response.status.isSuccess()) {
            throw ClaudeApiException(
                "Claude API returned ${response.status.value}: ${response.bodyAsText().take(200)}"
            )
        }
        return parseResponseText(response.bodyAsText())
    }

    private fun parseResponseText(json: String): String {
        return try {
            Json.parseToJsonElement(json)
                .jsonObject["content"]
                ?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("text")
                ?.jsonPrimitive?.content
                ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    companion object {
        const val MODEL = "claude-haiku-4-5-20251001"
    }
}