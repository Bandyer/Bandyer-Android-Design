package com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_core_ui.requestConfiguration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.NavigationBarsSpacer
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.FileShareUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.SharedFileUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareContent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareEmptyContent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareFab
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.viewmodel.FileShareViewModel
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle

const val ProgressIndicatorTag = "ProgressIndicatorTag"

@Composable
internal fun FileShareComponent(
    viewModel: FileShareViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = FileShareViewModel.provideFactory(::requestConfiguration)
    ),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FileShareComponent(
        uiState = uiState,
        onFabClick = {  },
        onItemClick = {  },
        onItemActionClick = {  },
        modifier = modifier
    )
}

@Composable
internal fun FileShareComponent(
    uiState: FileShareUiState,
    onFabClick: () -> Unit,
    onItemClick: (SharedFileUi) -> Unit,
    onItemActionClick: (SharedFileUi) -> Unit,
    modifier: Modifier = Modifier
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
                onItemActionClick = {}
            )
        }
    }
}
