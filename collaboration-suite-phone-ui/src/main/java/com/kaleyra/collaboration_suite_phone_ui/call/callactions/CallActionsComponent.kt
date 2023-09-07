package com.kaleyra.collaboration_suite_phone_ui.call.callactions

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.collaboration_suite_core_ui.requestConfiguration
import com.kaleyra.collaboration_suite_phone_ui.call.NavigationBarsSpacer
import com.kaleyra.collaboration_suite_phone_ui.call.callactions.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.callactions.model.CallActionsUiState
import com.kaleyra.collaboration_suite_phone_ui.call.callactions.model.mockCallActions
import com.kaleyra.collaboration_suite_phone_ui.call.callactions.view.CallActionsContent
import com.kaleyra.collaboration_suite_phone_ui.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.permission.findActivity
import com.kaleyra.collaboration_suite_core_ui.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle

@Composable
internal fun CallActionsComponent(
    viewModel: CallActionsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = CallActionsViewModel.provideFactory(::requestConfiguration)
    ),
    onItemClick: (action: CallAction) -> Unit,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current.findActivity()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CallActionsComponent(
        uiState = uiState,
        isDarkTheme = isDarkTheme,
        onItemClick = { action ->
            // TODO revise this
            when (action) {
                is CallAction.Microphone -> viewModel.toggleMic(activity)
                is CallAction.Camera -> viewModel.toggleCamera(activity)
                is CallAction.SwitchCamera -> viewModel.switchCamera()
                is CallAction.HangUp -> viewModel.hangUp()
                is CallAction.ScreenShare -> {
                    if (!viewModel.tryStopScreenShare()) {
                        onItemClick(action)
                    }
                }
                is CallAction.Chat -> viewModel.showChat(activity.baseContext)
                else -> onItemClick(action)
            }
        },
        modifier = modifier
    )
}

@Composable
internal fun CallActionsComponent(
    uiState: CallActionsUiState,
    onItemClick: (action: CallAction) -> Unit,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        CallActionsContent(
            items = uiState.actionList,
            itemsPerRow = uiState.actionList.count().coerceIn(1, 4),
            isDarkTheme = isDarkTheme,
            onItemClick = onItemClick
        )
        NavigationBarsSpacer()
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun CallActionsComponentPreview() {
    KaleyraTheme {
        Surface {
            CallActionsComponent(
                uiState = CallActionsUiState(actionList = mockCallActions),
                onItemClick = { }
            )
        }
    }
}