package com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.view.dialing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.model.PreCallUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.view.PreCallComponent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.viewmodel.PreCallViewModel
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle

const val DialingContentTag = "DialingContentTag"

@Composable
internal fun DialingComponent(
    modifier: Modifier = Modifier,
    viewModel: PreCallViewModel = viewModel(),
    onBackPressed: () -> Unit,
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
    uiState: PreCallUiState,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = { },
) {
    PreCallComponent(
        uiState = uiState,
        subtitle = stringResource(id = R.string.kaleyra_call_status_dialing),
        onBackPressed = onBackPressed,
        modifier = modifier.testTag(DialingContentTag)
    )
}

@Preview
@Composable
fun DialingComponentPreview() {
    KaleyraTheme {
        DialingComponent(
            uiState = PreCallUiState(participants = listOf("user1", "user2")),
            onBackPressed = { }
        )
    }
}