package com.biblelib.feature.selection.utils

object RegionMapper {
    const val ASIA = "Asia"
    const val AFRICA = "Africa"
    const val ANTARCTICA = "Antarctica"
    const val EUROPE = "Europe"
    const val NORTH_AMERICA = "North America"
    const val SOUTH_AMERICA = "South America"
    const val OCEANIA = "Oceania"
    const val UNSPECIFIED = "Unspecified"

    const val UNSPECIFIED_COUNTRY_ID = "ZZ"

    fun continentFor(countryId: String): String {
        if (countryId.equals(UNSPECIFIED_COUNTRY_ID, ignoreCase = true)) return UNSPECIFIED
        return codeToRegion[countryId.uppercase()] ?: UNSPECIFIED
    }

    private val codeToRegion: Map<String, String> = buildMap {
        putAll(EUROPE, listOf(
            "AL", "AD", "AT", "BY", "BE", "BA", "BG", "HR", "CZ", "DK", "EE", "FO", "FI", "FR",
            "DE", "GI", "GR", "GG", "VA", "HU", "IS", "IE", "IM", "IT", "JE", "XK", "LV", "LI",
            "LT", "LU", "MT", "MD", "MC", "ME", "NL", "MK", "NO", "PL", "PT", "RO", "RU", "SM",
            "RS", "SK", "SI", "ES", "SJ", "SE", "CH", "UA", "GB", "AX",
        ))
        putAll(AFRICA, listOf(
            "DZ", "AO", "BJ", "BW", "BF", "BI", "CV", "CM", "CF", "TD", "KM", "CG", "CD", "CI",
            "DJ", "EG", "GQ", "ER", "SZ", "ET", "GA", "GM", "GH", "GN", "GW", "KE", "LS", "LR",
            "LY", "MG", "MW", "ML", "MR", "MU", "YT", "MA", "MZ", "NA", "NE", "NG", "RE", "RW",
            "SH", "ST", "SN", "SC", "SL", "SO", "ZA", "SS", "SD", "TZ", "TG", "TN", "UG", "EH",
            "ZM", "ZW", "SA",
        ))
        putAll(ASIA, listOf(
            "AF", "AM", "AZ", "BH", "BD", "BT", "BN", "KH", "CN", "CY", "GE", "HK", "IN", "ID",
            "IR", "IQ", "IL", "JP", "JO", "KZ", "KP", "KR", "KW", "KG", "LA", "LB", "MO", "MY",
            "MV", "MN", "MM", "NP", "OM", "PK", "PS", "PH", "QA", "SG", "LK", "SY", "TW",
            "TJ", "TH", "TL", "TR", "TM", "AE", "UZ", "VN", "YE",
        ))
        putAll(NORTH_AMERICA, listOf(
            "AI", "AG", "AW", "BS", "BB", "BZ", "BM", "VG", "CA", "KY", "CR", "CU", "CW", "DM",
            "DO", "SV", "GL", "GD", "GP", "GT", "HT", "HN", "JM", "MQ", "MX", "MS", "NI", "PA",
            "PR", "BL", "KN", "LC", "MF", "PM", "VC", "SX", "TT", "TC", "US", "VI",
        ))
        putAll(SOUTH_AMERICA, listOf(
            "AR", "BO", "BR", "CL", "CO", "EC", "FK", "GF", "GY", "PY", "PE", "SR", "UY", "VE",
        ))
        putAll(OCEANIA, listOf(
            "AS", "AU", "CK", "FJ", "PF", "GU", "KI", "MH", "FM", "NR", "NC", "NZ", "NU", "NF",
            "MP", "PW", "PG", "PN", "WS", "SB", "TK", "TO", "TV", "VU", "WF",
        ))
        putAll(ANTARCTICA, listOf("AQ", "BV", "TF", "GS", "HM"))
    }

    private fun MutableMap<String, String>.putAll(continent: String, codes: List<String>) {
        codes.forEach { put(it, continent) }
    }
}
