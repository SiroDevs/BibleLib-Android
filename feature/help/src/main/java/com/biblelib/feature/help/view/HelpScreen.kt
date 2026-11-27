package com.biblelib.feature.help.view

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.biblelib.core.ui.components.action.AppTopBar
import com.biblelib.feature.help.R

private const val HELP_MAX_ATTACHMENTS = 5

@OptIn(ExperimentalMaterial3Api::class)
// Captures feedback and opens an email draft with device details.
@Composable
fun HelpScreen(
    navController: NavHostController,
    showGoBack: Boolean = true,
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var attachedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var titleError by remember { mutableStateOf(false) }
    var descriptionError by remember { mutableStateOf(false) }
    val emailSeparator = stringResource(R.string.help_email_separator)
    val emailDeviceInfo = stringResource(R.string.help_email_device_info)
    val emailModel = stringResource(R.string.help_email_model, Build.MODEL, Build.MANUFACTURER)
    val emailAndroid = stringResource(
        R.string.help_email_android,
        Build.VERSION.RELEASE,
        Build.VERSION.SDK_INT,
    )
    val emailTitle = stringResource(R.string.help_email_title, title)
    val emailDescription = stringResource(R.string.help_email_description)
    val emailSubject = stringResource(R.string.help_email_subject, title)
    val emailChooser = stringResource(R.string.help_email_chooser)
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
    ) { uris ->
        if (uris.isNotEmpty()) attachedUris = (attachedUris + uris).distinct().take(HELP_MAX_ATTACHMENTS)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.help_title),
                showGoBack = showGoBack,
                onNavIconClick = { navController.popBackStack() },
            )
        },
    ) { padding ->
        HelpFeedbackForm(
            modifier = Modifier.padding(padding).fillMaxSize(),
            title = title,
            onTitleChange = { title = it; titleError = false },
            titleError = titleError,
            description = description,
            onDescriptionChange = { description = it; descriptionError = false },
            descriptionError = descriptionError,
            attachedUris = attachedUris,
                maxAttachments = HELP_MAX_ATTACHMENTS,
            onPickAttachments = { pickMediaLauncher.launch("image/*") },
            onRemoveAttachment = { attachedUris = attachedUris - it },
            onSubmit = {
                titleError = title.isBlank()
                descriptionError = description.isBlank()
                if (title.isNotBlank() && description.isNotBlank()) {
                    val deviceInfo = buildString {
                        appendLine(emailSeparator)
                        appendLine(emailDeviceInfo)
                        appendLine(emailModel)
                        appendLine(emailAndroid)
                    }
                    val emailBody = buildString {
                        appendLine(emailTitle)
                        appendLine()
                        appendLine(emailDescription)
                        appendLine(description)
                        appendLine()
                        append(deviceInfo)
                    }
                    val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                        type = "message/rfc822"
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("futuristicken@gmail.com"))
                        putExtra(Intent.EXTRA_SUBJECT, emailSubject)
                        putExtra(Intent.EXTRA_TEXT, emailBody)
                        if (attachedUris.isNotEmpty()) {
                            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(attachedUris))
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                    }
                    context.startActivity(Intent.createChooser(
                        intent,
                        emailChooser,
                    ))
                }
            },
        )
    }
}
