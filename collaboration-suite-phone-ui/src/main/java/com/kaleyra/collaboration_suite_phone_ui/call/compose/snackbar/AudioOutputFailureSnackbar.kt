package com.kaleyra.collaboration_suite_phone_ui.call.compose.snackbar

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme

@Composable
internal fun AudioOutputFailureSnackbar() {
    val resources = LocalContext.current.resources
    UserMessageInfoSnackbar(title = resources.getString(R.string.kaleyra_already_in_system_call_routing_exception))
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun AudioOutputFailureSnackbarPreview() {
    KaleyraTheme {
        AudioOutputFailureSnackbar()
    }
}