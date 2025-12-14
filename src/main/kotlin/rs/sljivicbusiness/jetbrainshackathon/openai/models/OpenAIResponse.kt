package rs.sljivicbusiness.jetbrainshackathon.openai.models


import kotlinx.serialization.Serializable

@Serializable
data class ChatChoice(val message: ChatMessage)

@Serializable
data class ChatResponse(val choices: List<ChatChoice>)
