package com.kaleyra.collaboration_suite_phone_ui.call.streams

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_core_ui.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.pulse

internal const val RecordingDotTestTag = "RecordingDotTestTag"

@Composable
internal fun RecordingLabel(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(
                color = colorResource(id = R.color.kaleyra_recording_background_color),
                shape = RoundedCornerShape(5.dp)
            )
            .padding(top = 4.dp, bottom = 4.dp, start = 4.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RecordingDot()
        Text(
            text = stringResource(id = R.string.kaleyra_call_info_rec).uppercase(),
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}

@Composable
internal fun RecordingDot(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(id = R.drawable.ic_kaleyra_recording_dot),
        contentDescription = null,
        tint = colorResource(id = R.color.kaleyra_recording_color),
        modifier = modifier
            .size(20.dp)
            .pulse()
            .testTag(RecordingDotTestTag)
    )
}

@Preview
@Composable
internal fun RecordingLabelPreview() {
    KaleyraTheme {
        RecordingLabel()
    }
}