package li.alalin.nanassistant.network

import okhttp3.Interceptor
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface VolcanoApiService {
    @POST("chat/completions")
    suspend fun sendChatRequest(
        @Body request: ChatRequest
    ): Response<ChatResponse>

    companion object {
        fun createAuthInterceptor(apiKey: String): Interceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }
    }
}