package com.kaleyra.collaboration_suite_phone_ui.call.whiteboard.view

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
import com.kaleyra.collaboration_suite.whiteboard.WhiteboardView
import com.kaleyra.collaboration_suite_core_ui.requestConfiguration
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.IconButton
import com.kaleyra.collaboration_suite_phone_ui.call.core.view.appbar.CallAppBar
import com.kaleyra.collaboration_suite_phone_ui.call.whiteboard.viewmodel.WhiteboardViewModel
import com.kaleyra.collaboration_suite_core_ui.theme.KaleyraTheme

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