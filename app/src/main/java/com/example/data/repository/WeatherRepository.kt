package com.example.data.repository

import com.example.data.api.AirQualityResponseDto
import com.example.data.api.ForecastResponseDto
import com.example.data.api.OpenMeteoApi
import com.example.data.local.WeatherCacheEntity
import com.example.data.local.WeatherDao
import com.example.data.model.AirQualityInfo
import com.example.data.model.City
import com.example.data.model.CityArticleInfo
import com.example.data.model.CurrentWeather
import com.example.data.model.DailyForecastItem
import com.example.data.model.FullWeatherData
import com.example.data.model.HourlyForecastItem
import com.example.data.model.WORLD_CITIES
import com.example.data.model.WeatherCodeMapper
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class WeatherRepository(
    private val api: OpenMeteoApi,
    private val dao: WeatherDao
) {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val forecastAdapter = moshi.adapter(ForecastResponseDto::class.java)
    private val aqiAdapter = moshi.adapter(AirQualityResponseDto::class.java)

    suspend fun getWeatherData(city: City): Result<FullWeatherData> = withContext(Dispatchers.IO) {
        try {
            // Fetch live data from Open-Meteo
            val forecastDto = api.getForecast(
                latitude = city.latitude,
                longitude = city.longitude
            )

            val aqiDto: AirQualityResponseDto? = try {
                api.getAirQuality(
                    latitude = city.latitude,
                    longitude = city.longitude
                )
            } catch (e: Exception) {
                null
            }

            // Cache data in Room
            try {
                val forecastJson = forecastAdapter.toJson(forecastDto)
                val aqiJson = aqiDto?.let { aqiAdapter.toJson(it) }
                dao.insertCache(
                    WeatherCacheEntity(
                        cityId = city.id,
                        cityName = city.nameUk,
                        cachedForecastJson = forecastJson,
                        cachedAqiJson = aqiJson,
                        updatedAtMillis = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                // non-fatal cache write error
            }

            val domainData = mapToDomain(city, forecastDto, aqiDto)
            Result.success(domainData)
        } catch (networkEx: Exception) {
            // Try to load cached data
            val cached = dao.getCachedWeather(city.id)
            if (cached != null) {
                try {
                    val cachedForecastDto = forecastAdapter.fromJson(cached.cachedForecastJson)
                    val cachedAqiDto = cached.cachedAqiJson?.let { aqiAdapter.fromJson(it) }
                    if (cachedForecastDto != null) {
                        val domainData = mapToDomain(city, cachedForecastDto, cachedAqiDto)
                        return@withContext Result.success(domainData)
                    }
                } catch (e: Exception) {
                    // fall through
                }
            }
            Result.failure(networkEx)
        }
    }

    suspend fun clearCache() = withContext(Dispatchers.IO) {
        dao.clearAllCache()
    }

    suspend fun searchCities(query: String, languageCode: String = "uk"): List<City> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext emptyList()

        val qLower = trimmed.lowercase(Locale.ROOT)
        val localMatches = WORLD_CITIES.filter { city ->
            val localizedCountryEn = if (city.countryCode.length == 2) {
                Locale("", city.countryCode).getDisplayCountry(Locale.ENGLISH).lowercase(Locale.ROOT)
            } else ""
            val localizedCountryDevice = if (city.countryCode.length == 2) {
                Locale("", city.countryCode).getDisplayCountry(Locale(languageCode)).lowercase(Locale.ROOT)
            } else ""
            city.nameUk.lowercase(Locale.ROOT).contains(qLower) ||
                city.nameEn.lowercase(Locale.ROOT).contains(qLower) ||
                city.countryUk.lowercase(Locale.ROOT).contains(qLower) ||
                city.regionUk.lowercase(Locale.ROOT).contains(qLower) ||
                localizedCountryEn.contains(qLower) ||
                localizedCountryDevice.contains(qLower) ||
                city.countryCode.lowercase(Locale.ROOT) == qLower
        }

        val remoteResults = mutableListOf<City>()
        try {
            val primaryLang = languageCode.ifBlank { "en" }
            val responsePrimary = api.searchCity(name = trimmed, count = 20, language = primaryLang)
            val dtosPrimary = responsePrimary.results ?: emptyList()
            val dtos = if (dtosPrimary.isEmpty() && primaryLang != "en") {
                api.searchCity(name = trimmed, count = 20, language = "en").results ?: emptyList()
            } else {
                dtosPrimary
            }

            dtos.forEach { dto ->
                val countryName = dto.country ?: ""
                val regionLabel = listOfNotNull(dto.admin1, dto.country)
                    .filter { it.isNotBlank() }
                    .distinct()
                    .joinToString(", ")
                    .ifBlank { countryName }

                remoteResults.add(
                    City(
                        id = "loc_${dto.id ?: (dto.latitude.toString() + "_" + dto.longitude.toString())}",
                        nameUk = dto.name,
                        nameEn = dto.name,
                        regionUk = regionLabel,
                        latitude = dto.latitude,
                        longitude = dto.longitude,
                        countryUk = countryName,
                        countryCode = dto.countryCode ?: "",
                        isPrimary = false,
                        population = dto.population,
                        elevation = dto.elevation,
                        timezone = dto.timezone
                    )
                )
            }
        } catch (e: Exception) {
            // Fall back smoothly to local world catalog matches if offline
        }

        // Deduplicate local + remote by approximate coordinates (~10 km grid)
        val combined = LinkedHashMap<String, City>()
        for (city in localMatches + remoteResults) {
            val key = "${Math.round(city.latitude * 10)}_${Math.round(city.longitude * 10)}"
            if (!combined.containsKey(key)) {
                combined[key] = city
            }
        }
        combined.values.toList()
    }

    suspend fun getCityInfo(city: City, languageCode: String = "uk"): CityArticleInfo = withContext(Dispatchers.IO) {
        val ukWikiUrl = "https://uk.wikipedia.org/w/api.php"
        val enWikiUrl = "https://en.wikipedia.org/w/api.php"
        val isUk = languageCode.equals("uk", ignoreCase = true)

        if (isUk) {
            // 1. Try Ukrainian Wikipedia exact title
            try {
                val exactResp = api.getWikiSummaryByTitle(url = ukWikiUrl, titles = city.nameUk)
                val exactPage = exactResp.query?.pages?.firstOrNull { page ->
                    val ext = page.extract?.trim().orEmpty()
                    ext.length > 40 && !ext.contains("може означати", ignoreCase = true) &&
                        !ext.contains("неоднозначн", ignoreCase = true)
                }
                if (exactPage != null) {
                    return@withContext CityArticleInfo(
                        city = city,
                        title = exactPage.title ?: city.nameUk,
                        summary = exactPage.extract!!.trim(),
                        sourceLabel = "Вікіпедія (Українською)",
                        articleUrl = exactPage.fullUrl
                    )
                }
            } catch (_: Exception) {
            }

            // 2. Try Ukrainian Wikipedia search with city + country context
            try {
                val searchQuery = listOf(city.nameUk, city.countryUk)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")
                val searchResp = api.searchWikiSummary(url = ukWikiUrl, gsrSearch = searchQuery)
                val searchPage = searchResp.query?.pages?.firstOrNull { page ->
                    val ext = page.extract?.trim().orEmpty()
                    ext.length > 40 && !ext.contains("може означати", ignoreCase = true)
                }
                if (searchPage != null) {
                    return@withContext CityArticleInfo(
                        city = city,
                        title = searchPage.title ?: city.nameUk,
                        summary = searchPage.extract!!.trim(),
                        sourceLabel = "Вікіпедія (Українською)",
                        articleUrl = searchPage.fullUrl
                    )
                }
            } catch (_: Exception) {
            }
        }

        // 3. Language-specific or English Wikipedia
        val preferredWikiLang = if (languageCode in listOf("pl", "de", "es", "fr")) languageCode else "en"
        val preferredWikiUrl = "https://$preferredWikiLang.wikipedia.org/w/api.php"
        try {
            val query = listOf(city.nameEn.ifBlank { city.nameUk }, Locale("", city.countryCode).getDisplayCountry(Locale(preferredWikiLang)))
                .filter { it.isNotBlank() }
                .joinToString(" ")
            val resp = api.searchWikiSummary(url = preferredWikiUrl, gsrSearch = query)
            val page = resp.query?.pages?.firstOrNull { !it.extract.isNullOrBlank() }
            if (page != null) {
                return@withContext CityArticleInfo(
                    city = city,
                    title = page.title ?: city.nameEn.ifBlank { city.nameUk },
                    summary = page.extract!!.trim(),
                    sourceLabel = "Wikipedia (${preferredWikiLang.uppercase()})",
                    articleUrl = page.fullUrl
                )
            }
        } catch (_: Exception) {
        }

        if (preferredWikiLang != "en") {
            try {
                val enCountry = if (city.countryCode.length == 2) {
                    Locale.Builder().setRegion(city.countryCode).build().getDisplayCountry(Locale.ENGLISH)
                } else {
                    ""
                }
                val enQuery = listOf(city.nameEn, enCountry)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")
                val enResp = api.searchWikiSummary(url = enWikiUrl, gsrSearch = enQuery)
                val enPage = enResp.query?.pages?.firstOrNull { !it.extract.isNullOrBlank() }
                if (enPage != null) {
                    return@withContext CityArticleInfo(
                        city = city,
                        title = enPage.title ?: city.nameEn,
                        summary = enPage.extract!!.trim(),
                        sourceLabel = "Wikipedia (English)",
                        articleUrl = enPage.fullUrl
                    )
                }
            } catch (_: Exception) {
            }
        }

        // 4. Local geographic summary fallback
        val regionDesc = if (isUk) {
            city.regionUk.ifBlank { city.countryUk }
        } else {
            Locale("", city.countryCode).getDisplayCountry(Locale(languageCode)).ifBlank { city.regionUk }
        }
        val displayCityName = if (isUk) city.nameUk else city.nameEn.ifBlank { city.nameUk }
        val fallbackSummary = if (isUk) {
            buildString {
                if (city.description.isNotBlank()) {
                    append(city.description)
                    append(" ")
                } else {
                    append("$displayCityName — населений пункт")
                    if (regionDesc.isNotBlank()) {
                        append(" у регіоні $regionDesc")
                    }
                    append(". ")
                }
                append("Координати: ${String.format("%.4f", city.latitude)}°, ${String.format("%.4f", city.longitude)}°.")
                city.elevation?.let { append(" Висота над рівнем моря: ${Math.round(it)} м.") }
                city.population?.takeIf { it > 0 }?.let { append(" Населення: ~${String.format("%,d", it).replace(',', ' ')} осіб.") }
            }
        } else {
            buildString {
                append(displayCityName)
                if (regionDesc.isNotBlank()) append(", $regionDesc")
                append(" — live weather, air quality, and 14-day forecast. ")
                append("Coordinates: ${String.format("%.4f", city.latitude)}°, ${String.format("%.4f", city.longitude)}°.")
                city.elevation?.let { append(" Elevation: ${Math.round(it)} m.") }
                city.population?.takeIf { it > 0 }?.let { append(" Population: ~${String.format("%,d", it)}.") }
            }
        }

        CityArticleInfo(
            city = city,
            title = displayCityName,
            summary = fallbackSummary.trim(),
            sourceLabel = if (isUk) "Довідник Danny Weather" else "Danny Weather Guide",
            articleUrl = null
        )
    }

    private fun mapToDomain(
        city: City,
        forecast: ForecastResponseDto,
        aqi: AirQualityResponseDto?
    ): FullWeatherData {
        val currentDto = forecast.current
        val isDay = (currentDto?.isDay ?: 1) == 1
        val weatherCode = currentDto?.weatherCode ?: 0
        val condition = WeatherCodeMapper.map(weatherCode, isDay)

        val timeStr = currentDto?.time?.let {
            formatIsoTime(it)
        } ?: ""

        val current = CurrentWeather(
            tempCelsius = currentDto?.temperature2m ?: 0.0,
            feelsLikeCelsius = currentDto?.apparentTemperature ?: (currentDto?.temperature2m ?: 0.0),
            humidityPercent = currentDto?.relativeHumidity2m ?: 50,
            pressureHpa = currentDto?.pressureMsl ?: 1013.2,
            windSpeedKmh = currentDto?.windSpeed10m ?: 0.0,
            windDirectionDeg = currentDto?.windDirection10m ?: 0,
            cloudCoverPercent = currentDto?.cloudCover ?: 0,
            precipitationMm = currentDto?.precipitation ?: 0.0,
            condition = condition,
            isDay = isDay,
            uvIndex = forecast.daily?.uvIndexMax?.firstOrNull() ?: 3.0,
            lastUpdatedTime = timeStr
        )

        // Map hourly forecast (next 24 hours)
        val hourlyList = mutableListOf<HourlyForecastItem>()
        forecast.hourly?.let { h ->
            val times = h.time ?: emptyList()
            val temps = h.temperature2m ?: emptyList()
            val codes = h.weatherCode ?: emptyList()
            val precipProbs = h.precipitationProbability ?: emptyList()
            val windSpeeds = h.windSpeed10m ?: emptyList()
            val isDays = h.isDay ?: emptyList()

            // Find current hour index or start from now
            val nowStr = currentDto?.time ?: ""
            var startIndex = 0
            if (nowStr.isNotBlank()) {
                val idx = times.indexOfFirst { it.startsWith(nowStr.take(13)) }
                if (idx >= 0) startIndex = idx
            }

            val endIndex = (startIndex + 24).coerceAtMost(times.size)
            for (i in startIndex until endIndex) {
                val itemTime = times.getOrNull(i) ?: ""
                val hourLabel = formatHourLabel(itemTime)
                val itemTemp = temps.getOrNull(i) ?: 0.0
                val itemCode = codes.getOrNull(i) ?: 0
                val itemIsDay = (isDays.getOrNull(i) ?: 1) == 1
                val itemPrecipProb = precipProbs.getOrNull(i) ?: 0
                val itemWind = windSpeeds.getOrNull(i) ?: 0.0
                val itemCond = WeatherCodeMapper.map(itemCode, itemIsDay)

                hourlyList.add(
                    HourlyForecastItem(
                        timeLabel = if (i == startIndex) "Зараз" else hourLabel,
                        fullTime = itemTime,
                        tempCelsius = itemTemp,
                        condition = itemCond,
                        precipitationProb = itemPrecipProb,
                        windSpeedKmh = itemWind,
                        isDay = itemIsDay,
                        isNow = (i == startIndex)
                    )
                )
            }
        }

        // Map daily forecast (14 days)
        val dailyList = mutableListOf<DailyForecastItem>()
        forecast.daily?.let { d ->
            val times = d.time ?: emptyList()
            val codes = d.weatherCode ?: emptyList()
            val maxTemps = d.temperature2mMax ?: emptyList()
            val minTemps = d.temperature2mMin ?: emptyList()
            val precipProbs = d.precipitationProbabilityMax ?: emptyList()
            val precipSums = d.precipitationSum ?: emptyList()
            val sunrises = d.sunrise ?: emptyList()
            val sunsets = d.sunset ?: emptyList()
            val uvMaxs = d.uvIndexMax ?: emptyList()
            val windMaxs = d.windSpeed10mMax ?: emptyList()

            for (i in times.indices.take(14)) {
                val dateStr = times.getOrNull(i) ?: ""
                val dayOfWeekUk = formatDayOfWeekUk(dateStr, i == 0)
                val dateLabel = formatDateLabel(dateStr)
                val code = codes.getOrNull(i) ?: 0
                val maxTemp = maxTemps.getOrNull(i) ?: 0.0
                val minTemp = minTemps.getOrNull(i) ?: 0.0
                val cond = WeatherCodeMapper.map(code, true)

                dailyList.add(
                    DailyForecastItem(
                        dateLabel = dateLabel,
                        dayOfWeekUk = dayOfWeekUk,
                        maxTempCelsius = maxTemp,
                        minTempCelsius = minTemp,
                        condition = cond,
                        precipitationProbMax = precipProbs.getOrNull(i) ?: 0,
                        precipitationSumMm = precipSums.getOrNull(i) ?: 0.0,
                        sunrise = formatIsoTime(sunrises.getOrNull(i) ?: ""),
                        sunset = formatIsoTime(sunsets.getOrNull(i) ?: ""),
                        uvIndexMax = uvMaxs.getOrNull(i) ?: 0.0,
                        windSpeedMaxKmh = windMaxs.getOrNull(i) ?: 0.0,
                        isoDate = dateStr,
                        isToday = (i == 0)
                    )
                )
            }
        }

        // Map air quality
        val airQualityInfo = aqi?.current?.let { c ->
            val aqiVal = c.europeanAqi ?: 25
            val (level, advice) = WeatherCodeMapper.getAqiLevelUk(aqiVal)
            AirQualityInfo(
                europeanAqi = aqiVal,
                levelUk = level,
                pm25 = c.pm25 ?: 6.2,
                pm10 = c.pm10 ?: 12.4,
                ozone = c.ozone ?: 45.0,
                nitrogenDioxide = c.nitrogenDioxide ?: 14.0,
                adviceUk = advice
            )
        }

        return FullWeatherData(
            city = city,
            current = current,
            hourly = hourlyList,
            daily = dailyList,
            airQuality = airQualityInfo
        )
    }

    private fun formatHourLabel(isoString: String): String {
        return try {
            val ldt = LocalDateTime.parse(isoString)
            ldt.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            isoString.takeLast(5)
        }
    }

    private fun formatIsoTime(isoString: String): String {
        return try {
            val ldt = LocalDateTime.parse(isoString)
            ldt.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            isoString.takeLast(5)
        }
    }

    private fun formatDateLabel(dateString: String): String {
        return try {
            val ld = LocalDate.parse(dateString)
            ld.format(DateTimeFormatter.ofPattern("d MMM", Locale("uk")))
        } catch (e: Exception) {
            dateString
        }
    }

    private fun formatDayOfWeekUk(dateString: String, isToday: Boolean): String {
        if (isToday) return "Сьогодні"
        return try {
            val ld = LocalDate.parse(dateString)
            when (ld.dayOfWeek.value) {
                1 -> "Понеділок"
                2 -> "Вівторок"
                3 -> "Середа"
                4 -> "Четвер"
                5 -> "Пʼятниця"
                6 -> "Субота"
                7 -> "Неділя"
                else -> ""
            }
        } catch (e: Exception) {
            dateString
        }
    }
}
