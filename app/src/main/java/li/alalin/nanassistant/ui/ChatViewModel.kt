package li.alalin.nanassistant.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import li.alalin.nanassistant.BuildConfig
import li.alalin.nanassistant.data.ChatPersistence
import li.alalin.nanassistant.data.UiMessage
import li.alalin.nanassistant.network.ApiClient
import li.alalin.nanassistant.network.ChatMessage
import li.alalin.nanassistant.network.ChatRequest
import li.alalin.nanassistant.network.Function
import li.alalin.nanassistant.network.Tool
import li.alalin.nanassistant.network.ToolCall
import li.alalin.nanassistant.network.WeatherResult
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val persistence = ChatPersistence(application)
    private val _messages = androidx.compose.runtime.mutableStateOf<List<UiMessage>>(emptyList())
    val messages: List<UiMessage>
        get() = _messages.value

    private val availableTools = listOf(
        Tool(function = Function(
            name = "get_weather",
            description = "获取指定城市的天气信息",
            parameters = mapOf<String, Any>(
                "type" to "object",
                "properties" to mapOf<String, Any>(
                    "city" to mapOf<String, String>(
                        "type" to "string",
                        "description" to "城市名称，例如：北京、上海"
                    )
                ),
                "required" to listOf("city")
            )
        )),
        Tool(function = Function(
            name = "get_current_time",
            description = "获取当前时间",
            parameters = mapOf<String, Any>("type" to "object", "properties" to mapOf<String, Any>())
        ))
    )

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
                processChatTurn(userText)
            } catch (e: Exception) {
                _messages.value = _messages.value.filter { !it.isLoading }
                val errorMessage = UiMessage(text = "网络错误: ${e.message}", isUser = false)
                _messages.value = _messages.value + errorMessage
                saveMessages()
            }
        }
    }

    private suspend fun processChatTurn(userText: String, retryCount: Int = 0) {
        if (retryCount > 3) {
            _messages.value = _messages.value.filter { !it.isLoading }
            val errorMessage = UiMessage(text = "错误: 超过最大重试次数", isUser = false)
            _messages.value = _messages.value + errorMessage
            saveMessages()
            return
        }

        val apiMessages = buildApiMessages(userText)
        val request = ChatRequest(
            model = BuildConfig.VOLCANO_MODEL_ID,
            messages = apiMessages,
            tools = availableTools,
            toolChoice = "auto"
        )
        val response = ApiClient.apiService.sendChatRequest(request)

        if (!response.isSuccessful) {
            _messages.value = _messages.value.filter { !it.isLoading }
            val errorMsg = response.errorBody()?.string() ?: "请求失败"
            val errorMessage = UiMessage(text = "错误: $errorMsg", isUser = false)
            _messages.value = _messages.value + errorMessage
            saveMessages()
            return
        }

        response.body()?.let { body ->
            val choice = body.choices.firstOrNull()
            val message = choice?.message

            if (message == null) {
                _messages.value = _messages.value.filter { !it.isLoading }
                val errorMessage = UiMessage(text = "无回复内容", isUser = false)
                _messages.value = _messages.value + errorMessage
                saveMessages()
                return
            }

            val toolCalls = message.toolCalls
            if (toolCalls != null && toolCalls.isNotEmpty()) {
                _messages.value = _messages.value.filter { !it.isLoading }
                
                // Only add assistant message if it has actual content (not just tool calls)
                if (message.content != null && message.content!!.isNotBlank()) {
                    val assistantMessage = UiMessage(
                        text = message.content!!,
                        isUser = false,
                        toolCalls = toolCalls
                    )
                    _messages.value = _messages.value + assistantMessage
                    saveMessages()
                }

                val toolResults = mutableListOf<ChatMessage>()
                for (toolCall in toolCalls) {
                    val result = executeFunction(toolCall)
                    toolResults.add(ChatMessage(
                        role = "tool",
                        content = result,
                        toolCallId = toolCall.id,
                        name = toolCall.function.name
                    ))
                }

                val nextMessages = buildApiMessages(userText) + listOf(message) + toolResults
                val nextRequest = ChatRequest(
                    model = BuildConfig.VOLCANO_MODEL_ID,
                    messages = nextMessages,
                    tools = availableTools,
                    toolChoice = "auto"
                )
                val nextResponse = ApiClient.apiService.sendChatRequest(nextRequest)

                _messages.value = _messages.value.filter { !it.isLoading }

                if (nextResponse.isSuccessful) {
                    nextResponse.body()?.let { nextBody ->
                        val finalText = nextBody.choices.firstOrNull()?.message?.content ?: "无回复内容"
                        val finalMessage = UiMessage(text = finalText, isUser = false)
                        _messages.value = _messages.value + finalMessage
                        saveMessages()
                    }
                } else {
                    val errorMsg = nextResponse.errorBody()?.string() ?: "请求失败"
                    val errorMessage = UiMessage(text = "错误: $errorMsg", isUser = false)
                    _messages.value = _messages.value + errorMessage
                    saveMessages()
                }
            } else {
                _messages.value = _messages.value.filter { !it.isLoading }
                val aiText = message.content ?: "无回复内容"
                val aiMessage = UiMessage(text = aiText, isUser = false)
                _messages.value = _messages.value + aiMessage
                saveMessages()
            }
        }
    }

    private suspend fun executeFunction(toolCall: ToolCall): String {
        return try {
            when (toolCall.function.name) {
                "get_weather" -> {
                    val args = JSONObject(toolCall.function.arguments)
                    val city = args.getString("city")
                    getWeather(city)
                }
                "get_current_time" -> {
                    getCurrentTime()
                }
                else -> "未知功能: ${toolCall.function.name}"
            }
        } catch (e: Exception) {
            "执行失败: ${e.message}"
        }
    }

    private suspend fun getWeather(city: String): String = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.weatherService.getWeather(city)
            if (response.isSuccessful) {
                response.body()?.let { weather ->
                    val current = weather.currentCondition.firstOrNull()
                    current?.let {
                        val desc = it.langZh.firstOrNull()?.value ?: "未知"
                        WeatherResult(
                            city = city,
                            description = desc,
                            temperature = it.tempC,
                            feelsLike = it.feelsLikeC,
                            humidity = it.humidity,
                            windSpeed = it.windSpeedKmph,
                            windDirection = it.windDir16Point
                        ).toDisplayString()
                    } ?: "获取天气失败: 无天气数据"
                } ?: "获取天气失败: 空响应"
            } else {
                "获取天气失败: HTTP ${response.code()}"
            }
        } catch (e: Exception) {
            "网络错误: ${e.message}"
        }
    }

    private fun getCurrentTime(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return "当前时间：${LocalDateTime.now().format(formatter)}"
    }

    private fun saveMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            persistence.saveMessages(_messages.value)
        }
    }

    private fun buildApiMessages(currentUserText: String): List<ChatMessage> {
        val apiMessages = mutableListOf<ChatMessage>()
        apiMessages.add(ChatMessage(role = "system", content = "你是一个有用的AI助手，请用中文回答。当用户询问天气或时间时，请使用可用的工具获取信息。"))

        val historyMessages = _messages.value.filter { !it.isLoading }.takeLast(10)
        for (msg in historyMessages) {
            when {
                msg.isUser -> {
                    apiMessages.add(ChatMessage(role = "user", content = msg.text))
                }
                msg.isFunctionCall -> {
                    apiMessages.add(ChatMessage(
                        role = "assistant",
                        content = msg.text,
                        toolCalls = msg.toolCalls
                    ))
                }
                msg.isFunctionResult -> {
                    apiMessages.add(ChatMessage(
                        role = "tool",
                        content = msg.text,
                        toolCallId = msg.toolCallId,
                        name = msg.name
                    ))
                }
                else -> {
                    apiMessages.add(ChatMessage(role = "assistant", content = msg.text))
                }
            }
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