package com.biblelib.feature.selection.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biblelib.core.common.entity.Selectable
import com.biblelib.core.common.entity.UiState
import com.biblelib.core.data.repos.BibleRepo
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.network.dtos.BibleInfoDto
import com.biblelib.feature.selection.utils.GroupingMode
import com.biblelib.feature.selection.viewmodel.controller.FirstTimeSelectionController
import com.biblelib.feature.selection.viewmodel.controller.ReturningSelectionController
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

        const val FIRST_INSTALL_MAX = 7
        const val ADDITIONAL_BIBLES_ALLOWED = 5
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

    private val firstTimeSelection = FirstTimeSelectionController(
        bibleRepo = bibleRepo,
        prefsRepo = prefsRepo,
        context = context,
        scope = viewModelScope,
        uiState = _uiState,
        downloadProgress = _downloadProgress,
        downloadStep = _downloadStep,
    )

    private val returningSelection = ReturningSelectionController(
        bibleRepo = bibleRepo,
        prefsRepo = prefsRepo,
        context = context,
        scope = viewModelScope,
        uiState = _uiState,
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

    private fun currentSelection(): List<BibleInfoDto> =
        _bibles.value.filter { it.isSelected }.map { it.data }

    // -- First-install flow: delegates to FirstTimeSelectionController --

    fun saveSelectionAndDownload() =
        firstTimeSelection.saveSelectionAndDownload(currentSelection())

    fun continuePrimaryDownload() = firstTimeSelection.continuePrimaryDownload()

    fun restartPrimaryDownload() = firstTimeSelection.restartPrimaryDownload()

    // -- Returning-user reselection flow: delegates to ReturningSelectionController --

    fun saveSelectionInBackground() =
        returningSelection.saveSelectionInBackground(currentSelection())

    fun cancelReselection() = returningSelection.cancelReselection()
}
