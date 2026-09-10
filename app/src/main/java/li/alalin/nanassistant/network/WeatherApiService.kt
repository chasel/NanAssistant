package li.alalin.nanassistant.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WeatherApiService {
    @GET("{city}?format=j1&lang=zh")
    suspend fun getWeather(@Path("city") city: String): Response<WeatherResponse>

    companion object {
        const val BASE_URL = "https://wttr.in/"
    }
}