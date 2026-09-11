package com.biblelib.feature.selection.viewmodel.controller

import android.content.Context
import android.util.Log
import com.biblelib.core.common.entity.UiState
import com.biblelib.core.data.repos.BibleRepo
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.worker.SyncScheduler
import com.biblelib.core.network.dtos.BibleInfoDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Drives the selection flow when an already set-up user reselects Bibles (via "Change
 * Selection" on the Bibles screen). Unlike [FirstTimeSelectionController], nothing is
 * downloaded synchronously here — the new selection is persisted, every Bible (primary
 * included) is queued as background work, and the caller can navigate home right away.
 * Downloads then report progress the same way secondary-Bible downloads already do
 * (Bibles screen pills, Reader banner, etc).
 */
class ReturningSelectionController(
    private val bibleRepo: BibleRepo,
    private val prefsRepo: PrefsRepo,
    private val context: Context,
    private val scope: CoroutineScope,
    private val uiState: MutableStateFlow<UiState>,
) {
    companion object {
        private const val TAG = "ReturningSelectionController"
    }

    fun saveSelectionInBackground(selected: List<BibleInfoDto>) {
        if (selected.isEmpty()) return

        scope.launch {
            try {
                persistSelectionBookkeeping(bibleRepo, prefsRepo, context, selected)
                SyncScheduler.scheduleSecondaryDownloads(
                    context,
                    selected.map { it.abbreviation },
                )
                uiState.value = UiState.Saved
            } catch (e: Exception) {
                Log.e(TAG, "saveSelectionInBackground", e)
                uiState.value = UiState.Error(
                    "Couldn't save your selection. Please try again."
                )
            }
        }
    }

    /** Backs out of a reselection started from "Change Selection" without changing anything. */
    fun cancelReselection() {
        prefsRepo.selectAfresh = false
    }
}
