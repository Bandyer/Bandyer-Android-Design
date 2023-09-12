package com.kaleyra.collaboration_suite_phone_ui.call.component.fileshare.view

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.core.view.appbar.CallAppBar
import com.kaleyra.collaboration_suite_phone_ui.theme.KaleyraTheme

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