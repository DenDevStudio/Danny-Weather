package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Landscape
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.City
import com.example.data.model.CityArticleInfo
import com.example.data.model.STRYI_CITY
import com.example.data.model.WorldRegion
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.localizedName
import com.example.ui.i18n.localizedSubtitle
import com.example.ui.i18n.localizedTitle

@Composable
fun WorldCitiesSection(
    selectedCity: City,
    inspectedCity: City,
    cityArticle: CityArticleInfo?,
    isLoadingArticle: Boolean,
    pinnedCities: List<City>,
    presetCities: List<City>,
    selectedRegion: WorldRegion,
    searchQuery: String,
    searchResults: List<City>,
    isSearching: Boolean,
    isAdminLegacyMode: Boolean = false,
    listState: LazyListState = rememberLazyListState(),
    onRegionSelected: (WorldRegion) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onInspectCity: (City) -> Unit,
    onSelectCityWeather: (City) -> Unit,
    onTogglePinCity: (City) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val lang = strings.language
    val pinnedIds = remember(pinnedCities) { pinnedCities.map { it.id }.toSet() }
    val frameless = LocalFramelessAdminMode.current

    val displayedCities = remember(searchQuery, searchResults, presetCities, selectedRegion) {
        if (searchQuery.isNotBlank()) {
            searchResults
        } else if (selectedRegion == WorldRegion.ALL) {
            presetCities
        } else {
            presetCities.filter { it.worldRegion == selectedRegion }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("world_cities_section"),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isAdminLegacyMode) {
            item(key = "admin_classic_stryi", contentType = "classic_card") {
                AdminClassicStryiCard(
                    selectedCity = selectedCity,
                    onSelectCityWeather = onSelectCityWeather,
                    onInspectCity = onInspectCity
                )
            }
        }

        // Search card
        item(key = "world_search_card", contentType = "search_card") {
            LiquidGlassCard(shape = RoundedCornerShape(24.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0x33FFD54F), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Public,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = strings.worldFindCityTitle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = strings.worldFindCitySubtitle,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    placeholder = {
                        Text(
                            text = strings.worldSearchPlaceholder,
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F)
                        )
                    },
                    trailingIcon = {
                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFFFFD54F),
                                strokeWidth = 2.dp
                            )
                        } else if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = strings.clearCd,
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("world_city_search_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (frameless) Color.Transparent else Color(0xFFFFD54F),
                        unfocusedBorderColor = if (frameless) Color.Transparent else Color(0x44FFFFFF),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFFFFD54F),
                        focusedContainerColor = Color(0x18FFFFFF),
                        unfocusedContainerColor = Color(0x12FFFFFF)
                    )
                )

                if (searchQuery.isBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WorldRegion.entries.forEach { region ->
                            val isSelected = region == selectedRegion
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) Color(0x38FFD54F) else Color(0x18FFFFFF))
                                    .glassBorder(
                                        width = 1.dp,
                                        color = if (isSelected) Color(0xFFFFD54F) else Color(0x33FFFFFF),
                                        shape = RoundedCornerShape(14.dp),
                                        frameless = frameless
                                    )
                                    .clickable { onRegionSelected(region) }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                                    .testTag("region_chip_${region.name}")
                            ) {
                                Text(
                                    text = "${region.iconEmoji} ${region.localizedTitle(lang)}",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFFFFD54F) else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
        }

        // City Encyclopedia / About Reader Card
        item(key = "city_about_reader", contentType = "reader_card") {
            CityAboutReaderCard(
                city = inspectedCity,
                article = cityArticle,
                isLoading = isLoadingArticle,
                isPinned = inspectedCity.id in pinnedIds,
                onShowWeather = { onSelectCityWeather(inspectedCity) },
                onTogglePin = { onTogglePinCity(inspectedCity) }
            )
        }

        // Results / World Cities List Header
        item(key = "world_cities_header", contentType = "header") {
            Text(
                text = if (searchQuery.isNotBlank()) {
                    strings.worldResultsHint
                } else {
                    strings.worldRegionsHint
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.75f),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        if (searchQuery.isNotBlank() && displayedCities.isEmpty() && !isSearching) {
            item(key = "empty_search_result", contentType = "empty") {
                LiquidGlassCard(shape = RoundedCornerShape(20.dp)) {
                    Text(
                        text = strings.worldEmptyHint,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(
                items = displayedCities,
                key = { it.id },
                contentType = { "city_item" }
            ) { city ->
                WorldCityCardItem(
                    city = city,
                    isWeatherActive = city.id == selectedCity.id,
                    isInspected = city.id == inspectedCity.id,
                    isPinned = city.id in pinnedIds,
                    onInspect = { onInspectCity(city) },
                    onOpenWeather = { onSelectCityWeather(city) },
                    onTogglePin = { onTogglePinCity(city) }
                )
            }
        }
    }
}

@Composable
private fun AdminClassicStryiCard(
    selectedCity: City,
    onSelectCityWeather: (City) -> Unit,
    onInspectCity: (City) -> Unit
) {
    val strings = LocalAppStrings.current
    val lang = strings.language
    val primaryStryi = remember { STRYI_CITY.copy(isPrimary = true) }
    val isSelected = selectedCity.id == primaryStryi.id

    LiquidGlassCard(shape = RoundedCornerShape(24.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSelected) Color(0x28FFD54F) else Color(0x14FFFFFF))
                .clickable {
                    onInspectCity(primaryStryi)
                    onSelectCityWeather(primaryStryi)
                }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${primaryStryi.flagEmoji} ${primaryStryi.localizedName(lang)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color(0xFFFFD54F) else Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.primaryCityBadge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD54F)
                    )
                }
                Text(
                    text = primaryStryi.localizedSubtitle(lang),
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.78f)
                )
            }
            Icon(
                imageVector = Icons.Rounded.WbSunny,
                contentDescription = strings.showWeatherHere,
                tint = if (isSelected) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CityAboutReaderCard(
    city: City,
    article: CityArticleInfo?,
    isLoading: Boolean,
    isPinned: Boolean,
    onShowWeather: () -> Unit,
    onTogglePin: () -> Unit
) {
    val frameless = LocalFramelessAdminMode.current
    val strings = LocalAppStrings.current
    val lang = strings.language
    LiquidGlassCard(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.testTag("city_about_reader_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0x28FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = city.flagEmoji, fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.AutoStories,
                                contentDescription = null,
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.aboutCityLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFFD54F)
                            )
                        }
                        Text(
                            text = city.localizedName(lang),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = city.localizedSubtitle(lang),
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.78f)
                        )
                    }
                }
            }

            // Geographic Metadata Chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CityMetaChip(
                    icon = Icons.Rounded.Explore,
                    label = "${String.format("%.2f", city.latitude)}°, ${String.format("%.2f", city.longitude)}°"
                )
                city.population?.takeIf { it > 0 }?.let { pop ->
                    CityMetaChip(
                        icon = Icons.Rounded.Groups,
                        label = "${strings.populationPrefix} ${String.format("%,d", pop).replace(',', ' ')}"
                    )
                }
                city.elevation?.let { elev ->
                    CityMetaChip(
                        icon = Icons.Rounded.Landscape,
                        label = "${strings.elevationPrefix} ${Math.round(elev)} ${strings.metersUnit}"
                    )
                }
                city.timezone?.takeIf { it.isNotBlank() }?.let { tz ->
                    CityMetaChip(
                        icon = Icons.Rounded.Schedule,
                        label = tz
                    )
                }
            }

            // Story / Wikipedia Summary
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x16FFFFFF))
                    .glassBorder(0.8.dp, Color(0x2BFFFFFF), RoundedCornerShape(16.dp), frameless)
                    .padding(14.dp)
            ) {
                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color(0xFFFFD54F),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "${strings.loadingCityInfoPrefix} ${city.localizedName(lang)}…",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val summaryText = article?.summary
                            ?: if (lang == com.example.ui.i18n.AppLanguage.UKRAINIAN && city.description.isNotBlank()) {
                                city.description
                            } else {
                                "${city.localizedName(lang)} (${city.localizedSubtitle(lang)})"
                            }
                        Text(
                            text = summaryText,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = Color.White.copy(alpha = 0.92f)
                        )
                        if (article != null) {
                            Text(
                                text = "${strings.sourcePrefix} ${article.sourceLabel}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFFFD54F).copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            // Action Buttons: Show Weather + Pin to Top
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFD54F))
                        .clickable(onClick = onShowWeather)
                        .padding(vertical = 10.dp)
                        .testTag("reader_show_weather_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.WbSunny,
                            contentDescription = null,
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.showWeatherHere,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isPinned) Color(0x3838BDF8) else Color(0x20FFFFFF))
                        .glassBorder(
                            width = 1.dp,
                            color = if (isPinned) Color(0xFF38BDF8) else Color(0x44FFFFFF),
                            shape = RoundedCornerShape(14.dp),
                            frameless = frameless
                        )
                        .clickable(onClick = onTogglePin)
                        .padding(vertical = 10.dp)
                        .testTag("reader_pin_city_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isPinned) Icons.Rounded.PushPin else Icons.Outlined.PushPin,
                            contentDescription = null,
                            tint = if (isPinned) Color(0xFF67E8F9) else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPinned) strings.pinnedLabel else strings.pinToTopLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isPinned) Color(0xFF67E8F9) else Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CityMetaChip(
    icon: ImageVector,
    label: String
) {
    val frameless = LocalFramelessAdminMode.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x1CFFFFFF))
            .glassBorder(0.8.dp, Color(0x2BFFFFFF), RoundedCornerShape(10.dp), frameless)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFFFD54F),
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
private fun WorldCityCardItem(
    city: City,
    isWeatherActive: Boolean,
    isInspected: Boolean,
    isPinned: Boolean,
    onInspect: () -> Unit,
    onOpenWeather: () -> Unit,
    onTogglePin: () -> Unit
) {
    val strings = LocalAppStrings.current
    val lang = strings.language
    LiquidGlassCard(
        modifier = Modifier.clickable(onClick = onInspect),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isInspected) Color(0x24FFD54F) else Color.Transparent)
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .testTag("world_city_item_${city.id}"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0x22FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = city.flagEmoji,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = city.localizedName(lang),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isInspected) Color(0xFFFFD54F) else Color.White
                        )
                        if (isWeatherActive) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = Color(0xFF4ADE80),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = city.localizedSubtitle(lang),
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onOpenWeather,
                    modifier = Modifier.testTag("open_weather_${city.id}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WbSunny,
                        contentDescription = strings.showWeatherHere,
                        tint = if (isWeatherActive) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.testTag("world_pin_${city.id}")
                ) {
                    Icon(
                        imageVector = if (isPinned) Icons.Rounded.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (isPinned) strings.unpinTooltip else strings.pinTooltip,
                        tint = if (isPinned) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
