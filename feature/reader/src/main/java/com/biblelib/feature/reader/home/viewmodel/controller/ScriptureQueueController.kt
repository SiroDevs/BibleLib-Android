package com.biblelib.feature.reader.home.viewmodel.controller

import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.repos.ScriptureQueueRepo
import com.biblelib.core.database.entities.ScriptureItemEntity
import com.biblelib.feature.reader.home.utils.ReaderUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScriptureQueueController(
    private val scriptureQueueRepo: ScriptureQueueRepo,
    private val prefsRepo: PrefsRepo,
    private val contentController: ContentController,
    private val scope: CoroutineScope,
    private val state: MutableStateFlow<ReaderUiState>,
) {
    fun observeQueue() {
        scope.launch {
            combine(
                scriptureQueueRepo.items,
                scriptureQueueRepo.activeItemId,
            ) { items, activeId -> items to activeId }
                .collect { (items, activeId) ->
                    state.update { it.copy(queueItems = items, queueActiveItemId = activeId) }
                }
        }
    }

    fun jumpToQueueItem(item: ScriptureItemEntity) {
        scriptureQueueRepo.setActiveItem(item.id)
        val abbr = state.value.activeBibleAbbr
        scope.launch {
            contentController.loadBooks(abbr, item.bookId, item.chapterId)
            state.update { it.copy(restoreVerseId = item.verseId, highlightQuery = null) }
            prefsRepo.lastVerseId = item.verseId
        }
    }

    fun dismissScriptureQueue() {
        scriptureQueueRepo.dismiss()
    }
}