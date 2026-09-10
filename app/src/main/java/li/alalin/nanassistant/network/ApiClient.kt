package li.alalin.nanassistant.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import li.alalin.nanassistant.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val VOLCANO_BASE_URL = "https://ark.cn-beijing.volces.com/api/v3/"
    private const val WEATHER_BASE_URL = "https://wttr.in/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = VolcanoApiService.createAuthInterceptor(BuildConfig.VOLCANO_API_KEY)

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val volcanoClient = okHttpClient.newBuilder()
        .addInterceptor(authInterceptor)
        .build()

    private val volcanoRetrofit = Retrofit.Builder()
        .baseUrl(VOLCANO_BASE_URL)
        .client(volcanoClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val weatherRetrofit = Retrofit.Builder()
        .baseUrl(WEATHER_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: VolcanoApiService = volcanoRetrofit.create(VolcanoApiService::class.java)
    val weatherService: WeatherApiService = weatherRetrofit.create(WeatherApiService::class.java)
}