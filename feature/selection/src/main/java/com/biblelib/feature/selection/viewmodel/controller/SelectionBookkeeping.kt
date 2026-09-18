package com.biblelib.feature.selection.viewmodel.controller

import android.content.Context
import com.biblelib.core.data.repos.BibleRepo
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.worker.SyncScheduler
import com.biblelib.core.database.entities.BibleEntity
import com.biblelib.core.network.dtos.BibleInfoDto
import com.biblelib.core.network.dtos.primaryCountryName

internal suspend fun persistSelectionBookkeeping(
    bibleRepo: BibleRepo,
    prefsRepo: PrefsRepo,
    context: Context,
    selected: List<BibleInfoDto>,
) {
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
                path = dto.path,
            )
        }
    )
}
