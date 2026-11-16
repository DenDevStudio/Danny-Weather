package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ForecastResponseDto(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "timezone") val timezone: String?,
    @Json(name = "current") val current: CurrentWeatherDto?,
    @Json(name = "hourly") val hourly: HourlyWeatherDto?,
    @Json(name = "daily") val daily: DailyWeatherDto?
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherDto(
    @Json(name = "time") val time: String?,
    @Json(name = "temperature_2m") val temperature2m: Double?,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: Int?,
    @Json(name = "apparent_temperature") val apparentTemperature: Double?,
    @Json(name = "is_day") val isDay: Int?,
    @Json(name = "precipitation") val precipitation: Double?,
    @Json(name = "weather_code") val weatherCode: Int?,
    @Json(name = "cloud_cover") val cloudCover: Int?,
    @Json(name = "pressure_msl") val pressureMsl: Double?,
    @Json(name = "surface_pressure") val surfacePressure: Double?,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double?,
    @Json(name = "wind_direction_10m") val windDirection10m: Int?,
    @Json(name = "wind_gusts_10m") val windGusts10m: Double?
)

@JsonClass(generateAdapter = true)
data class HourlyWeatherDto(
    @Json(name = "time") val time: List<String>?,
    @Json(name = "temperature_2m") val temperature2m: List<Double>?,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: List<Int>?,
    @Json(name = "apparent_temperature") val apparentTemperature: List<Double>?,
    @Json(name = "precipitation_probability") val precipitationProbability: List<Int>?,
    @Json(name = "precipitation") val precipitation: List<Double>?,
    @Json(name = "weather_code") val weatherCode: List<Int>?,
    @Json(name = "pressure_msl") val pressureMsl: List<Double>?,
    @Json(name = "wind_speed_10m") val windSpeed10m: List<Double>?,
    @Json(name = "uv_index") val uvIndex: List<Double>?,
    @Json(name = "is_day") val isDay: List<Int>?
)

@JsonClass(generateAdapter = true)
data class DailyWeatherDto(
    @Json(name = "time") val time: List<String>?,
    @Json(name = "weather_code") val weatherCode: List<Int>?,
    @Json(name = "temperature_2m_max") val temperature2mMax: List<Double>?,
    @Json(name = "temperature_2m_min") val temperature2mMin: List<Double>?,
    @Json(name = "apparent_temperature_max") val apparentTemperatureMax: List<Double>?,
    @Json(name = "apparent_temperature_min") val apparentTemperatureMin: List<Double>?,
    @Json(name = "sunrise") val sunrise: List<String>?,
    @Json(name = "sunset") val sunset: List<String>?,
    @Json(name = "uv_index_max") val uvIndexMax: List<Double>?,
    @Json(name = "precipitation_sum") val precipitationSum: List<Double>?,
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int>?,
    @Json(name = "wind_speed_10m_max") val windSpeed10mMax: List<Double>?
)

@JsonClass(generateAdapter = true)
data class AirQualityResponseDto(
    @Json(name = "latitude") val latitude: Double?,
    @Json(name = "longitude") val longitude: Double?,
    @Json(name = "current") val current: AirQualityCurrentDto?
)

@JsonClass(generateAdapter = true)
data class AirQualityCurrentDto(
    @Json(name = "time") val time: String?,
    @Json(name = "european_aqi") val europeanAqi: Int?,
    @Json(name = "pm10") val pm10: Double?,
    @Json(name = "pm2_5") val pm25: Double?,
    @Json(name = "carbon_monoxide") val carbonMonoxide: Double?,
    @Json(name = "nitrogen_dioxide") val nitrogenDioxide: Double?,
    @Json(name = "sulphur_dioxide") val sulphurDioxide: Double?,
    @Json(name = "ozone") val ozone: Double?
)

@JsonClass(generateAdapter = true)
data class GeocodingResponseDto(
    @Json(name = "results") val results: List<GeocodingResultDto>?
)

@JsonClass(generateAdapter = true)
data class GeocodingResultDto(
    @Json(name = "id") val id: Long?,
    @Json(name = "name") val name: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "elevation") val elevation: Double?,
    @Json(name = "population") val population: Long?,
    @Json(name = "timezone") val timezone: String?,
    @Json(name = "country") val country: String?,
    @Json(name = "country_code") val countryCode: String?,
    @Json(name = "admin1") val admin1: String?,
    @Json(name = "admin2") val admin2: String?
)

@JsonClass(generateAdapter = true)
data class WikiResponseDto(
    @Json(name = "query") val query: WikiQueryDto?
)

@JsonClass(generateAdapter = true)
data class WikiQueryDto(
    @Json(name = "pages") val pages: List<WikiPageDto>?
)

@JsonClass(generateAdapter = true)
data class WikiPageDto(
    @Json(name = "pageid") val pageId: Long?,
    @Json(name = "title") val title: String?,
    @Json(name = "extract") val extract: String?,
    @Json(name = "fullurl") val fullUrl: String?
)
