package com.biblelib.feature.bibles.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.biblelib.core.data.repos.BibleRepo
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.worker.SyncScheduler
import com.biblelib.core.data.worker.SyncWorker
import com.biblelib.core.database.entities.BibleEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BiblesUiState(
    val bibles: List<BibleEntity> = emptyList(),
    val primaryAbbr: String = "",
    val downloadProgress: Map<String, Float> = emptyMap(),
    val pendingDelete: BibleEntity? = null,
    val showPrimaryPicker: Boolean = false,
    val isLoading: Boolean = true,
    val multiBibleEnabled: Boolean = false,
    val secondaryBibles: List<String> = emptyList(),
    val showManagementInfo: Boolean = false,
    val showFirstOpenPrompt: Boolean = false,
)

@HiltViewModel
class BiblesViewModel @Inject constructor(
    private val bibleRepo: BibleRepo,
    private val prefsRepo: PrefsRepo,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val workManager = WorkManager.getInstance(context)
    private val observedAbbrs = mutableSetOf<String>()

    private val _uiState = MutableStateFlow(BiblesUiState())
    val uiState: StateFlow<BiblesUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            val bibles = bibleRepo.getbibles().sortedBy { it.sortOrder }
            val primary = prefsRepo.primaryBible
            val owned = bibles.map { it.abbreviation }.toSet()

            var secondary = prefsRepo.getSecondaryBibleList().filter { it in owned && it != primary }
            if (secondary.isEmpty()) {
                secondary = bibles
                    .filter { it.abbreviation != primary }
                    .take(PrefsRepo.DEFAULT_SECONDARY_BIBLES)
                    .map { it.abbreviation }
            }
            prefsRepo.setSecondaryBibleList(secondary)

            _uiState.update {
                it.copy(
                    bibles = bibles,
                    primaryAbbr = primary,
                    isLoading = false,
                    multiBibleEnabled = prefsRepo.multiBibleReaderEnabled,
                    secondaryBibles = secondary,
                    showFirstOpenPrompt = !prefsRepo.hasSeenBiblesManagementTip,
                )
            }
            observeDownloads(bibles)
        }
    }

    private fun observeDownloads(bibles: List<BibleEntity>) {
        bibles
            .filter { !it.isDownloaded && it.abbreviation !in observedAbbrs }
            .forEach { bible ->
                observedAbbrs += bible.abbreviation
                viewModelScope.launch {
                    workManager
                        .getWorkInfosByTagFlow(bible.abbreviation)
                        .collect { infos ->
                            val info = infos
                                .filterNot { it.state.isFinished }
                                .maxByOrNull { it.id.hashCode() }
                                ?: infos.maxByOrNull { it.id.hashCode() }
                            val progress = info?.progress?.getFloat(SyncWorker.KEY_PROGRESS, 0f) ?: 0f
                            if (progress > 0f) {
                                _uiState.update {
                                    it.copy(downloadProgress = it.downloadProgress + (bible.abbreviation to progress))
                                }
                            }
                            if (info != null && info.state.isFinished) {
                                observedAbbrs -= bible.abbreviation
                                load()
                            }
                        }
                }
            }
    }

    fun retryDownload(abbr: String) {
        viewModelScope.launch {
            observedAbbrs -= abbr
            _uiState.update { it.copy(downloadProgress = it.downloadProgress + (abbr to 0f)) }
            SyncScheduler.scheduleSecondaryDownload(context, abbr)
            load()
        }
    }

    fun restartDownload(abbr: String) {
        viewModelScope.launch {
            observedAbbrs -= abbr
            SyncScheduler.cancelDownload(context, abbr)
            bibleRepo.clearBibleContent(abbr)
            _uiState.update { it.copy(downloadProgress = it.downloadProgress + (abbr to 0f)) }
            SyncScheduler.scheduleSecondaryDownload(context, abbr)
            load()
        }
    }

    fun openPrimaryPicker() = _uiState.update { it.copy(showPrimaryPicker = true) }
    fun dismissPrimaryPicker() = _uiState.update { it.copy(showPrimaryPicker = false) }

    fun setPrimaryBible(abbr: String) {
        val newName = _uiState.value.bibles.find { it.abbreviation == abbr }?.name
            ?: _uiState.value.primaryAbbr

        val bible = _uiState.value.bibles.find { it.abbreviation == abbr } ?: return
        if (!bible.isDownloaded) return

        prefsRepo.primaryBible = abbr
        prefsRepo.lastBibleAbbr = abbr
        prefsRepo.lastBible = newName
        prefsRepo.lastBookId = ""
        prefsRepo.lastChapterId = ""
        val updatedSecondary = prefsRepo.getSecondaryBibleList() - abbr
        prefsRepo.setSecondaryBibleList(updatedSecondary)

        _uiState.update {
            it.copy(primaryAbbr = abbr, showPrimaryPicker = false, secondaryBibles = updatedSecondary)
        }
    }

    fun requestDelete(bible: BibleEntity) = _uiState.update { it.copy(pendingDelete = bible) }
    fun cancelDelete() = _uiState.update { it.copy(pendingDelete = null) }

    fun confirmDelete() {
        val bible = _uiState.value.pendingDelete ?: return
        viewModelScope.launch {
            SyncScheduler.cancelDownload(context, bible.abbreviation)
            bibleRepo.deleteBible(bible.abbreviation)

            val remaining = prefsRepo.getSelectedBibleList() - bible.abbreviation
            prefsRepo.selectedBibles = remaining.joinToString(",")
            prefsRepo.setSecondaryBibleList(prefsRepo.getSecondaryBibleList() - bible.abbreviation)

            if (prefsRepo.primaryBible == bible.abbreviation) {
                prefsRepo.primaryBible = remaining.firstOrNull() ?: ""
            }

            _uiState.update { it.copy(pendingDelete = null) }
            load()
        }
    }

    fun requestReselection() {
        prefsRepo.selectAfresh = true
    }

    fun setMultiBibleEnabled(enabled: Boolean) {
        prefsRepo.multiBibleReaderEnabled = enabled
        _uiState.update { it.copy(multiBibleEnabled = enabled) }
    }

    fun toggleSecondaryBible(abbr: String) {
        val current = _uiState.value.secondaryBibles
        val updated = if (abbr in current) {
            current - abbr
        } else {
            if (current.size >= PrefsRepo.MAX_SECONDARY_BIBLES) return
            current + abbr
        }
        _uiState.update { it.copy(secondaryBibles = updated) }
        prefsRepo.setSecondaryBibleList(updated)
    }

    fun moveSecondaryBible(abbr: String, direction: Int) {
        val list = _uiState.value.secondaryBibles.toMutableList()
        val idx = list.indexOf(abbr)
        val newIdx = idx + direction
        if (idx < 0 || newIdx !in list.indices) return
        val item = list.removeAt(idx)
        list.add(newIdx, item)
        _uiState.update { it.copy(secondaryBibles = list) }
        prefsRepo.setSecondaryBibleList(list)
    }

    fun openManagementInfo() = _uiState.update { it.copy(showManagementInfo = true) }
    fun dismissManagementInfo() = _uiState.update { it.copy(showManagementInfo = false) }

    fun dismissFirstOpenPrompt(showInfoNext: Boolean) {
        prefsRepo.hasSeenBiblesManagementTip = true
        _uiState.update {
            it.copy(
                showFirstOpenPrompt = false,
                showManagementInfo = showInfoNext,
            )
        }
    }
}
