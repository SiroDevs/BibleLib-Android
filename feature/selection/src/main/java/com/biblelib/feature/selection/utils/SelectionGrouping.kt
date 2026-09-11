package com.biblelib.feature.selection.utils

import com.biblelib.core.common.entity.Selectable
import com.biblelib.core.network.dtos.BibleInfoDto

const val ALL_FILTER = "All"

data class FilterOption(val name: String, val count: Int)

sealed interface GridEntry {
    data class Header(
        val key: String,
        val title: String,
        val totalCount: Int,
    ) : GridEntry

    data class CountryFilterStrip(
        val key: String,
        val continentKey: String,
        val options: List<FilterOption>,
        val selected: String,
    ) : GridEntry

    data class Item(
        val key: String,
        val bible: Selectable<BibleInfoDto>,
        val soloInGroup: Boolean = false,
    ) : GridEntry
}

private val PRIORITY_LANGUAGES = listOf("English", "French")
private const val PRIORITY_COUNTRY = "Kenya"

private val REGION_PRIORITY = listOf(
    RegionMapper.EUROPE,
    RegionMapper.AFRICA,
    RegionMapper.ASIA,
    RegionMapper.NORTH_AMERICA,
    RegionMapper.SOUTH_AMERICA,
    RegionMapper.OCEANIA,
    RegionMapper.ANTARCTICA,
    // Unspecified handled separately to always be last
)

private fun regionPriority(region: String): Int = when {
    region == RegionMapper.UNSPECIFIED -> Int.MAX_VALUE
    else -> REGION_PRIORITY.indexOf(region).let { if (it == -1) REGION_PRIORITY.size else it }
}

fun buildGridEntries(
    bibles: List<Selectable<BibleInfoDto>>,
    mode: GroupingMode,
    expandedGroups: Map<String, Boolean>,
    countryFilters: Map<String, String> = emptyMap(),
    defaultExpanded: Boolean = true,
): List<GridEntry> = when (mode) {
    GroupingMode.NONE -> {
        val solo = bibles.size <= 1
        bibles.map { GridEntry.Item(key = it.data.abbreviation, bible = it, soloInGroup = solo) }
    }

    GroupingMode.LANGUAGES -> buildLanguageEntries(bibles, expandedGroups, defaultExpanded)

    GroupingMode.COUNTRIES -> buildCountryEntries(
        groups = groupByCountry(bibles),
        keyPrefix = "country",
        expandedGroups = expandedGroups,
        defaultExpanded = defaultExpanded,
    )

    GroupingMode.REGIONS -> buildRegionEntries(
        bibles,
        expandedGroups,
        countryFilters,
        defaultExpanded
    )
}

private fun languagePriority(language: String): Int {
    val priorityIndex = PRIORITY_LANGUAGES.indexOfFirst { it.equals(language, ignoreCase = true) }
    return when {
        language.equals("Unspecified", ignoreCase = true) -> Int.MAX_VALUE
        priorityIndex >= 0 -> priorityIndex
        else -> PRIORITY_LANGUAGES.size
    }
}

private fun sortLanguages(
    groups: Map<String, List<Selectable<BibleInfoDto>>>,
): List<Map.Entry<String, List<Selectable<BibleInfoDto>>>> =
    groups.entries.sortedWith(
        compareBy(
            { languagePriority(it.key) },
            { it.key.lowercase() },
        )
    )

private fun buildLanguageEntries(
    bibles: List<Selectable<BibleInfoDto>>,
    expandedGroups: Map<String, Boolean>,
    defaultExpanded: Boolean,
): List<GridEntry> {
    val groups = bibles.groupBy { it.data.language.name.ifBlank { "Unspecified" } }
    val ordered = sortLanguages(groups)

    return ordered.flatMap { (language, items) ->
        val key = "language:$language"
        val isExpanded = expandedGroups[key] ?: defaultExpanded
        buildList {
            add(GridEntry.Header(key, language, items.size))
            if (isExpanded) {
                addAll(
                    items.map {
                        GridEntry.Item(
                            "$key:${it.data.abbreviation}",
                            it,
                            soloInGroup = items.size == 1
                        )
                    }
                )
            }
        }
    }
}

private fun groupByCountry(
    bibles: List<Selectable<BibleInfoDto>>,
): LinkedHashMap<String, MutableList<Selectable<BibleInfoDto>>> {
    val byCountry = linkedMapOf<String, MutableList<Selectable<BibleInfoDto>>>()
    bibles.forEach { selectable ->
        selectable.data.countryRefs().forEach { country ->
            byCountry.getOrPut(country.name) { mutableListOf() }.add(selectable)
        }
    }
    return byCountry
}

private fun countryPriority(country: String, items: List<Selectable<BibleInfoDto>>): Int = when {
    country.equals(UNSPECIFIED_COUNTRY_NAME, ignoreCase = true) -> Int.MAX_VALUE
    items.any { it.data.language.name.equals(PRIORITY_LANGUAGES[0], ignoreCase = true) } -> 0
    items.any { it.data.language.name.equals(PRIORITY_LANGUAGES[1], ignoreCase = true) } -> 1
    country.equals(PRIORITY_COUNTRY, ignoreCase = true) -> 2
    else -> 3
}

private fun sortCountries(
    groups: Map<String, List<Selectable<BibleInfoDto>>>,
): List<Map.Entry<String, List<Selectable<BibleInfoDto>>>> =
    groups.entries.sortedWith(
        compareBy(
            { countryPriority(it.key, it.value) },
            { it.key.lowercase() },
        )
    )

private fun buildCountryEntries(
    groups: Map<String, List<Selectable<BibleInfoDto>>>,
    keyPrefix: String,
    expandedGroups: Map<String, Boolean>,
    defaultExpanded: Boolean,
): List<GridEntry> {
    val ordered = sortCountries(groups)

    return ordered.flatMap { (country, items) ->
        val key = "$keyPrefix:$country"
        val isExpanded = expandedGroups[key] ?: defaultExpanded
        buildList {
            add(GridEntry.Header(key, country, items.size))
            if (isExpanded) {
                addAll(
                    items.map {
                        GridEntry.Item(
                            "$key:${it.data.abbreviation}",
                            it,
                            soloInGroup = items.size == 1
                        )
                    }
                )
            }
        }
    }
}

private class RegionBucket {
    val items = mutableListOf<Selectable<BibleInfoDto>>()
    private val seenAbbreviations = mutableSetOf<String>()
    val byCountry = linkedMapOf<String, MutableList<Selectable<BibleInfoDto>>>()

    fun addDistinct(bible: Selectable<BibleInfoDto>) {
        if (seenAbbreviations.add(bible.data.abbreviation)) items.add(bible)
    }
}

private fun buildRegionEntries(
    bibles: List<Selectable<BibleInfoDto>>,
    expandedGroups: Map<String, Boolean>,
    countryFilters: Map<String, String>,
    defaultExpanded: Boolean,
): List<GridEntry> {
    val byRegion = linkedMapOf<String, RegionBucket>()

    bibles.forEach { selectable ->
        selectable.data.countryRefs().forEach { country ->
            val continent = RegionMapper.continentFor(country.id)
            val bucket = byRegion.getOrPut(continent) { RegionBucket() }
            bucket.addDistinct(selectable)
            bucket.byCountry.getOrPut(country.name) { mutableListOf() }.add(selectable)
        }
    }

    val orderedRegions = byRegion.keys.sortedWith(
        compareBy({ regionPriority(it) }, { it.lowercase() })
    )

    return orderedRegions.flatMap { continent ->
        val continentKey = "continent:$continent"
        val bucket = byRegion.getValue(continent)
        val continentExpanded = expandedGroups[continentKey] ?: defaultExpanded

        buildList {
            add(GridEntry.Header(continentKey, continent, bucket.items.size))

            if (continentExpanded) {
                val orderedCountries = sortCountries(bucket.byCountry)

                val options = buildList {
                    add(FilterOption(ALL_FILTER, bucket.items.size))
                    orderedCountries.forEach { (country, items) ->
                        add(FilterOption(country, items.size))
                    }
                }

                val selected = countryFilters[continentKey]
                    ?.takeIf { it == ALL_FILTER || bucket.byCountry.containsKey(it) }
                    ?: ALL_FILTER

                add(
                    GridEntry.CountryFilterStrip(
                        "$continentKey:filter",
                        continentKey,
                        options,
                        selected
                    )
                )

                val filteredItems = if (selected == ALL_FILTER) {
                    bucket.items
                } else {
                    bucket.byCountry[selected].orEmpty()
                }
                val solo = filteredItems.size == 1

                addAll(
                    filteredItems.map {
                        GridEntry.Item(
                            "$continentKey:${it.data.abbreviation}",
                            it,
                            soloInGroup = solo
                        )
                    }
                )
            }
        }
    }
}
