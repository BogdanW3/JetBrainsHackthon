package rs.sljivicbusiness.reggin.openai

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import rs.sljivicbusiness.reggin.openai.models.ChatMessage
import rs.sljivicbusiness.reggin.openai.models.ChatRequest
import rs.sljivicbusiness.reggin.openai.models.ChatResponse
import rs.sljivicbusiness.reggin.settings.OpenAISettings
import java.io.Closeable
import java.util.UUID

class OpenAIService : Closeable {

    private val client = HttpClient(CIO) {
        engine {
            requestTimeout = 0
        }
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private val settings: OpenAISettings
        get() = OpenAISettings.getInstance()

    private fun getApiKey(): String =
        settings.apiKey
            ?.takeIf { it.isNotBlank() }
            ?: System.getenv("OPENAI_API_KEY")
            ?: throw IllegalStateException(
                "OpenAI API Key not configured. Please set it in Settings > OpenAI API Settings"
            )

    private fun getOrganizationId(): String? =
        settings.organizationId
            ?.takeIf { it.isNotBlank() }
            ?: System.getenv("OPENAI_ORG_ID")

    suspend fun askOpenAI(prompt: String): String {
        val requestId = UUID.randomUUID().toString()
        val systemPrompt =
            "You are a helpful assistant that always responds in a friendly way."

        return try {
            val response: HttpResponse = client.post("https://api.openai.com/v1/chat/completions") {
                header(HttpHeaders.Authorization, "Bearer ${getApiKey()}")
                getOrganizationId()?.let {
                    header("OpenAI-Organization", it)
                }
                header("X-Client-Request-Id", requestId)
                contentType(ContentType.Application.Json)
                setBody(
                    ChatRequest(
                        model = "gpt-4o-mini",
                        messages = listOf(
                            ChatMessage(role = "system", content = systemPrompt),
                            ChatMessage(role = "user", content = prompt)
                        )
                    )
                )
            }

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                println(
                    "OpenAI request failed: ${response.status} " +
                            "(x-request-id=$requestId). Body: $errorBody"
                )
                "Error: ${response.status}"
            } else {
                val chatResponse: ChatResponse = response.body()
                chatResponse.choices
                    .firstOrNull()
                    ?.message
                    ?.content
                    ?: "No response"
            }
        } catch (e: Exception) {
            println("Exception during OpenAI request (x-request-id=$requestId): ${e.message}")
            "Exception: ${e.message}"
        }
    }

    override fun close() {
        client.close()
    }
}
