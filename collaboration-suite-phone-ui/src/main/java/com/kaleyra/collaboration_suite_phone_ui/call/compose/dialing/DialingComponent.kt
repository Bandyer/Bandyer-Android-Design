package com.kaleyra.collaboration_suite_phone_ui.call.compose.dialing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaleyra.collaboration_suite_core_ui.requestConfiguration
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.dialing.view.DialingUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.dialing.viewmodel.DialingViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.PreCallComponent
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle

const val DialingContentTag = "DialingContentTag"

@Composable
internal fun DialingComponent(
    modifier: Modifier = Modifier,
    viewModel: DialingViewModel = viewModel(
        factory = DialingViewModel.provideFactory(::requestConfiguration)
    ),
    onBackPressed: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DialingComponent(
        uiState = uiState,
        onBackPressed = onBackPressed,
        modifier = modifier
    )
}

@Composable
internal fun DialingComponent(
    uiState: DialingUiState,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = { }
) {
    PreCallComponent(
        uiState = uiState,
        subtitle = stringResource(id = R.string.kaleyra_call_status_ringing),
        onBackPressed = onBackPressed,
        modifier = modifier.testTag(DialingContentTag)
    )
}

@Preview
@Composable
fun DialingComponentPreview() {
    KaleyraTheme {
        DialingComponent(
            uiState = DialingUiState(participants = ImmutableList(listOf("user1", "user2"))),
            onBackPressed = { }
        )
    }
}