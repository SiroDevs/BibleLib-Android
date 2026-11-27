package com.biblelib.feature.help.view

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biblelib.feature.help.R

// Renders the feedback form and its attachment controls.
@Composable
fun HelpFeedbackForm(
    modifier: Modifier,
    title: String,
    onTitleChange: (String) -> Unit,
    titleError: Boolean,
    description: String,
    onDescriptionChange: (String) -> Unit,
    descriptionError: Boolean,
    attachedUris: List<Uri>,
    maxAttachments: Int,
    onPickAttachments: () -> Unit,
    onRemoveAttachment: (Uri) -> Unit,
    onSubmit: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HelpIntroCard()
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text(stringResource(R.string.help_form_title)) },
            placeholder = { Text(stringResource(R.string.help_title_placeholder)) },
            isError = titleError,
            supportingText = if (titleError) {
                { Text(stringResource(R.string.help_title_required)) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text(stringResource(R.string.help_description)) },
            placeholder = { Text(stringResource(R.string.help_description_placeholder)) },
            isError = descriptionError,
            supportingText = if (descriptionError) {
                { Text(stringResource(R.string.help_description_required)) }
            } else null,
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
            maxLines = 8,
        )
        HelpAttachmentList(
            attachedUris = attachedUris,
            maxAttachments = maxAttachments,
            onPickAttachments = onPickAttachments,
            onRemoveAttachment = onRemoveAttachment,
        )
        HorizontalDivider()
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            androidx.compose.material3.Icon(Icons.Default.Email, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.help_contact_us), style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(8.dp))
    }
}

// Introduces the help form with a short support message.
@Composable
private fun HelpIntroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.help_intro_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.help_intro_summary),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}
