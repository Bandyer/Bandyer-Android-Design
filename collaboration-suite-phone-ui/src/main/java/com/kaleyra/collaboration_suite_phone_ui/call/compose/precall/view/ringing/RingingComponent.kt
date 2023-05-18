package com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.view.ringing

import android.graphics.Rect
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaleyra.collaboration_suite_core_ui.requestConfiguration
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.model.PreCallUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.view.PreCallComponent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.view.common.HelperText
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.viewmodel.PreCallViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.recording.model.RecordingTypeUi
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle

const val RingingContentTag = "RingingContentTag"
const val TapToAnswerTimerMillis = 7000L

@Composable
internal fun RingingComponent(
    modifier: Modifier = Modifier,
    viewModel: PreCallViewModel = viewModel(
        factory = PreCallViewModel.provideFactory(::requestConfiguration)
    ),
    onBackPressed: () -> Unit,
    onStreamViewPositioned: (Rect) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RingingComponent(
        uiState = uiState,
        onBackPressed = onBackPressed,
        onAnswerClick = viewModel::answer,
        onDeclineClick = viewModel::decline,
        onStreamViewPositioned = onStreamViewPositioned,
        modifier = modifier
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun RingingComponent(
    uiState: PreCallUiState,
    modifier: Modifier = Modifier,
    tapToAnswerTimerMillis: Long = TapToAnswerTimerMillis,
    onBackPressed: () -> Unit,
    onAnswerClick: () -> Unit,
    onDeclineClick: () -> Unit,
    onStreamViewPositioned: (Rect) -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()

    PreCallComponent(
        uiState = uiState,
        subtitle = pluralStringResource(id = R.plurals.kaleyra_call_incoming_status_ringing, count = uiState.participants.size),
        onBackPressed = onBackPressed,
        onStreamViewPositioned = onStreamViewPositioned,
        modifier = modifier.testTag(RingingContentTag)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            if (uiState.recording != null) {
                HelperText(text = stringResource(id = if (uiState.recording == RecordingTypeUi.OnConnect) R.string.kaleyra_automatic_recording_disclaimer else R.string.kaleyra_manual_recording_disclaimer))
            }
            val countDownTimer by rememberCountdownTimerState(tapToAnswerTimerMillis)
            if (countDownTimer == 0L) {
                HelperText(text = stringResource(id = R.string.kaleyra_tap_to_answer))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp)
            ) {
                RingingActionButton(
                    painter = painterResource(id = R.drawable.ic_kaleyra_decline),
                    text = stringResource(id = R.string.kaleyra_ringing_decline),
                    backgroundColor = colorResource(id = if (isDarkTheme) R.color.kaleyra_color_hang_up_button_night else R.color.kaleyra_color_hang_up_button),
                    onClick = onDeclineClick
                )
                RingingActionButton(
                    painter = painterResource(id = R.drawable.ic_kaleyra_answer),
                    text = stringResource(id = R.string.kaleyra_ringing_answer),
                    backgroundColor = colorResource(id = if (isDarkTheme) R.color.kaleyra_color_answer_button_night else R.color.kaleyra_color_answer_button),
                    onClick = onAnswerClick
                )
            }
        }
    }
}

@Composable
private fun RingingActionButton(
    painter: Painter,
    text: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            icon = painter,
            iconDescription = text,
            iconSize = 48.dp,
            onClick = onClick,
            indication = rememberRipple(bounded = false, radius = 40.dp),
            modifier = Modifier
                .background(
                    color = backgroundColor,
                    shape = CircleShape
                )
                .size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = text,
            fontSize = 12.sp
        )
    }
}

@Preview
@Composable
internal fun RingingComponentPreview() {
    KaleyraTheme {
        RingingComponent(
            PreCallUiState(
                stream = streamUiMock,
                participants = listOf("user1")
            ),
            onAnswerClick = { },
            onDeclineClick = { },
            onBackPressed = { },
            onStreamViewPositioned = { }
        )
    }
}