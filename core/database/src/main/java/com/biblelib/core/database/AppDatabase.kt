package com.biblelib.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.biblelib.core.database.daos.BibleDao
import com.biblelib.core.database.daos.BookDao
import com.biblelib.core.database.daos.BookmarkDao
import com.biblelib.core.database.daos.ChapterDao
import com.biblelib.core.database.daos.NoteDao
import com.biblelib.core.database.daos.ScriptureItemDao
import com.biblelib.core.database.daos.ScriptureListDao
import com.biblelib.core.database.daos.VerseDao
import com.biblelib.core.database.daos.SearchDao
import com.biblelib.core.database.daos.HistoryDao
import com.biblelib.core.database.entities.BibleEntity
import com.biblelib.core.database.entities.BookEntity
import com.biblelib.core.database.entities.BookmarkEntity
import com.biblelib.core.database.entities.ChapterEntity
import com.biblelib.core.database.entities.NoteEntity
import com.biblelib.core.database.entities.ScriptureItemEntity
import com.biblelib.core.database.entities.ScriptureListEntity
import com.biblelib.core.database.entities.VerseEntity
import com.biblelib.core.database.entities.HistoryEntity
import com.biblelib.core.database.entities.SearchEntity

@Database(
    entities = [
        BibleEntity::class,
        BookEntity::class,
        ChapterEntity::class,
        VerseEntity::class,
        HistoryEntity::class,
        SearchEntity::class,
        BookmarkEntity::class,
        NoteEntity::class,
        ScriptureListEntity::class,
        ScriptureItemEntity::class,
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun biblesDao(): BibleDao
    abstract fun booksDao(): BookDao
    abstract fun chaptersDao(): ChapterDao
    abstract fun versesDao(): VerseDao
    abstract fun historiesDao(): HistoryDao
    abstract fun searchesDao(): SearchDao
    abstract fun bookmarksDao(): BookmarkDao
    abstract fun notesDao(): NoteDao
    abstract fun scriptureListsDao(): ScriptureListDao
    abstract fun scriptureItemsDao(): ScriptureItemDao
}
