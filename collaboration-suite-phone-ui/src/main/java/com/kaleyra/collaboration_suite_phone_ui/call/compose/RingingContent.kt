package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.CallInfoUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.callInfoMock
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme

const val RingingContentTag = "RingingContentTag"
const val TapToAnswerTimerMillis = 7000L

@Composable
internal fun RingingContent(
    stream: StreamUi? = null,
    callInfo: CallInfoUi,
    groupCall: Boolean = false,
    tapToAnswerTimerMillis: Long = TapToAnswerTimerMillis,
    onBackPressed: () -> Unit = { },
    onAnswerClick: () -> Unit = { },
    onDeclineClick: () -> Unit = { },
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    PreCallContent(
        stream = stream,
        callInfo = callInfo,
        groupCall = groupCall,
        onBackPressed = onBackPressed,
        modifier = modifier.testTag(RingingContentTag)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            if (callInfo.recording != null) {
                HelperText(text = stringResource(id = if (callInfo.recording == Recording.AUTOMATIC) R.string.kaleyra_automatic_recording_disclaimer else R.string.kaleyra_manual_recording_disclaimer))
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

@Composable
private fun HelperText(text: String) = Text(text = text, fontSize = 12.sp, fontStyle = FontStyle.Italic)

@Preview
@Composable
internal fun RingingActionButtonPreview() {
    KaleyraTheme {
        RingingContent(streamUiMock, callInfoMock)
    }
}