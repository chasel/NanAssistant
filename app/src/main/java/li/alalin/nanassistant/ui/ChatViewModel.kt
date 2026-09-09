package li.alalin.nanassistant.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import li.alalin.nanassistant.BuildConfig
import li.alalin.nanassistant.data.ChatPersistence
import li.alalin.nanassistant.data.UiMessage
import li.alalin.nanassistant.network.ApiClient
import li.alalin.nanassistant.network.ChatMessage
import li.alalin.nanassistant.network.ChatRequest

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val persistence = ChatPersistence(application)
    private val _messages = androidx.compose.runtime.mutableStateOf<List<UiMessage>>(emptyList())
    val messages: List<UiMessage>
        get() = _messages.value

    init {
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            val savedMessages = persistence.loadMessages()
            _messages.value = savedMessages
        }
    }

    fun sendMessage(userText: String) {
        val userMessage = UiMessage(text = userText, isUser = true)
        _messages.value = _messages.value + userMessage
        saveMessages()

        val loadingMessage = UiMessage(text = "", isUser = false, isLoading = true)
        _messages.value = _messages.value + loadingMessage

        viewModelScope.launch {
            try {
                val apiMessages = buildApiMessages(userText)
                val request = ChatRequest(
                    model = BuildConfig.VOLCANO_MODEL_ID,
                    messages = apiMessages
                )
                val response = ApiClient.apiService.sendChatRequest(request)

                _messages.value = _messages.value.filter { !it.isLoading }

                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        val aiText = body.choices.firstOrNull()?.message?.content ?: "无回复内容"
                        val aiMessage = UiMessage(text = aiText, isUser = false)
                        _messages.value = _messages.value + aiMessage
                        saveMessages()
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "请求失败"
                    val errorMessage = UiMessage(text = "错误: $errorMsg", isUser = false)
                    _messages.value = _messages.value + errorMessage
                    saveMessages()
                }
            } catch (e: Exception) {
                _messages.value = _messages.value.filter { !it.isLoading }
                val errorMessage = UiMessage(text = "网络错误: ${e.message}", isUser = false)
                _messages.value = _messages.value + errorMessage
                saveMessages()
            }
        }
    }

    private fun saveMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            persistence.saveMessages(_messages.value)
        }
    }

    private fun buildApiMessages(currentUserText: String): List<ChatMessage> {
        val apiMessages = mutableListOf<ChatMessage>()
        apiMessages.add(ChatMessage(role = "system", content = "你是一个有用的AI助手，请用中文回答。"))

        val historyMessages = _messages.value.filter { !it.isLoading }.takeLast(10)
        for (msg in historyMessages) {
            val role = if (msg.isUser) "user" else "assistant"
            apiMessages.add(ChatMessage(role = role, content = msg.text))
        }

        return apiMessages
    }

    fun clearMessages() {
        _messages.value = emptyList()
        viewModelScope.launch(Dispatchers.IO) {
            persistence.clearMessages()
        }
    }
}

@Composable
fun rememberChatViewModel(): ChatViewModel = viewModel()