package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.City
import com.example.data.model.WorldRegion
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.localizedName
import com.example.ui.i18n.localizedSubtitle
import com.example.ui.i18n.localizedTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitySelectorSheet(
    sheetState: SheetState,
    selectedCity: City,
    pinnedCities: List<City>,
    presetCities: List<City>,
    searchQuery: String,
    searchResults: List<City>,
    isSearching: Boolean,
    onSearchQueryChanged: (String) -> Unit,
    onCitySelected: (City) -> Unit,
    onTogglePinCity: (City) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val lang = strings.language
    var selectedRegion by remember { mutableStateOf(WorldRegion.ALL) }
    val pinnedIds = remember(pinnedCities) { pinnedCities.map { it.id }.toSet() }

    val filteredPresets = remember(presetCities, selectedRegion) {
        if (selectedRegion == WorldRegion.ALL) {
            presetCities
        } else {
            presetCities.filter { it.worldRegion == selectedRegion }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F172A),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .testTag("city_selector_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.citySheetTitle,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = strings.citySheetSubtitle,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = strings.closeCd,
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = {
                    Text(
                        text = strings.firstLaunchSearchPlaceholder,
                        color = Color.White.copy(alpha = 0.5f)
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
                    .testTag("city_search_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFD54F),
                    unfocusedBorderColor = Color(0x33FFFFFF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFFFFD54F),
                    focusedContainerColor = Color(0x22FFFFFF),
                    unfocusedContainerColor = Color(0x1AFFFFFF)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search results or Presets by region
            if (searchQuery.isNotBlank()) {
                Text(
                    text = strings.searchResultsHeader,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (searchResults.isEmpty() && !isSearching) {
                    Text(
                        text = "${strings.nothingFoundPrefix} \"$searchQuery\"",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(searchResults, key = { it.id }) { city ->
                            CityRowItem(
                                city = city,
                                isSelected = city.id == selectedCity.id,
                                isPinned = city.id in pinnedIds,
                                onClick = { onCitySelected(city) },
                                onTogglePin = { onTogglePinCity(city) }
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WorldRegion.entries.forEach { region ->
                        val isRegionSelected = region == selectedRegion
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isRegionSelected) Color(0x38FFD54F) else Color(0x18FFFFFF))
                                .border(
                                    width = 1.dp,
                                    color = if (isRegionSelected) Color(0xFFFFD54F) else Color(0x28FFFFFF),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedRegion = region }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${region.iconEmoji} ${region.localizedTitle(lang)}",
                                fontSize = 12.sp,
                                fontWeight = if (isRegionSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isRegionSelected) Color(0xFFFFD54F) else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.height(310.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredPresets, key = { it.id }) { city ->
                        CityRowItem(
                            city = city,
                            isSelected = city.id == selectedCity.id,
                            isPinned = city.id in pinnedIds,
                            onClick = { onCitySelected(city) },
                            onTogglePin = { onTogglePinCity(city) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CityRowItem(
    city: City,
    isSelected: Boolean,
    isPinned: Boolean,
    onClick: () -> Unit,
    onTogglePin: () -> Unit
) {
    val strings = LocalAppStrings.current
    val lang = strings.language
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0x33FFD54F) else Color(0x1AFFFFFF))
            .border(
                1.dp,
                if (isSelected) Color(0xAAFFD54F) else Color(0x1AFFFFFF),
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("city_item_${city.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0x22FFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = city.flagEmoji,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = city.localizedName(lang),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = city.localizedSubtitle(lang),
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color(0xFFFFD54F),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            IconButton(
                onClick = onTogglePin,
                modifier = Modifier.testTag("pin_city_${city.id}")
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
