package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.CallInfoUi

@Composable
internal fun DialingContent(
    stream: StreamUi? = null,
    callInfo: CallInfoUi,
    groupCall: Boolean = false,
    onBackPressed: () -> Unit = { },
    modifier: Modifier = Modifier
) {
    PreCallContent(
        stream = stream,
        callInfo = callInfo,
        groupCall = groupCall,
        onBackPressed = onBackPressed,
        modifier = modifier,
        content = { }
    )
}