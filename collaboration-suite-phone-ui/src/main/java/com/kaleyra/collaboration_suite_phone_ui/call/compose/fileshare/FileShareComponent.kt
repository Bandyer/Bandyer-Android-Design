package com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_core_ui.requestConfiguration
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.doesFileExists
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.NavigationBarsSpacer
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.FileShareUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.SharedFileUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareContent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareEmptyContent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareFab
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.viewmodel.FileShareViewModel
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle
import com.kaleyra.collaboration_suite_phone_ui.extensions.getMimeType

const val ProgressIndicatorTag = "ProgressIndicatorTag"

@Composable
internal fun FileShareComponent(
    viewModel: FileShareViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = FileShareViewModel.provideFactory(::requestConfiguration)
    ),
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val (showUnableToOpenFileSnackBar, setShowUnableToOpenSnackBar) = remember { mutableStateOf(false) }
    val (showCancelledFileSnackBar, setShowCancelledFileSnackBar) = remember { mutableStateOf(false) }
    val snackBarHostState = remember { SnackbarHostState() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val unableToOpenFileText = stringResource(id = R.string.kaleyra_fileshare_impossible_open_file)
    val fileCancelledText = stringResource(id = R.string.kaleyra_fileshare_file_cancelled)

    if (showUnableToOpenFileSnackBar) {
        LaunchedEffect(unableToOpenFileText, snackBarHostState) {
            snackBarHostState.showSnackbar(message = unableToOpenFileText)
            setShowUnableToOpenSnackBar(false)
        }
    }

    if (showCancelledFileSnackBar) {
        LaunchedEffect(fileCancelledText, snackBarHostState) {
            snackBarHostState.showSnackbar(message = fileCancelledText)
            setShowCancelledFileSnackBar(false)
        }
    }

    val onItemClick = remember {
        { sharedFile: SharedFileUi ->
            onItemClick(
                context = context,
                sharedFile = sharedFile,
                onFileDoesNotExist = { setShowCancelledFileSnackBar(true) },
                onUnableToOpenFile = { setShowUnableToOpenSnackBar(true) }
            )
        }
    }

    FileShareComponent(
        uiState = uiState,
        onFabClick = {  },
        onItemClick = onItemClick,
        onItemActionClick = {
            if (it.state is SharedFileUi.State.Success) onItemClick(it)
            else viewModel.onActionClick(it)
        },
        modifier = modifier,
        snackBarHostState = snackBarHostState
    )
}

private fun onItemClick(
    context: Context,
    sharedFile: SharedFileUi,
    onFileDoesNotExist: () -> Unit,
    onUnableToOpenFile: () -> Unit
) {
    if (sharedFile.state !is SharedFileUi.State.Success) return
    val uri = if (sharedFile.isMine) sharedFile.uri.value else sharedFile.state.uri.value
    if (context.doesFileExists(uri)) {
        tryToOpenFile(context = context, uri = uri, onFailure = onUnableToOpenFile)
    } else {
        onFileDoesNotExist.invoke()
    }
}

private fun tryToOpenFile(context: Context, uri: Uri, onFailure: () -> Unit) {
    runCatching {
        val mimeType = uri.getMimeType(context)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        context.startActivity(intent)
    }.onFailure {
        onFailure.invoke()
    }
}

@Composable
internal fun FileShareComponent(
    uiState: FileShareUiState,
    onFabClick: () -> Unit,
    onItemClick: (SharedFileUi) -> Unit,
    onItemActionClick: (SharedFileUi) -> Unit,
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState
) {
    Column(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxSize()
    ) {
        Box(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        ) {
            if (uiState.sharedFiles.count() < 1) {
                FileShareEmptyContent(modifier = Modifier.matchParentSize())
            } else {
                FileShareContent(
                    items = uiState.sharedFiles,
                    onItemClick = onItemClick,
                    onItemActionClick = onItemActionClick,
                    modifier = Modifier.matchParentSize()
                )
            }

            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 72.dp)
            )

            FileShareFab(
                collapsed = uiState.sharedFiles.count() > 0,
                onClick = onFabClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
        NavigationBarsSpacer()
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun FileShareComponentPreview() {
    KaleyraTheme {
        Surface {
            FileShareComponent(
                uiState = FileShareUiState(),
                onFabClick = {},
                onItemClick = {},
                onItemActionClick = {},
                snackBarHostState = SnackbarHostState()
            )
        }
    }
}
