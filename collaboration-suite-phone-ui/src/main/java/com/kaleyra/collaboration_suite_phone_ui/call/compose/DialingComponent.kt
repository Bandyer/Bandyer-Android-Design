package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.kaleyra.collaboration_suite_phone_ui.R

const val DialingContentTag = "DialingContentTag"

@Composable
internal fun DialingComponent(
    stream: StreamUi? = null,
    groupCall: Boolean = false,
    onBackPressed: () -> Unit = { },
    modifier: Modifier = Modifier
) {
    PreCallComponent(
        stream = stream,
        title = "",
        subtitle = stringResource(id = R.string.kaleyra_call_status_dialing),
        watermarkInfo = null,
        groupCall = groupCall,
        onBackPressed = onBackPressed,
        modifier = modifier.testTag(DialingContentTag),
        content = { }
    )
}