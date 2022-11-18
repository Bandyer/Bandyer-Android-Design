package com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.SubMenuLayout
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.FileShareState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.TransferUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareContent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareEmptyContent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareFab
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.viewmodel.FileShareViewModel
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle

const val ProgressIndicatorTag = "ProgressIndicatorTag"

@Composable
internal fun FileShareScreen(
    viewModel: FileShareViewModel,
    onFabClick: () -> Unit,
    onItemClick: (TransferUi) -> Unit,
    onItemActionClick: (TransferUi) -> Unit,
    onBackPressed: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FileShareScreen(
        uiState = uiState,
        onFabClick = onFabClick,
        onItemClick = onItemClick,
        onItemActionClick = onItemActionClick,
        onBackPressed = onBackPressed
    )
}

@Composable
internal fun FileShareScreen(
    uiState: FileShareState,
    onFabClick: () -> Unit,
    onItemClick: (TransferUi) -> Unit,
    onItemActionClick: (TransferUi) -> Unit,
    onBackPressed: () -> Unit
) {
    SubMenuLayout(
        title = stringResource(id = R.string.kaleyra_fileshare),
        onCloseClick = onBackPressed
    ) {
        Box(Modifier.fillMaxSize()) {

            if (uiState.transfersList.count() < 1) {
                FileShareEmptyContent()
            } else {
                FileShareContent(
                    items = uiState.transfersList,
                    onItemClick = onItemClick,
                    onItemActionClick = onItemActionClick
                )
            }

            FileShareFab(
                collapsed = uiState.transfersList.count() > 0,
                onClick = onFabClick,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun FileShareScreenPreview() {
    KaleyraTheme {
        Surface {
            FileShareScreen(
                uiState = FileShareState(),
                onFabClick = {},
                onBackPressed = {},
                onItemClick = {},
                onItemActionClick = {}
            )
        }
    }
}
