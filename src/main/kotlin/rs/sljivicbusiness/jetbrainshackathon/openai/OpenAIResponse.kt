package rs.sljivicbusiness.jetbrainshackathon.openai


import kotlinx.serialization.Serializable
import rs.sljivicbusiness.jetbrainshackathon.openai.model.ChatMessage

@Serializable
data class ChatChoice(val message: ChatMessage)

@Serializable
data class ChatResponse(val choices: List<ChatChoice>)
