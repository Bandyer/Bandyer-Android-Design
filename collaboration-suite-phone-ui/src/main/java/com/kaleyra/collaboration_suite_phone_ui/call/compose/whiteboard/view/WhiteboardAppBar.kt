package com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.view

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.IconButton
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.appbar.CallAppBar
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme

@Composable
internal fun WhiteboardAppBar(
    onBackPressed: () -> Unit,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CallAppBar(
        onBackPressed = onBackPressed,
        title = stringResource(id = R.string.kaleyra_whiteboard),
        actions = {
            IconButton(
                icon = painterResource(id = R.drawable.ic_kaleyra_image),
                iconDescription = stringResource(id = R.string.kaleyra_upload_file),
                onClick = onUploadClick,
                modifier = Modifier.padding(4.dp)
            )
        },
        modifier = modifier
    )
}

@Preview
@Composable
internal fun WhiteboardAppBarTest() {
    KaleyraTheme {
        WhiteboardAppBar(
            onBackPressed = {},
            onUploadClick = {}
        )
    }
}