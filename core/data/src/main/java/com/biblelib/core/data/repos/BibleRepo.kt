package com.biblelib.core.data.repos

import android.util.Log
import com.biblelib.core.common.entity.VerseDisplay
import com.biblelib.core.database.daos.BibleDao
import com.biblelib.core.database.daos.BookDao
import com.biblelib.core.database.daos.ChapterDao
import com.biblelib.core.database.daos.VerseDao
import com.biblelib.core.database.entities.BibleEntity
import com.biblelib.core.database.entities.BookEntity
import com.biblelib.core.database.entities.ChapterEntity
import com.biblelib.core.database.entities.VerseEntity
import com.biblelib.core.network.dtos.BibleInfoDto
import com.biblelib.core.network.dtos.ChapterContentDto
import com.biblelib.core.network.dtos.ContentItemDto
import com.biblelib.core.network.dtos.primaryCountryName
import com.biblelib.core.network.util.RetryPolicy
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import com.biblelib.core.network.services.BibleLibService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BibleRepo @Inject constructor(
    private val service: BibleLibService,
    private val bibleDao: BibleDao,
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao,
    private val verseDao: VerseDao,
) {
    private val gson = Gson()
    suspend fun fetchAvailableBibles(): List<BibleInfoDto> =
        withContext(Dispatchers.IO) {
            val groups = RetryPolicy.retrying { service.getGroups() }
            coroutineScope {
                groups.map { group ->
                    async {
                        try {
                            RetryPolicy.retrying { service.getGroupInfo(group) }
                        } catch (e: Exception) {
                            Log.w(TAG, "⚠️ Couldn't fetch group '$group', skipping", e)
                            emptyList()
                        }
                    }
                }.awaitAll().flatten()
            }
        }

    private suspend fun resolvePath(abbr: String): String =
        bibleDao.getByAbbr(abbr)?.path?.ifBlank { abbr } ?: abbr

    suspend fun downloadBible(
        abbr: String,
        onProgress: suspend (step: String, progress: Float) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        val path = resolvePath(abbr)
        Log.d(TAG, "▶ Downloading $abbr bible from path=$path")

        val reportProgress: suspend (String, Float) -> Unit = { step, progress ->
            bibleDao.updateProgress(abbr, progress)
            onProgress(step, progress)
        }

        try {
            reportProgress("Fetching books...", 0.05f)
            val booksResp = RetryPolicy.retrying { service.getBooks(path) }
            val bookEntities = booksResp.mapIndexed { i, dto ->
                BookEntity(
                    id = dto.id,
                    bibleAbbr = abbr,
                    abbreviation = dto.abbreviation,
                    name = dto.name,
                    nameLong = dto.nameLong,
                    sortOrder = i,
                )
            }
            bookDao.insertAll(bookEntities)
            Log.d(TAG, "✅ ${bookEntities.size} books saved for $abbr")

            reportProgress("Fetching chapters...", 0.15f)
            val chaptersResp = RetryPolicy.retrying { service.getChapters(path) }
            val chapterEntities = mutableListOf<ChapterEntity>()
            chaptersResp.forEach { (_, chapters) ->
                chapters.forEach { dto ->
                    chapterEntities.add(
                        ChapterEntity(
                            id = dto.id,
                            bibleAbbr = abbr,
                            bookId = dto.bookId,
                            number = dto.number,
                            reference = dto.reference,
                        )
                    )
                }
            }
            chapterDao.insertAll(chapterEntities)
            Log.d(TAG, "✅ ${chapterEntities.size} chapters saved for $abbr")

            val chaptersByBook = chapterEntities.groupBy { it.bookId }
            val bookIds = booksResp.map { it.id }.filter { chaptersByBook[it]?.isNotEmpty() == true }

            val alreadyCachedChapterIds = verseDao.getCachedChapterIds(abbr).toSet()

            reportProgress("Fetching verses...", 0.25f)

            val semaphore = Semaphore(MAX_CONCURRENT_BOOK_BATCHES)
            val progressMutex = Mutex()
            var completedBooks = 0

            coroutineScope {
                bookIds.map { bookId ->
                    async {
                        semaphore.withPermit {
                            try {
                                val chaptersForBook = chaptersByBook[bookId].orEmpty()
                                val pendingChapters = chaptersForBook.filter { it.id !in alreadyCachedChapterIds }
                                if (pendingChapters.isNotEmpty()) {
                                    val verseEntities = fetchVersesForBook(abbr, path, bookId, pendingChapters)
                                    if (verseEntities.isNotEmpty()) {
                                        verseDao.insertAll(verseEntities)
                                    }
                                }
                            } catch (e: Exception) {
                                Log.w(TAG, "⚠️ Book $bookId failed for $abbr, continuing with others", e)
                            }

                            progressMutex.withLock {
                                completedBooks++
                                val fraction = completedBooks.toFloat() / bookIds.size
                                reportProgress(
                                    "Fetching verses ($bookId, $completedBooks/${bookIds.size})...",
                                    0.25f + fraction * 0.7f
                                )
                            }
                        }
                    }
                }.awaitAll()
            }

            Log.d(TAG, "✅ Verses saved in ${bookIds.size} book batches for $abbr")

            bibleDao.markDownloaded(abbr)
            onProgress("Done!", 1.0f)
            Log.d(TAG, "✅ Download complete for $abbr")
        } catch (e: Exception) {
            val lastKnownProgress = bibleDao.getByAbbr(abbr)?.downloadProgress ?: 0f
            bibleDao.markFailed(abbr, lastKnownProgress)
            throw e
        }
    }

    private suspend fun fetchVersesForBook(
        abbr: String,
        path: String,
        bookId: String,
        chapters: List<ChapterEntity>,
    ): List<VerseEntity> {
        val verseEntities = mutableListOf<VerseEntity>()
        for (chapter in chapters) {
            try {
                val content = RetryPolicy.retrying {
                    service.getVersesForChapter(path, bookId, chapter.number)
                }
                val verses = extractVerses(content)
                verseEntities.add(
                    VerseEntity(
                        chapterId = chapter.id,
                        bibleAbbr = abbr,
                        bookId = bookId,
                        verseCount = content.verseCount,
                        contentJson = gson.toJson(verses),
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Skipping unparseable chapter $bookId/${chapter.number} for $abbr", e)
            }
        }
        return verseEntities
    }

    suspend fun getLocalBooks(abbr: String): List<BookEntity> =
        withContext(Dispatchers.IO) { bookDao.getByBible(abbr) }

    suspend fun getLocalChapters(abbr: String, bookId: String): List<ChapterEntity> =
        withContext(Dispatchers.IO) { chapterDao.getByBook(abbr, bookId) }

    suspend fun getLocalVerses(abbr: String, chapterId: String): List<VerseDisplay>? =
        withContext(Dispatchers.IO) {
            val entity = verseDao.getChapter(abbr, chapterId) ?: return@withContext null
            val type = object : TypeToken<List<VerseDisplay>>() {}.type
            gson.fromJson<List<VerseDisplay>>(entity.contentJson, type)
        }

    suspend fun searchVerses(abbr: String, query: String): List<VerseDisplay> =
        withContext(Dispatchers.IO) {
            val entities = verseDao.searchInBible(abbr, query)
            val type = object : TypeToken<List<VerseDisplay>>() {}.type
            entities.flatMap { entity ->
                val verses: List<VerseDisplay> = gson.fromJson(entity.contentJson, type)
                verses.filter { it.text.contains(query, ignoreCase = true) }
            }
        }

    suspend fun getbibles(): List<BibleEntity> =
        withContext(Dispatchers.IO) { bibleDao.getAll() }

    suspend fun saveBibles(entities: List<BibleEntity>) =
        withContext(Dispatchers.IO) { bibleDao.insertAll(entities) }

    suspend fun deleteBible(abbr: String) = withContext(Dispatchers.IO) {
        bibleDao.deleteByAbbr(abbr)
        bookDao.deleteByBible(abbr)
        chapterDao.deleteByBible(abbr)
        verseDao.deleteByBible(abbr)
    }

    suspend fun clearBibleContent(abbr: String) = withContext(Dispatchers.IO) {
        bookDao.deleteByBible(abbr)
        chapterDao.deleteByBible(abbr)
        verseDao.deleteByBible(abbr)
        bibleDao.getByAbbr(abbr)?.let { existing ->
            bibleDao.insert(
                existing.copy(isDownloaded = false, downloadProgress = 0f, downloadFailed = false)
            )
        }
    }

    suspend fun markDownloadFailed(abbr: String) = withContext(Dispatchers.IO) {
        val progress = bibleDao.getByAbbr(abbr)?.downloadProgress ?: 0f
        bibleDao.markFailed(abbr, progress)
    }

    suspend fun deleteAllData() = withContext(Dispatchers.IO) {
        bibleDao.deleteAll()
        bookDao.deleteAll()
        chapterDao.deleteAll()
        verseDao.deleteAll()
    }

    private fun extractVerses(content: ChapterContentDto): List<VerseDisplay> {
        val verses = mutableListOf<VerseDisplay>()
        var currentVerseNumber = 0
        var currentVerseId = ""

        fun walkItems(items: List<ContentItemDto>) {
            for (item in items) {
                @Suppress("SENSELESS_COMPARISON")
                if (item == null) continue

                if (item.type == "tag" && item.name == "verse") {
                    currentVerseNumber = item.attrs?.get("number")?.toIntOrNull() ?: currentVerseNumber
                    currentVerseId = item.attrs?.get("sid")?.replace(" ", ".") ?: ""
                } else if (item.type == "text" && item.text != null) {
                    val verseId = item.attrs?.get("verseId") ?: ""
                    val text = item.text!!.trim()
                    if (verseId.isNotEmpty() && text.isNotEmpty() && currentVerseNumber > 0) {
                        val existing = verses.lastOrNull { it.verseId == verseId }
                        if (existing != null) {
                            val idx = verses.lastIndexOf(existing)
                            verses[idx] = existing.copy(text = existing.text + " " + text)
                        } else {
                            verses.add(
                                VerseDisplay(
                                    verseId = verseId,
                                    number = currentVerseNumber,
                                    text = text,
                                    chapterId = content.id,
                                    bookId = content.bookId,
                                )
                            )
                        }
                    }
                }
                item.items?.let { walkItems(it) }
            }
        }

        walkItems(content.content)
        return verses.sortedBy { it.number }
    }

    companion object {
        private const val TAG = "BibleRepo"
        private const val MAX_CONCURRENT_BOOK_BATCHES = 20
    }
}
