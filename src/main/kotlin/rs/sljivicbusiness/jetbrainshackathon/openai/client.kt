package rs.sljivicbusiness.jetbrainshackathon.openai

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import io.ktor.serialization.kotlinx.json.*

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(val role: String, val content: String)

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>
)

@Serializable
data class ChatChoice(val message: ChatMessage)

@Serializable
data class ChatResponse(val choices: List<ChatChoice>)


suspend fun askOpenAI(prompt: String): String {

    val apiKey = System.getenv("OPENAI_API_KEY")

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    val initialPrompt = "You are a helpful assistant that always responds in a friendly way."

    val response: ChatResponse = client.post("https://api.openai.com/v1/chat/completions") {
        header("Authorization", "Bearer $apiKey")
        contentType(ContentType.Application.Json)
        setBody(
            ChatRequest(
                model = "gpt-3.5-turbo",
                messages = listOf(
                    ChatMessage(role = "system", content = initialPrompt),
                    ChatMessage(role = "user", content = prompt))
            )
        )
    }.body()

    client.close()

    return response.choices.firstOrNull()?.message?.content ?: "No response"
}
