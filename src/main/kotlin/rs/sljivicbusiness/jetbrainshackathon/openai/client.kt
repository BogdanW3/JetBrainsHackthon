package rs.sljivicbusiness.jetbrainshackathon.openai

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
//import rs.sljivicbusiness.jetbrainshackathon.openai.ChatResponse
import rs.sljivicbusiness.jetbrainshackathon.openai.model.*
import java.util.*
import java.io.Closeable


class OpenAIService : Closeable {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private val apiKey = System.getenv("OPENAI_API_KEY")
    private val organizationId = System.getenv("OPENAI_ORG_ID") // optional

    suspend fun askOpenAI(prompt: String): String {
        val initialPrompt = "You are a helpful assistant that always responds in a friendly way."
        val requestId = UUID.randomUUID().toString()

        try {
            val response: HttpResponse = client.post("https://api.openai.com/v1/chat/completions") {
                header("Authorization", "Bearer $apiKey")
                organizationId?.let { header("OpenAI-Organization", it) }
                header("X-Client-Request-Id", requestId)
                contentType(ContentType.Application.Json)
                setBody(
                    ChatRequest(
                        model = "gpt-4o-mini",
                        messages = listOf(
                            ChatMessage(role = "system", content = initialPrompt),
                            ChatMessage(role = "user", content = prompt)
                        )
                    )
                )
            }

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                println("OpenAI request failed: ${response.status}. x-request-id=$requestId. Body: $errorBody")
                return "Error: ${response.status}"
            }

            val chatResponse: ChatResponse = response.body()
            return chatResponse.choices.firstOrNull()?.message?.content ?: "No response"

        } catch (e: Exception) {
            println("Exception during OpenAI request (x-request-id=$requestId): ${e.message}")
            return "Exception: ${e.message}"
        }
    }

    override fun close() {
        client.close()
    }
}
