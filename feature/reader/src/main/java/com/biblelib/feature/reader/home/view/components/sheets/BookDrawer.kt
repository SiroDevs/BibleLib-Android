package com.biblelib.feature.reader.home.view.components.sheets

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.biblelib.core.database.entities.BookEntity
import com.biblelib.feature.reader.home.utils.ReaderUiState

@OptIn(ExperimentalMaterial3Api::class)
// Opens the searchable book picker as a modal sheet.
@Composable
fun BookDrawer(
    state: ReaderUiState,
    onSelect: (BookEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        BoxWithConstraints {
            BookPickerContent(
                state = state,
                selectedBookId = state.activeBook?.id,
                onSelect = onSelect,
                modifier = Modifier,
                listHeight = (maxHeight - 120.dp).coerceAtLeast(120.dp),
            )
        }
    }
}
