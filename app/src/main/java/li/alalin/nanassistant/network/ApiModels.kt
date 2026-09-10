package li.alalin.nanassistant.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatRequest(
    @Json(name = "model") val model: String,
    @Json(name = "messages") val messages: List<ChatMessage>,
    @Json(name = "temperature") val temperature: Float = 0.7f,
    @Json(name = "max_tokens") val maxTokens: Int = 2048,
    @Json(name = "stream") val stream: Boolean = false,
    @Json(name = "tools") val tools: List<Tool>? = null,
    @Json(name = "tool_choice") val toolChoice: Any? = null
)

@JsonClass(generateAdapter = true)
data class ChatMessage(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String? = null,
    @Json(name = "tool_calls") val toolCalls: List<ToolCall>? = null,
    @Json(name = "tool_call_id") val toolCallId: String? = null,
    @Json(name = "name") val name: String? = null
)

@JsonClass(generateAdapter = true)
data class Tool(
    @Json(name = "type") val type: String = "function",
    @Json(name = "function") val function: Function
)

@JsonClass(generateAdapter = true)
data class Function(
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String,
    @Json(name = "parameters") val parameters: Map<String, Any>
)

@JsonClass(generateAdapter = true)
data class ToolCall(
    @Json(name = "id") val id: String,
    @Json(name = "type") val type: String,
    @Json(name = "function") val function: FunctionCall
)

@JsonClass(generateAdapter = true)
data class FunctionCall(
    @Json(name = "name") val name: String,
    @Json(name = "arguments") val arguments: String
)

@JsonClass(generateAdapter = true)
data class ChatResponse(
    @Json(name = "id") val id: String,
    @Json(name = "object") val objectType: String,
    @Json(name = "created") val created: Long,
    @Json(name = "model") val model: String,
    @Json(name = "choices") val choices: List<Choice>,
    @Json(name = "usage") val usage: Usage?
)

@JsonClass(generateAdapter = true)
data class Choice(
    @Json(name = "index") val index: Int,
    @Json(name = "message") val message: ChatMessage,
    @Json(name = "finish_reason") val finishReason: String?
)

@JsonClass(generateAdapter = true)
data class Usage(
    @Json(name = "prompt_tokens") val promptTokens: Int,
    @Json(name = "completion_tokens") val completionTokens: Int,
    @Json(name = "total_tokens") val totalTokens: Int
)

@JsonClass(generateAdapter = true)
data class ErrorResponse(
    @Json(name = "error") val error: ApiError
)

@JsonClass(generateAdapter = true)
data class ApiError(
    @Json(name = "message") val message: String,
    @Json(name = "type") val type: String,
    @Json(name = "code") val code: String?
)