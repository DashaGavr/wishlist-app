package org.example.project

import data.ClaudeApiException
import data.ClaudeApiService
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ClaudeApiServiceTest {

    private fun makeClient(responseBody: String, status: HttpStatusCode = HttpStatusCode.OK): HttpClient {
        val engine = MockEngine { _ ->
            respond(
                content = responseBody,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        return HttpClient(engine)
    }

    @Test
    fun `sendMessage extracts text from content array`() = runTest {
        val json = """{"content":[{"type":"text","text":"Here are some options!"}]}"""
        val service = ClaudeApiService(makeClient(json))
        val result = service.sendMessage("sk-test", "system", listOf("user" to "hello"))
        assertEquals("Here are some options!", result)
    }

    @Test
    fun `sendMessage returns empty string on malformed 200 response`() = runTest {
        val service = ClaudeApiService(makeClient("not json"))
        val result = service.sendMessage("sk-test", "system", listOf("user" to "hello"))
        assertEquals("", result)
    }

    @Test
    fun `sendMessage throws ClaudeApiException on error status`() = runTest {
        val service = ClaudeApiService(
            makeClient("""{"error":{"type":"authentication_error"}}""", HttpStatusCode.Unauthorized)
        )
        assertFailsWith<ClaudeApiException> {
            service.sendMessage("bad-key", "system", listOf("user" to "hello"))
        }
    }

    @Test
    fun `sendMessage sends correct headers`() = runTest {
        var capturedApiKey = ""
        var capturedVersion = ""
        val engine = MockEngine { request ->
            capturedApiKey = request.headers["x-api-key"] ?: ""
            capturedVersion = request.headers["anthropic-version"] ?: ""
            respond(
                content = """{"content":[{"type":"text","text":"ok"}]}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val service = ClaudeApiService(HttpClient(engine))
        service.sendMessage("my-api-key", "system", listOf("user" to "test"))
        assertEquals("my-api-key", capturedApiKey)
        assertEquals("2023-06-01", capturedVersion)
    }
}