package com.kaleyra.video_sdk.call.fileshare.view

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.appbar.CallAppBar
import com.kaleyra.video_sdk.theme.KaleyraTheme
import com.kaleyra.video_sdk.R

@Composable
internal fun FileShareAppBar(onBackPressed: () -> Unit, modifier: Modifier = Modifier) {
    CallAppBar(
        onBackPressed = onBackPressed,
        title = stringResource(id = R.string.kaleyra_fileshare),
        actions = { Spacer(Modifier.width(56.dp)) },
        modifier = modifier
    )
}

@Preview
@Composable
internal fun FileShareAppBarTest() {
    KaleyraTheme {
        FileShareAppBar(onBackPressed = { })
    }
}