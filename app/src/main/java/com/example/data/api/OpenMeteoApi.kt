package com.example.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

interface OpenMeteoApi {

    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code,cloud_cover,pressure_msl,surface_pressure,wind_speed_10m,wind_direction_10m,wind_gusts_10m",
        @Query("hourly") hourly: String = "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation_probability,precipitation,weather_code,pressure_msl,wind_speed_10m,uv_index,is_day",
        @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min,apparent_temperature_max,apparent_temperature_min,sunrise,sunset,uv_index_max,precipitation_sum,precipitation_probability_max,wind_speed_10m_max",
        @Query("forecast_days") forecastDays: Int = 14,
        @Query("timezone") timezone: String = "auto"
    ): ForecastResponseDto

    @GET("https://air-quality-api.open-meteo.com/v1/air-quality")
    suspend fun getAirQuality(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "european_aqi,pm10,pm2_5,carbon_monoxide,nitrogen_dioxide,sulphur_dioxide,ozone"
    ): AirQualityResponseDto

    @GET("https://geocoding-api.open-meteo.com/v1/search")
    suspend fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = 20,
        @Query("language") language: String = "uk",
        @Query("format") format: String = "json"
    ): GeocodingResponseDto

    @GET
    suspend fun getWikiSummaryByTitle(
        @Url url: String,
        @Query("action") action: String = "query",
        @Query("format") format: String = "json",
        @Query("formatversion") formatVersion: Int = 2,
        @Query("prop") prop: String = "extracts|info",
        @Query("inprop") inProp: String = "url",
        @Query("exintro") exIntro: Int = 1,
        @Query("explaintext") explainText: Int = 1,
        @Query("exsentences") exSentences: Int = 6,
        @Query("redirects") redirects: Int = 1,
        @Query("titles") titles: String
    ): WikiResponseDto

    @GET
    suspend fun searchWikiSummary(
        @Url url: String,
        @Query("action") action: String = "query",
        @Query("format") format: String = "json",
        @Query("formatversion") formatVersion: Int = 2,
        @Query("prop") prop: String = "extracts|info",
        @Query("inprop") inProp: String = "url",
        @Query("exintro") exIntro: Int = 1,
        @Query("explaintext") explainText: Int = 1,
        @Query("exsentences") exSentences: Int = 6,
        @Query("generator") generator: String = "search",
        @Query("gsrsearch") gsrSearch: String,
        @Query("gsrlimit") gsrLimit: Int = 3
    ): WikiResponseDto

    companion object {
        private const val BASE_URL = "https://api.open-meteo.com/"

        fun create(): OpenMeteoApi {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("User-Agent", "DannyWeather/2.0 (Android Weather App)")
                        .build()
                    chain.proceed(request)
                }
                .addInterceptor(logging)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            return retrofit.create(OpenMeteoApi::class.java)
        }
    }
}
