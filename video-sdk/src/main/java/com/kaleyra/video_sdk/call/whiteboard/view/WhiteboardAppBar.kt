package com.kaleyra.video_sdk.call.whiteboard.view

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video.whiteboard.WhiteboardView
import com.kaleyra.video_common_ui.requestConfiguration
import com.kaleyra.video_sdk.common.button.IconButton
import com.kaleyra.video_sdk.call.appbar.CallAppBar
import com.kaleyra.video_sdk.call.whiteboard.viewmodel.WhiteboardViewModel
import com.kaleyra.video_sdk.theme.KaleyraTheme
import com.kaleyra.video_sdk.R

@Composable
internal fun WhiteboardAppBar(
    viewModel: WhiteboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = WhiteboardViewModel.provideFactory(::requestConfiguration, WhiteboardView(LocalContext.current))
    ),
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.uploadMediaFile(uri)
    }

    WhiteboardAppBar(
        onBackPressed = onBackPressed,
        onUploadClick = { launcher.launch("image/*") },
        modifier = modifier
    )
}

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