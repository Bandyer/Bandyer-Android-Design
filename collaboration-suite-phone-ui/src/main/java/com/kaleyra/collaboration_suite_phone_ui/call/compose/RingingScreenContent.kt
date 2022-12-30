package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.CallInfoUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.callInfoMock
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme

@Composable
internal fun RingingScreenContent(
    stream: StreamUi,
    callInfo: CallInfoUi,
    groupCall: Boolean = false,
    onBackPressed: () -> Unit = { },
    onAnswer: () -> Unit = { },
    onDecline: () -> Unit = { },
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    PreCallContent(
        stream = stream,
        callInfo = callInfo,
        groupCall = groupCall,
        onBackPressed = onBackPressed,
        modifier = modifier
    ) {
        // TODO add helper text
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 32.dp)
                .align(Alignment.BottomCenter)
        ) {
            RingingActionButton(
                painter = painterResource(id = R.drawable.ic_kaleyra_decline),
                text = stringResource(id = R.string.kaleyra_ringing_decline),
                backgroundColor = colorResource(id = if (isDarkTheme) R.color.kaleyra_color_hang_up_button_night else R.color.kaleyra_color_hang_up_button),
                onClick = onDecline
            )
            RingingActionButton(
                painter = painterResource(id = R.drawable.ic_kaleyra_answer),
                text = stringResource(id = R.string.kaleyra_ringing_answer),
                backgroundColor = colorResource(id = if (isDarkTheme) R.color.kaleyra_color_answer_button_night else R.color.kaleyra_color_answer_button),
                onClick = onAnswer
            )
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
            iconDescription = null,
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
internal fun RingingActionButtonPreview() {
    KaleyraTheme {
        RingingScreenContent(streamUiMock, callInfoMock)
    }
}