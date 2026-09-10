package li.alalin.nanassistant.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherResponse(
    @Json(name = "current_condition") val currentCondition: List<CurrentCondition>,
    @Json(name = "nearest_area") val nearestArea: List<NearestArea>?,
    @Json(name = "request") val request: List<WeatherRequest>?
)

@JsonClass(generateAdapter = true)
data class CurrentCondition(
    @Json(name = "temp_C") val tempC: String,
    @Json(name = "humidity") val humidity: String,
    @Json(name = "FeelsLikeC") val feelsLikeC: String,
    @Json(name = "windspeedKmph") val windSpeedKmph: String,
    @Json(name = "winddir16Point") val windDir16Point: String,
    @Json(name = "lang_zh") val langZh: List<LangZh>
)

@JsonClass(generateAdapter = true)
data class LangZh(
    @Json(name = "value") val value: String
)

@JsonClass(generateAdapter = true)
data class NearestArea(
    @Json(name = "areaName") val areaName: List<AreaName>,
    @Json(name = "country") val country: List<Country>
)

@JsonClass(generateAdapter = true)
data class AreaName(
    @Json(name = "value") val value: String
)

@JsonClass(generateAdapter = true)
data class Country(
    @Json(name = "value") val value: String
)

@JsonClass(generateAdapter = true)
data class WeatherRequest(
    @Json(name = "query") val query: String
)

@JsonClass(generateAdapter = true)
data class WeatherResult(
    val city: String,
    val description: String,
    val temperature: String,
    val feelsLike: String,
    val humidity: String,
    val windSpeed: String,
    val windDirection: String
) {
    fun toDisplayString(): String {
        return "$city 天气：$description，温度 ${temperature}°C (体感 ${feelsLike}°C)，湿度 ${humidity}%，风速 ${windSpeed}km/h ${windDirection}"
    }
}