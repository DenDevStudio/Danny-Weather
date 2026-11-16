package com.example.data.model

object WeatherCodeMapper {

    fun map(code: Int, isDay: Boolean = true): WeatherCondition {
        return when (code) {
            0 -> WeatherCondition(
                code = code,
                nameUk = if (isDay) "Ясно" else "Ясна ніч",
                skyType = if (isDay) SkyType.CLEAR_DAY else SkyType.CLEAR_NIGHT,
                iconKey = if (isDay) "wb_sunny" else "nightlight_round"
            )
            1 -> WeatherCondition(
                code = code,
                nameUk = if (isDay) "Переважно ясно" else "Малохмарна ніч",
                skyType = if (isDay) SkyType.CLEAR_DAY else SkyType.CLEAR_NIGHT,
                iconKey = if (isDay) "wb_sunny" else "nightlight_round"
            )
            2 -> WeatherCondition(
                code = code,
                nameUk = "Мінлива хмарність",
                skyType = if (isDay) SkyType.PARTLY_CLOUDY_DAY else SkyType.PARTLY_CLOUDY_NIGHT,
                iconKey = "cloud_queue"
            )
            3 -> WeatherCondition(
                code = code,
                nameUk = "Похмуро",
                skyType = SkyType.OVERCAST,
                iconKey = "cloud"
            )
            45 -> WeatherCondition(
                code = code,
                nameUk = "Туман",
                skyType = SkyType.FOG,
                iconKey = "foggy"
            )
            48 -> WeatherCondition(
                code = code,
                nameUk = "Паморозь та туман",
                skyType = SkyType.FOG,
                iconKey = "foggy"
            )
            51 -> WeatherCondition(
                code = code,
                nameUk = "Легка мряка",
                skyType = SkyType.RAIN,
                iconKey = "grain"
            )
            53 -> WeatherCondition(
                code = code,
                nameUk = "Помірна мряка",
                skyType = SkyType.RAIN,
                iconKey = "grain"
            )
            55 -> WeatherCondition(
                code = code,
                nameUk = "Густа мряка",
                skyType = SkyType.RAIN,
                iconKey = "grain"
            )
            56, 57 -> WeatherCondition(
                code = code,
                nameUk = "Льодяна мряка",
                skyType = SkyType.SNOW,
                iconKey = "ac_unit"
            )
            61 -> WeatherCondition(
                code = code,
                nameUk = "Невеликий дощ",
                skyType = SkyType.RAIN,
                iconKey = "umbrella"
            )
            63 -> WeatherCondition(
                code = code,
                nameUk = "Помірний дощ",
                skyType = SkyType.RAIN,
                iconKey = "water_drop"
            )
            65 -> WeatherCondition(
                code = code,
                nameUk = "Сильний дощ",
                skyType = SkyType.HEAVY_RAIN,
                iconKey = "water_drop"
            )
            66, 67 -> WeatherCondition(
                code = code,
                nameUk = "Льодяний дощ",
                skyType = SkyType.SNOW,
                iconKey = "ac_unit"
            )
            71 -> WeatherCondition(
                code = code,
                nameUk = "Невеликий снігопад",
                skyType = SkyType.SNOW,
                iconKey = "ac_unit"
            )
            73 -> WeatherCondition(
                code = code,
                nameUk = "Помірний снігопад",
                skyType = SkyType.SNOW,
                iconKey = "ac_unit"
            )
            75 -> WeatherCondition(
                code = code,
                nameUk = "Сильний снігопад",
                skyType = SkyType.SNOW,
                iconKey = "ac_unit"
            )
            77 -> WeatherCondition(
                code = code,
                nameUk = "Снігові зерна",
                skyType = SkyType.SNOW,
                iconKey = "ac_unit"
            )
            80 -> WeatherCondition(
                code = code,
                nameUk = "Короткочасний дощ",
                skyType = SkyType.RAIN,
                iconKey = "grain"
            )
            81 -> WeatherCondition(
                code = code,
                nameUk = "Помірні дощові зливи",
                skyType = SkyType.RAIN,
                iconKey = "water_drop"
            )
            82 -> WeatherCondition(
                code = code,
                nameUk = "Сильна злива",
                skyType = SkyType.HEAVY_RAIN,
                iconKey = "thunderstorm"
            )
            85, 86 -> WeatherCondition(
                code = code,
                nameUk = "Хуртовина / Сніговий душ",
                skyType = SkyType.SNOW,
                iconKey = "ac_unit"
            )
            95 -> WeatherCondition(
                code = code,
                nameUk = "Гроза",
                skyType = SkyType.THUNDERSTORM,
                iconKey = "bolt"
            )
            96, 99 -> WeatherCondition(
                code = code,
                nameUk = "Гроза з градом",
                skyType = SkyType.THUNDERSTORM,
                iconKey = "bolt"
            )
            else -> WeatherCondition(
                code = code,
                nameUk = "Мінлива погода",
                skyType = SkyType.PARTLY_CLOUDY_DAY,
                iconKey = "cloud"
            )
        }
    }

    fun getWindDirectionUk(degrees: Int): String {
        return when (degrees) {
            in 338..360, in 0..22 -> "Північний (Пн)"
            in 23..67 -> "Північно-східний (Пн-Сх)"
            in 68..112 -> "Східний (Сх)"
            in 113..157 -> "Південно-східний (Пд-Сх)"
            in 158..202 -> "Південний (Пд)"
            in 203..247 -> "Південно-західний (Пд-Зх)"
            in 248..292 -> "Західний (Зх)"
            in 293..337 -> "Північно-західний (Пн-Зх)"
            else -> "Змінний"
        }
    }

    fun getAqiLevelUk(aqi: Int): Pair<String, String> {
        return when (aqi) {
            in 0..20 -> "Відмінна" to "Повітря свіже й чисте. Ідеально для прогулянок на свіжому повітрі."
            in 21..40 -> "Добра" to "Якість повітря задовільна. Забруднення практично відсутнє."
            in 41..60 -> "Помірна" to "Прийнятна якість. Чутливим людям слід звернути увагу при тривалому перебуванні на вулиці."
            in 61..80 -> "Посередня" to "Можливе незначне подразнення дихальних шляхів у чутливих осіб."
            in 81..100 -> "Погана" to "Рекомендується обмежити фізичну активність на відкритому повітрі."
            else -> "Дуже погана" to "Небезпечний рівень забруднення. Краще залишатися в приміщенні."
        }
    }

    fun localizedConditionName(
        code: Int,
        isDay: Boolean,
        language: com.example.ui.i18n.AppLanguage
    ): String {
        if (language == com.example.ui.i18n.AppLanguage.UKRAINIAN) {
            return map(code, isDay).nameUk
        }
        return when (language) {
            com.example.ui.i18n.AppLanguage.POLISH -> when (code) {
                0 -> if (isDay) "Bezchmurnie" else "Pogodna noc"
                1 -> if (isDay) "Przeważnie bezchmurnie" else "Lekkie zachmurzenie"
                2 -> "Częściowe zachmurzenie"
                3 -> "Pochmurno"
                45, 48 -> "Mgła"
                51, 53, 55 -> "Mżawka"
                56, 57 -> "Marznąca mżawka"
                61 -> "Lekki deszcz"
                63 -> "Umiarkowany deszcz"
                65 -> "Silny deszcz"
                66, 67 -> "Marznący deszcz"
                71 -> "Lekki śnieg"
                73 -> "Śnieg"
                75, 85, 86 -> "Śnieżyca"
                77 -> "Ziarna śnieżne"
                80, 81 -> "Przelotne opady"
                82 -> "Ulewa"
                95 -> "Burza"
                96, 99 -> "Burza z gradem"
                else -> "Zmienna pogoda"
            }
            com.example.ui.i18n.AppLanguage.GERMAN -> when (code) {
                0 -> if (isDay) "Klar" else "Klare Nacht"
                1 -> if (isDay) "Überwiegend klar" else "Leicht bewölkt"
                2 -> "Teilweise bewölkt"
                3 -> "Bedeckt"
                45, 48 -> "Nebel"
                51, 53, 55 -> "Nieselregen"
                56, 57 -> "Gefrierender Nieselregen"
                61 -> "Leichter Regen"
                63 -> "Mäßiger Regen"
                65 -> "Starker Regen"
                66, 67 -> "Gefrierender Regen"
                71 -> "Leichter Schneefall"
                73 -> "Schneefall"
                75, 85, 86 -> "Starker Schneefall"
                77 -> "Schneegriesel"
                80, 81 -> "Regenschauer"
                82 -> "Starkregen"
                95 -> "Gewitter"
                96, 99 -> "Gewitter mit Hagel"
                else -> "Wechselhaft"
            }
            com.example.ui.i18n.AppLanguage.SPANISH -> when (code) {
                0 -> if (isDay) "Despejado" else "Noche despejada"
                1 -> if (isDay) "Mayormente despejado" else "Pocas nubes"
                2 -> "Parcialmente nublado"
                3 -> "Nublado"
                45, 48 -> "Niebla"
                51, 53, 55 -> "Llovizna"
                56, 57 -> "Llovizna helada"
                61 -> "Lluvia ligera"
                63 -> "Lluvia moderada"
                65 -> "Lluvia fuerte"
                66, 67 -> "Lluvia helada"
                71 -> "Nieve ligera"
                73 -> "Nieve"
                75, 85, 86 -> "Fuerte nevada"
                77 -> "Granizo menudo"
                80, 81 -> "Chubascos"
                82 -> "Lluvia intensa"
                95 -> "Tormenta"
                96, 99 -> "Tormenta con granizo"
                else -> "Clima variable"
            }
            com.example.ui.i18n.AppLanguage.FRENCH -> when (code) {
                0 -> if (isDay) "Ensoleillé" else "Nuit claire"
                1 -> if (isDay) "Peu nuageux" else "Nuit peu nuageuse"
                2 -> "Partiellement nuageux"
                3 -> "Couvert"
                45, 48 -> "Brouillard"
                51, 53, 55 -> "Bruine"
                56, 57 -> "Bruine verglaçante"
                61 -> "Pluie faible"
                63 -> "Pluie modérée"
                65 -> "Forte pluie"
                66, 67 -> "Pluie verglaçante"
                71 -> "Neige faible"
                73 -> "Chutes de neige"
                75, 85, 86 -> "Forte neige"
                77 -> "Grésil"
                80, 81 -> "Averses"
                82 -> "Forte averse"
                95 -> "Orage"
                96, 99 -> "Orage avec grêle"
                else -> "Temps variable"
            }
            else -> when (code) {
                0 -> if (isDay) "Clear sky" else "Clear night"
                1 -> if (isDay) "Mainly clear" else "Partly clear night"
                2 -> "Partly cloudy"
                3 -> "Overcast"
                45 -> "Fog"
                48 -> "Freezing fog"
                51 -> "Light drizzle"
                53 -> "Moderate drizzle"
                55 -> "Dense drizzle"
                56, 57 -> "Freezing drizzle"
                61 -> "Light rain"
                63 -> "Moderate rain"
                65 -> "Heavy rain"
                66, 67 -> "Freezing rain"
                71 -> "Light snow"
                73 -> "Moderate snow"
                75 -> "Heavy snow"
                77 -> "Snow grains"
                80 -> "Rain showers"
                81 -> "Moderate showers"
                82 -> "Violent showers"
                85, 86 -> "Snow showers"
                95 -> "Thunderstorm"
                96, 99 -> "Thunderstorm with hail"
                else -> "Partly cloudy"
            }
        }
    }

    fun getWindDirection(degrees: Int, language: com.example.ui.i18n.AppLanguage): String {
        if (language == com.example.ui.i18n.AppLanguage.UKRAINIAN) {
            return getWindDirectionUk(degrees)
        }
        return when (degrees) {
            in 338..360, in 0..22 -> "North (N)"
            in 23..67 -> "Northeast (NE)"
            in 68..112 -> "East (E)"
            in 113..157 -> "Southeast (SE)"
            in 158..202 -> "South (S)"
            in 203..247 -> "Southwest (SW)"
            in 248..292 -> "West (W)"
            in 293..337 -> "Northwest (NW)"
            else -> "Variable"
        }
    }

    fun getAqiLevel(aqi: Int, language: com.example.ui.i18n.AppLanguage): Pair<String, String> {
        if (language == com.example.ui.i18n.AppLanguage.UKRAINIAN) {
            return getAqiLevelUk(aqi)
        }
        return when (aqi) {
            in 0..20 -> "Excellent" to "Air is fresh and clean. Ideal for outdoor activities."
            in 21..40 -> "Good" to "Air quality is satisfactory with minimal pollution."
            in 41..60 -> "Moderate" to "Acceptable air quality. Sensitive individuals should take care outdoors."
            in 61..80 -> "Fair" to "Possible mild irritation for sensitive respiratory groups."
            in 81..100 -> "Poor" to "Consider limiting prolonged outdoor exertion."
            else -> "Very Poor" to "High pollution level. Best to stay indoors."
        }
    }
}
