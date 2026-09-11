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
 * Drives the selection flow the very first time someone sets up the app: the primary Bible
 * is downloaded synchronously with visible step/progress reporting (the "Saving..." screen),
 * and only once that succeeds are the remaining Bibles queued as background downloads. This
 * mirrors what a first-time user expects — something visibly happening before they're dropped
 * into the reader — as opposed to [ReturningSelectionController], which sends an already
 * set-up user home immediately and downloads everything in the background.
 */
class FirstTimeSelectionController(
    private val bibleRepo: BibleRepo,
    private val prefsRepo: PrefsRepo,
    private val context: Context,
    private val scope: CoroutineScope,
    private val uiState: MutableStateFlow<UiState>,
    private val downloadProgress: MutableStateFlow<Float>,
    private val downloadStep: MutableStateFlow<String>,
) {
    companion object {
        private const val TAG = "FirstTimeSelectionController"
    }

    private var pendingSelection: List<BibleInfoDto> = emptyList()

    fun saveSelectionAndDownload(selected: List<BibleInfoDto>) {
        if (selected.isEmpty()) return
        pendingSelection = selected

        uiState.value = UiState.Saving
        downloadProgress.value = 0f
        downloadStep.value = "Preparing..."

        scope.launch {
            try {
                persistSelectionBookkeeping(bibleRepo, prefsRepo, context, selected)
                downloadPrimaryAndQueueSecondaries(selected)
                uiState.value = UiState.Saved
            } catch (e: Exception) {
                Log.e(TAG, "saveSelectionAndDownload", e)
                uiState.value = UiState.SaveFailed(
                    message = "Failed to download the Bible. You can continue where it left off or restart.",
                    progress = downloadProgress.value,
                )
            }
        }
    }

    fun continuePrimaryDownload() {
        val selected = pendingSelection
        if (selected.isEmpty()) return

        uiState.value = UiState.Saving
        downloadStep.value = "Resuming download..."

        scope.launch {
            try {
                downloadProgress.value = bibleRepo.getbibles()
                    .find { it.abbreviation == selected.first().abbreviation }
                    ?.downloadProgress ?: 0f

                downloadPrimaryAndQueueSecondaries(selected)
                uiState.value = UiState.Saved
            } catch (e: Exception) {
                Log.e(TAG, "continuePrimaryDownload", e)
                uiState.value = UiState.SaveFailed(
                    message = "Still couldn't finish the download. You can continue or restart.",
                    progress = downloadProgress.value,
                )
            }
        }
    }

    fun restartPrimaryDownload() {
        val selected = pendingSelection
        if (selected.isEmpty()) return

        uiState.value = UiState.Saving
        downloadProgress.value = 0f
        downloadStep.value = "Restarting download..."

        scope.launch {
            try {
                bibleRepo.clearBibleContent(selected.first().abbreviation)
                downloadPrimaryAndQueueSecondaries(selected)
                uiState.value = UiState.Saved
            } catch (e: Exception) {
                Log.e(TAG, "restartPrimaryDownload", e)
                uiState.value = UiState.SaveFailed(
                    message = "Failed to download the Bible. You can continue where it left off or restart.",
                    progress = downloadProgress.value,
                )
            }
        }
    }

    private suspend fun downloadPrimaryAndQueueSecondaries(selected: List<BibleInfoDto>) {
        val primary = selected.first()
        if (bibleRepo.getbibles().none { it.abbreviation == primary.abbreviation }) {
            persistSelectionBookkeeping(bibleRepo, prefsRepo, context, selected)
        }

        bibleRepo.downloadBible(primary.abbreviation) { step, progress ->
            downloadStep.value = step
            downloadProgress.value = progress
        }

        prefsRepo.isPrimaryLoaded = true

        SyncScheduler.scheduleSecondaryDownloads(
            context,
            selected.drop(1).map { it.abbreviation },
        )
    }
}
