package com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.collaboration_suite_phone_ui.call.compose.BottomInsetsSpacer
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallActionsUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.mockCallActions
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.view.CallActionsContent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle

@Composable
internal fun CallActionsSection(
    viewModel: CallActionsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onItemClick: (action: CallAction, toggled: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CallActionsSection(
        uiState = uiState,
        onItemClick = onItemClick,
        modifier = modifier
    )
}

@Composable
internal fun CallActionsSection(
    uiState: CallActionsUiState,
    onItemClick: (action: CallAction, toggled: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        CallActionsContent(
            items = uiState.actionList,
            itemsPerRow = uiState.actionList.count().coerceIn(1, 4),
            onItemClick = onItemClick
        )
        BottomInsetsSpacer()
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun CallActionsSectionPreview() {
    KaleyraTheme {
        Surface {
            CallActionsSection(
                uiState = CallActionsUiState(actionList = mockCallActions),
                onItemClick = { _, _ -> }
            )
        }
    }
}