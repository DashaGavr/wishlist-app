package org.example.project

import data.AiChatRepository
import data.ClaudeApiService
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import presentation.ai.Role
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AiChatRepositoryTest {

    /** Builds a repo whose mock API returns [claudeResponse] as the assistant text (properly JSON-escaped). */
    private fun makeRepo(claudeResponse: String): AiChatRepository {
        val engine = MockEngine { _ ->
            val body = buildJsonObject {
                putJsonArray("content") {
                    addJsonObject {
                        put("type", "text")
                        put("text", claudeResponse)
                    }
                }
            }.toString()
            respond(
                content = body,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        return AiChatRepository(ClaudeApiService(HttpClient(engine)))
    }

    @Test
    fun `sendMessage appends user and assistant messages`() = runTest {
        val repo = makeRepo("Found some stuff!")
        repo.sendMessage("I want a bag", "sk-key")
        val messages = repo.messages.value
        assertEquals(2, messages.size)
        assertEquals(Role.USER, messages[0].role)
        assertEquals("I want a bag", messages[0].text)
        assertEquals(Role.ASSISTANT, messages[1].role)
        assertEquals("Found some stuff!", messages[1].text)
    }

    @Test
    fun `sendMessage with empty API key adds error message without calling API`() = runTest {
        val repo = AiChatRepository(ClaudeApiService(HttpClient(MockEngine { error("should not be called") })))
        repo.sendMessage("test", "")
        val messages = repo.messages.value
        assertEquals(2, messages.size)
        assertEquals(Role.ASSISTANT, messages[1].role)
        assertTrue(messages[1].text.contains("API key", ignoreCase = true))
    }

    @Test
    fun `api failure adds error message instead of crashing`() = runTest {
        val engine = MockEngine { _ ->
            respond(content = "server error", status = HttpStatusCode.InternalServerError)
        }
        val repo = AiChatRepository(ClaudeApiService(HttpClient(engine)))
        repo.sendMessage("bag", "sk-key")
        val messages = repo.messages.value
        assertEquals(2, messages.size)
        assertTrue(messages[1].text.contains("Couldn't reach", ignoreCase = true))
    }

    @Test
    fun `blank response adds error message`() = runTest {
        val repo = makeRepo("")
        repo.sendMessage("bag", "sk-key")
        val assistant = repo.messages.value.last()
        assertTrue(assistant.text.contains("empty response", ignoreCase = true))
    }

    @Test
    fun `parseAssistantMessage splits text and JSON block`() = runTest {
        val response = "Found 1 option!\n```json\n" +
            """[{"title":"Test Bag","description":"Nice","link":"https://example.com","imageUrl":""}]""" +
            "\n```"
        val repo = makeRepo(response)
        repo.sendMessage("bag", "sk-key")
        val assistant = repo.messages.value.last()
        assertTrue(assistant.text.startsWith("Found 1 option!"))
        assertEquals(1, assistant.wishes.size)
        assertEquals("Test Bag", assistant.wishes[0].title)
        assertEquals("https://example.com", assistant.wishes[0].link)
    }

    @Test
    fun `parseAssistantMessage with empty JSON array returns no wishes`() = runTest {
        val repo = makeRepo("Nothing found.\n```json\n[]\n```")
        repo.sendMessage("weird request", "sk-key")
        val assistant = repo.messages.value.last()
        assertEquals("Nothing found.", assistant.text)
        assertEquals(0, assistant.wishes.size)
    }

    @Test
    fun `clearHistory empties the message list`() = runTest {
        val repo = makeRepo("ok")
        repo.sendMessage("hello", "sk-key")
        assertEquals(2, repo.messages.value.size)
        repo.clearHistory()
        assertEquals(0, repo.messages.value.size)
    }
}