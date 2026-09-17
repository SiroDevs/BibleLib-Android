package com.biblelib.core.network.dtos

import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

data class BibleInfoDto(
    val name: String = "",
    val description: String = "",
    val abbreviation: String = "",
    val tagline: String = "",
    val language: BibleLangDto = BibleLangDto(),
    val countries: List<BibleCountryDto> = emptyList(),
    val info: String = "",
    val path: String = "",
)

fun BibleInfoDto.networkPath(): String = path.ifBlank { abbreviation }

data class BibleLangDto(
    val id: String = "",
    val name: String = "",
    val script: String = "",
    val scriptDirection: String = "LTR",
)

data class BibleCountryDto(
    val id: String = "",
    val name: String = "",
)

fun BibleInfoDto.primaryCountryName(): String =
    countries.firstOrNull()?.name?.takeIf { it.isNotBlank() } ?: "Other"

typealias BooksResponse = List<BookDto>

data class BookDto(
    val id: String = "",
    val bibleId: String = "",
    val abbreviation: String = "",
    val name: String = "",
    val nameLong: String = "",
)

typealias ChaptersResponse = Map<String, List<ChapterDto>>

data class ChapterDto(
    val id: String = "",
    val bibleId: String = "",
    val bookId: String = "",
    val number: String = "",
    val reference: String = "",
)

data class ChapterContentDto(
    val id: String = "",
    val bibleId: String = "",
    val number: String = "",
    val bookId: String = "",
    val reference: String = "",
    val verseCount: Int = 0,
    val content: List<ContentItemDto> = emptyList(),
)

data class ContentItemDto(
    val name: String? = null,
    val type: String = "",
    val text: String? = null,
    @JsonAdapter(LenientAttrsAdapter::class)
    val attrs: Map<String, String>? = null,
    val items: List<ContentItemDto>? = null,
)

class LenientAttrsAdapter : TypeAdapter<Map<String, String>?>() {
    override fun write(out: JsonWriter, value: Map<String, String>?) {
        if (value == null) {
            out.nullValue()
            return
        }
        out.beginObject()
        for ((key, v) in value) {
            out.name(key)
            out.value(v)
        }
        out.endObject()
    }

    override fun read(reader: JsonReader): Map<String, String>? {
        return when (reader.peek()) {
            JsonToken.NULL -> {
                reader.nextNull()
                null
            }
            JsonToken.BEGIN_OBJECT -> {
                val map = mutableMapOf<String, String>()
                reader.beginObject()
                while (reader.hasNext()) {
                    val key = reader.nextName()
                    map[key] = when (reader.peek()) {
                        JsonToken.STRING -> reader.nextString()
                        JsonToken.NUMBER -> reader.nextString()
                        JsonToken.BOOLEAN -> reader.nextBoolean().toString()
                        JsonToken.NULL -> {
                            reader.nextNull()
                            ""
                        }
                        else -> {
                            reader.skipValue()
                            ""
                        }
                    }
                }
                reader.endObject()
                map
            }
            JsonToken.BEGIN_ARRAY -> {
                reader.skipValue()
                null
            }
            else -> {
                reader.skipValue()
                null
            }
        }
    }
}
