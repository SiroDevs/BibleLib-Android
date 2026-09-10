package com.biblelib.feature.selection.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biblelib.core.common.entity.Selectable
import com.biblelib.core.common.entity.UiState
import com.biblelib.core.data.repos.BibleRepo
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.worker.SyncScheduler
import com.biblelib.core.database.entities.BibleEntity
import com.biblelib.core.network.dtos.BibleInfoDto
import com.biblelib.core.network.dtos.primaryCountryName
import com.biblelib.feature.selection.utils.GroupingMode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectionViewModel @Inject constructor(
    private val bibleRepo: BibleRepo,
    private val prefsRepo: PrefsRepo,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    companion object {
        private const val TAG = "SelectionViewModel"

        const val FIRST_INSTALL_MAX = 5
        const val ADDITIONAL_BIBLES_ALLOWED = 7
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    private val _downloadStep = MutableStateFlow("Preparing ...")
    val downloadStep: StateFlow<String> = _downloadStep.asStateFlow()

    private val _bibles = MutableStateFlow<List<Selectable<BibleInfoDto>>>(emptyList())
    val bibles = _bibles.asStateFlow()

    private val _groupingMode = MutableStateFlow(GroupingMode.Default)
    val groupingMode = _groupingMode.asStateFlow()

    val isFirstInstall: Boolean
        get() = !prefsRepo.isDataSelected

    private val _maxSelections = MutableStateFlow(FIRST_INSTALL_MAX)
    val maxSelections: StateFlow<Int> = _maxSelections.asStateFlow()

    private var pendingSelection: List<BibleInfoDto> = emptyList()

    val selectedCount: StateFlow<Int> =
        _bibles
            .map { list -> list.count { it.isSelected } }
            .stateIn(
                viewModelScope,
                SharingStarted.Companion.Eagerly,
                0
            )

    val canProceed: StateFlow<Boolean> =
        selectedCount
            .map { it > 0 }
            .stateIn(
                viewModelScope,
                SharingStarted.Companion.Eagerly,
                false
            )

    fun fetchBibles() {
        _uiState.value = UiState.Loading

        viewModelScope.launch {
            try {
                val selected = prefsRepo.getSelectedBibleList().toSet()

                _maxSelections.value = if (isFirstInstall) {
                    FIRST_INSTALL_MAX
                } else {
                    FIRST_INSTALL_MAX + ADDITIONAL_BIBLES_ALLOWED
                }

                _bibles.value = bibleRepo.fetchAvailableBibles().map {
                    Selectable(
                        data = it,
                        isSelected = it.abbreviation in selected
                    )
                }
                _uiState.value = UiState.Loaded
            } catch (e: Exception) {
                Log.e(TAG, "fetchBibles", e)

                _uiState.value = UiState.Error(
                    "Could not load Bibles. Please check your connection and try again."
                )
            }
        }
    }

    fun setGroupingMode(mode: GroupingMode) {
        _groupingMode.value = mode
    }

    fun toggleSelection(abbr: String) {
        val current = _bibles.value

        val target = current.find {
            it.data.abbreviation == abbr
        } ?: return

        val shouldSelect = !target.isSelected

        if (shouldSelect && selectedCount.value >= _maxSelections.value) {
            return
        }

        _bibles.value = current.map {
            if (it.data.abbreviation == abbr) {
                it.copy(isSelected = shouldSelect)
            } else {
                it
            }
        }
    }

    fun saveSelectionAndDownload() {
        val selected = _bibles.value
            .filter { it.isSelected }
            .map { it.data }

        if (selected.isEmpty()) return
        pendingSelection = selected

        _uiState.value = UiState.Saving
        _downloadProgress.value = 0f
        _downloadStep.value = "Preparing..."

        viewModelScope.launch {
            try {
                persistSelectionBookkeeping(selected)
                downloadPrimaryAndQueueSecondaries(selected)
                _uiState.value = UiState.Saved
            } catch (e: Exception) {
                Log.e(TAG, "saveSelection", e)
                _uiState.value = UiState.SaveFailed(
                    message = "Failed to download the Bible. You can continue where it left off or restart.",
                    progress = _downloadProgress.value,
                )
            }
        }
    }

    fun continuePrimaryDownload() {
        val selected = pendingSelection
        if (selected.isEmpty()) return

        _uiState.value = UiState.Saving
        _downloadStep.value = "Resuming download..."

        viewModelScope.launch {
            try {
                _downloadProgress.value = bibleRepo.getbibles()
                    .find { it.abbreviation == selected.first().abbreviation }
                    ?.downloadProgress ?: 0f

                downloadPrimaryAndQueueSecondaries(selected)
                _uiState.value = UiState.Saved
            } catch (e: Exception) {
                Log.e(TAG, "continuePrimaryDownload", e)
                _uiState.value = UiState.SaveFailed(
                    message = "Still couldn't finish the download. You can continue or restart.",
                    progress = _downloadProgress.value,
                )
            }
        }
    }

    fun restartPrimaryDownload() {
        val selected = pendingSelection
        if (selected.isEmpty()) return

        _uiState.value = UiState.Saving
        _downloadProgress.value = 0f
        _downloadStep.value = "Restarting download..."

        viewModelScope.launch {
            try {
                bibleRepo.clearBibleContent(selected.first().abbreviation)
                downloadPrimaryAndQueueSecondaries(selected)
                _uiState.value = UiState.Saved
            } catch (e: Exception) {
                Log.e(TAG, "restartPrimaryDownload", e)
                _uiState.value = UiState.SaveFailed(
                    message = "Failed to download the Bible. You can continue where it left off or restart.",
                    progress = _downloadProgress.value,
                )
            }
        }
    }

    private suspend fun persistSelectionBookkeeping(selected: List<BibleInfoDto>) {
        val primary = selected.first()
        val newAbbrs = selected.map { it.abbreviation }.toSet()

        val previouslyOwned = prefsRepo.getSelectedBibleList()
        val removed = previouslyOwned.filter { it !in newAbbrs }
        removed.forEach { abbr ->
            SyncScheduler.cancelDownload(context, abbr)
            bibleRepo.deleteBible(abbr)
        }

        prefsRepo.selectedBibles = selected.joinToString(",") { it.abbreviation }

        prefsRepo.primaryBible = primary.abbreviation
        prefsRepo.isDataSelected = true
        prefsRepo.selectAfresh = false

        prefsRepo.lastBible = primary.name
        prefsRepo.lastBibleAbbr = primary.abbreviation
        prefsRepo.lastBookId = ""
        prefsRepo.lastChapterId = ""

        val prunedSecondary = prefsRepo.getSecondaryBibleList()
            .filter { it in newAbbrs && it != primary.abbreviation }
        val secondary = prunedSecondary.ifEmpty {
            selected.drop(1)
                .take(PrefsRepo.DEFAULT_SECONDARY_BIBLES)
                .map { it.abbreviation }
        }
        prefsRepo.setSecondaryBibleList(secondary)

        bibleRepo.saveBibles(
            selected.mapIndexed { index, dto ->
                BibleEntity(
                    abbreviation = dto.abbreviation,
                    name = dto.name,
                    description = dto.description,
                    languageName = dto.language.name,
                    scriptDirection = dto.language.scriptDirection,
                    sortOrder = index,
                    isDownloaded = false,
                    countryName = dto.primaryCountryName(),
                )
            }
        )
    }

    private suspend fun downloadPrimaryAndQueueSecondaries(selected: List<BibleInfoDto>) {
        val primary = selected.first()
        if (bibleRepo.getbibles().none { it.abbreviation == primary.abbreviation }) {
            persistSelectionBookkeeping(selected)
        }

        bibleRepo.downloadBible(primary.abbreviation) { step, progress ->
            _downloadStep.value = step
            _downloadProgress.value = progress
        }

        prefsRepo.isPrimaryLoaded = true

        SyncScheduler.scheduleSecondaryDownloads(
            context,
            selected.drop(1).map { it.abbreviation },
        )
    }
}
