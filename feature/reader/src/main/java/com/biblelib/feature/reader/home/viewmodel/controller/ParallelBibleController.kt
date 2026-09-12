package com.biblelib.feature.reader.home.viewmodel.controller

import com.biblelib.core.common.entity.VerseDisplay
import com.biblelib.core.data.repos.BibleRepo
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.feature.reader.home.utils.ReaderUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class ParallelBibleController(
    private val bibleRepo: BibleRepo,
    private val prefsRepo: PrefsRepo,
    private val state: MutableStateFlow<ReaderUiState>,
) {
    val isEnabled: Boolean
        get() = prefsRepo.multiBibleReaderEnabled

    suspend fun loadParallel(primaryAbbr: String, chapterId: String): Map<String, List<VerseDisplay>> {
        if (!isEnabled) return emptyMap()

        val downloadedAbbrs = state.value.savedBibles
            .filter { it.isDownloaded }
            .map { it.abbreviation }
            .toSet()

        val orderedSecondary = prefsRepo.getSecondaryBibleList()
            .filter { it != primaryAbbr && it in downloadedAbbrs }
            .ifEmpty {
                state.value.savedBibles
                    .filter { it.abbreviation != primaryAbbr && it.isDownloaded }
                    .map { it.abbreviation }
            }

        val parallelMap = mutableMapOf<String, List<VerseDisplay>>()
        orderedSecondary.forEach { sAbbr ->
            val verses = bibleRepo.getLocalVerses(sAbbr, chapterId)
            if (verses != null) parallelMap[sAbbr] = verses
        }
        return parallelMap
    }

    fun setEnabled(enabled: Boolean) {
        prefsRepo.multiBibleReaderEnabled = enabled
        state.update { it.copy(multiBibleReaderEnabled = enabled) }
    }
}
