package com.biblelib.feature.reader.home.viewmodel.controller

import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.feature.reader.home.utils.ReaderUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class PreferencesController(
    private val prefsRepo: PrefsRepo,
    private val state: MutableStateFlow<ReaderUiState>,
) {
    fun setFontSize(sp: Float) {
        prefsRepo.fontSizeSp = sp
        state.update { it.copy(fontSizeSp = sp) }
    }

    fun setFontFamily(id: String) {
        prefsRepo.readerFontFamily = id
        state.update { it.copy(fontFamilyId = id) }
    }

    fun setReaderBackground(id: String) {
        prefsRepo.readerBackground = id
        state.update { it.copy(readerBackgroundId = id) }
    }
}