package com.biblelib.core.network.services

import androidx.annotation.Keep
import com.biblelib.core.network.dtos.BibleInfoDto
import com.biblelib.core.network.dtos.BooksResponse
import com.biblelib.core.network.dtos.ChapterContentDto
import com.biblelib.core.network.dtos.ChaptersResponse
import retrofit2.http.GET
import retrofit2.http.Path

@Keep
interface BibleLibService {
    @GET("info.json")
    suspend fun getGroups(): List<String>

    @GET("{group}/info.json")
    suspend fun getGroupInfo(@Path(value = "group", encoded = true) group: String): List<BibleInfoDto>

    @GET("{path}/books.json")
    suspend fun getBooks(@Path(value = "path", encoded = true) path: String): BooksResponse

    @GET("{path}/chapters.json")
    suspend fun getChapters(@Path(value = "path", encoded = true) path: String): ChaptersResponse

    @GET("{path}/verses/{bookId}/{chapter}.json")
    suspend fun getVersesForChapter(
        @Path(value = "path", encoded = true) path: String,
        @Path("bookId") bookId: String,
        @Path("chapter") chapter: String,
    ): ChapterContentDto
}