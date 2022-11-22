package com.kaleyra.collaboration_suite_phone_ui.call.compose.callaction

import android.content.res.Configuration
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callaction.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callaction.model.CallActionsUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callaction.model.mockCallActions
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callaction.view.CallActionsContent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callaction.viewmodel.CallActionsViewModel
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle

@Composable
internal fun CallActionsScreen(
    viewModel: CallActionsViewModel,
    onItemClick: (CallAction) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CallActionsScreen(
        uiState = uiState,
        onItemClick = onItemClick
    )
}

@Composable
internal fun CallActionsScreen(
    uiState: CallActionsUiState,
    onItemClick: (CallAction) -> Unit
) {
    CallActionsContent(
        items = uiState.actionList,
        itemsPerRow = uiState.actionList.count().coerceIn(1, 4),
        onItemClick = onItemClick
    )
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun CallActionsScreenPreview() {
    KaleyraTheme {
        Surface {
            CallActionsScreen(
                uiState = CallActionsUiState(actionList = mockCallActions),
                onItemClick = {}
            )
        }
    }
}