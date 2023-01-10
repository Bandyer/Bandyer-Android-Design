package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.CallInfoUi

const val DialingContentTag = "DialingContentTag"

@Composable
internal fun DialingContent(
    stream: StreamUi? = null,
    callInfo: CallInfoUi,
    groupCall: Boolean = false,
    onBackPressed: () -> Unit = { },
    modifier: Modifier = Modifier
) {
    PreCallComponent(
        stream = stream,
        callInfo = callInfo,
        groupCall = groupCall,
        onBackPressed = onBackPressed,
        modifier = modifier.testTag(DialingContentTag),
        content = { }
    )
}