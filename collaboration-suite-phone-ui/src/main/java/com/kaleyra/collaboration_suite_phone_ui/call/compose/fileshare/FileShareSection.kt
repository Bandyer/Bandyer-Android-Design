package com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.subfeaturelayout.SubFeatureLayout
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.FileShareUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.TransferUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareContent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareEmptyContent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareFab
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.viewmodel.FileShareViewModel
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle

const val ProgressIndicatorTag = "ProgressIndicatorTag"

@Composable
internal fun FileShareSection(
    viewModel: FileShareViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onFabClick: () -> Unit,
    onItemClick: (TransferUi) -> Unit,
    onItemActionClick: (TransferUi) -> Unit,
    onBackPressed: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FileShareSection(
        uiState = uiState,
        onFabClick = onFabClick,
        onItemClick = onItemClick,
        onItemActionClick = onItemActionClick,
        onBackPressed = onBackPressed
    )
}

@Composable
internal fun FileShareSection(
    uiState: FileShareUiState,
    onFabClick: () -> Unit,
    onItemClick: (TransferUi) -> Unit,
    onItemActionClick: (TransferUi) -> Unit,
    onBackPressed: () -> Unit
) {
    SubFeatureLayout(
        title = stringResource(id = R.string.kaleyra_fileshare),
        onCloseClick = onBackPressed
    ) {
        Box(Modifier.fillMaxSize()) {

            if (uiState.transferList.count() < 1) {
                FileShareEmptyContent()
            } else {
                FileShareContent(
                    items = uiState.transferList,
                    onItemClick = onItemClick,
                    onItemActionClick = onItemActionClick
                )
            }

            FileShareFab(
                collapsed = uiState.transferList.count() > 0,
                onClick = onFabClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            )
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun FileShareSectionPreview() {
    KaleyraTheme {
        Surface {
            FileShareSection(
                uiState = FileShareUiState(),
                onFabClick = {},
                onBackPressed = {},
                onItemClick = {},
                onItemActionClick = {}
            )
        }
    }
}