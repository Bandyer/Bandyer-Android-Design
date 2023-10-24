package com.kaleyra.collaboration_suite_phone_ui.call.ringing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalTextStyle
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.helpertext.HelperText
import com.kaleyra.collaboration_suite_phone_ui.call.*
import com.kaleyra.collaboration_suite_phone_ui.call.precall.PreCallComponent
import com.kaleyra.collaboration_suite_phone_ui.call.recording.model.RecordingTypeUi
import com.kaleyra.collaboration_suite_phone_ui.call.ringing.model.RingingUiState
import com.kaleyra.collaboration_suite_phone_ui.call.ringing.viewmodel.RingingViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.stream.view.core.DefaultStreamAvatarSize
import com.kaleyra.collaboration_suite_phone_ui.common.usermessages.model.UserMessage
import com.kaleyra.collaboration_suite_phone_ui.extensions.TextStyleExtensions.shadow
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.theme.kaleyra_answer_dark_color
import com.kaleyra.collaboration_suite_phone_ui.theme.kaleyra_answer_light_color
import com.kaleyra.collaboration_suite_phone_ui.theme.kaleyra_hang_up_dark_color
import com.kaleyra.collaboration_suite_phone_ui.theme.kaleyra_hang_up_light_color
import com.kaleyra.collaboration_suite_phone_ui.common.button.IconButton
import com.kaleyra.collaboration_suite_phone_ui.call.countdowntimer.rememberCountdownTimerState
import com.kaleyra.collaboration_suite_phone_ui.call.stream.model.streamUiMock
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.collaboration_suite_core_ui.requestConfiguration

const val RingingContentTag = "RingingContentTag"
const val TapToAnswerTimerMillis = 7000L
val WaitingForOtherAvatarPadding = DefaultStreamAvatarSize / 2 + 24.dp

@Composable
internal fun RingingComponent(
    modifier: Modifier = Modifier,
    viewModel: RingingViewModel = viewModel(
        factory = RingingViewModel.provideFactory(::requestConfiguration)
    ),
    isDarkTheme: Boolean = false,
    onBackPressed: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle(null)

    RingingComponent(
        uiState = uiState,
        userMessage = userMessage,
        onBackPressed = onBackPressed,
        onAnswerClick = viewModel::accept,
        onDeclineClick = viewModel::decline,
        isDarkTheme = isDarkTheme,
        modifier = modifier
    )
}

@Composable
internal fun RingingComponent(
    uiState: RingingUiState,
    modifier: Modifier = Modifier,
    userMessage: UserMessage? = null,
    isDarkTheme: Boolean = false,
    tapToAnswerTimerMillis: Long = TapToAnswerTimerMillis,
    onBackPressed: () -> Unit,
    onAnswerClick: () -> Unit,
    onDeclineClick: () -> Unit
) {
    PreCallComponent(
        uiState = uiState,
        userMessage = userMessage,
        subtitle = pluralStringResource(id = R.plurals.kaleyra_call_incoming_status_ringing, count = uiState.participants.count()),
        onBackPressed = onBackPressed,
        modifier = modifier.testTag(RingingContentTag)
    ) {
        if (uiState.amIWaitingOthers) {
            val padding by animateDpAsState(targetValue = if (uiState.video?.view == null || !uiState.video.isEnabled) WaitingForOtherAvatarPadding else 0.dp, label = "waitingOtherPadding")
            Text(
                text = stringResource(id = R.string.kaleyra_waiting_for_other_participants),
                style = LocalTextStyle.current.shadow(),
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset {
                        val offset = padding
                            .toPx()
                            .toInt()
                        IntOffset(0, offset)
                    }
            )
        }
        AnimatedVisibility(
            visible = !uiState.answered,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1f))
                if (uiState.recording != null && uiState.recording != RecordingTypeUi.Never) {
                    HelperText(text = stringResource(id = if (uiState.recording == RecordingTypeUi.OnConnect) R.string.kaleyra_automatic_recording_disclaimer else R.string.kaleyra_manual_recording_disclaimer))
                }
                val countDownTimer by rememberCountdownTimerState(tapToAnswerTimerMillis)
                if (countDownTimer == 0L) {
                    HelperText(text = stringResource(id = R.string.kaleyra_tap_to_answer))
                }
                Spacer(modifier = Modifier.height(24.dp))
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
                        backgroundColor = if (isDarkTheme) kaleyra_hang_up_dark_color else kaleyra_hang_up_light_color,
                        onClick = onDeclineClick
                    )
                    RingingActionButton(
                        painter = painterResource(id = R.drawable.ic_kaleyra_answer),
                        text = stringResource(id = R.string.kaleyra_ringing_answer),
                        backgroundColor = if (isDarkTheme) kaleyra_answer_dark_color else kaleyra_answer_light_color,
                        onClick = onAnswerClick
                    )
                }
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
            RingingUiState(
                video = streamUiMock.video,
                participants = ImmutableList(listOf("user1"))
            ),
            onAnswerClick = { },
            onDeclineClick = { },
            onBackPressed = { }
        )
    }
}