package com.biblelib.feature.reader.main.viewmodel.controller

import android.content.Context
import androidx.work.WorkManager
import com.biblelib.core.data.repos.BibleRepo
import com.biblelib.core.data.worker.SyncScheduler
import com.biblelib.core.data.worker.SyncWorker
import com.biblelib.core.database.entities.BibleEntity
import com.biblelib.feature.reader.main.utils.ReaderUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DownloadController(
    private val bibleRepo: BibleRepo,
    private val context: Context,
    private val scope: CoroutineScope,
    private val state: MutableStateFlow<ReaderUiState>,
    private val onBibleDownloaded: (abbr: String) -> Unit = {},
) {
    private val workManager = WorkManager.getInstance(context)
    private val observedAbbrs = mutableSetOf<String>()

    fun observeDownloads(bibles: List<BibleEntity>) {
        bibles
            .filter { !it.isDownloaded && it.abbreviation !in observedAbbrs }
            .forEach { bible ->
                observedAbbrs += bible.abbreviation
                scope.launch {
                    workManager
                        .getWorkInfosByTagFlow(bible.abbreviation)
                        .collect { infos ->
                            val info = infos
                                .filterNot { it.state.isFinished }
                                .maxByOrNull { it.id.hashCode() }
                                ?: infos.maxByOrNull { it.id.hashCode() }
                            val progress =
                                info?.progress?.getFloat(SyncWorker.KEY_PROGRESS, 0f) ?: 0f
                            if (progress > 0f) {
                                state.update {
                                    it.copy(downloadProgress = it.downloadProgress + (bible.abbreviation to progress))
                                }
                            }
                            if (info != null && info.state.isFinished) {
                                observedAbbrs -= bible.abbreviation
                                state.update {
                                    it.copy(savedBibles = bibleRepo.getbibles())
                                }
                                onBibleDownloaded(bible.abbreviation)
                            }
                        }
                }
            }
    }

    fun retryBibleDownload(abbr: String) {
        observedAbbrs -= abbr
        SyncScheduler.scheduleSecondaryDownload(context, abbr)
        observeDownloads(listOf(placeholderBible(abbr)))
    }

    fun restartBibleDownload(abbr: String) {
        observedAbbrs -= abbr
        SyncScheduler.cancelDownload(context, abbr)
        scope.launch {
            bibleRepo.clearBibleContent(abbr)
            SyncScheduler.scheduleSecondaryDownload(context, abbr)
            observeDownloads(listOf(placeholderBible(abbr)))
        }
    }

    private fun placeholderBible(abbr: String) = BibleEntity(
        abbreviation = abbr, name = "", description = "", languageName = "",
        scriptDirection = "LTR",
    )
}