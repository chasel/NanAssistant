package li.alalin.nanassistant.data

import android.content.Context
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import li.alalin.nanassistant.network.ToolCall

@JsonClass(generateAdapter = true)
data class PersistedMessage(
    @Json(name = "text") val text: String,
    @Json(name = "is_user") val isUser: Boolean,
    @Json(name = "timestamp") val timestamp: Long = System.currentTimeMillis(),
    @Json(name = "tool_calls") val toolCalls: List<ToolCall>? = null,
    @Json(name = "tool_call_id") val toolCallId: String? = null,
    @Json(name = "name") val name: String? = null
) {
    fun toUiMessage() = UiMessage(text, isUser, toolCalls = toolCalls, toolCallId = toolCallId, name = name)
}

data class UiMessage(
    val text: String,
    val isUser: Boolean,
    val isLoading: Boolean = false,
    val toolCalls: List<ToolCall>? = null,
    val toolCallId: String? = null,
    val name: String? = null
) {
    val isFunctionResult: Boolean
        get() = toolCallId != null && !isUser

    val isFunctionCall: Boolean
        get() = !toolCalls.isNullOrEmpty()
}

class ChatPersistence(private val context: Context) {
    private val fileName = "chat_messages.json"
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val type = Types.newParameterizedType(List::class.java, PersistedMessage::class.java)
    private val adapter = moshi.adapter<List<PersistedMessage>>(type)

    private val file: File = context.filesDir.resolve(fileName)

    suspend fun loadMessages(): List<UiMessage> = withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext emptyList()
        
        try {
            val json = file.readText()
            if (json.isBlank()) return@withContext emptyList()
            
            val messages = adapter.fromJson(json) ?: emptyList()
            messages.map { it.toUiMessage() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveMessages(messages: List<UiMessage>) = withContext(Dispatchers.IO) {
        val persistedMessages = messages
            .filter { !it.isLoading }
            .map { PersistedMessage(it.text, it.isUser) }
        
        val json = adapter.toJson(persistedMessages)
        file.writeText(json)
    }

    suspend fun clearMessages() = withContext(Dispatchers.IO) {
        if (file.exists()) {
            file.delete()
        }
    }
}