package com.kaleyra.collaboration_suite_phone_ui.call.compose.snackbar

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_core_ui.theme.KaleyraTheme

@Composable
internal fun AudioOutputGenericFailureSnackbar() {
    val resources = LocalContext.current.resources
    UserMessageInfoSnackbar(title = resources.getString(R.string.kaleyra_generic_audio_routing_error))
}

@Composable
internal fun AudioOutputInSystemCallFailureSnackbar() {
    val resources = LocalContext.current.resources
    UserMessageInfoSnackbar(title = resources.getString(R.string.kaleyra_already_in_system_call_audio_routing_error))
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun AudioOutputGenericFailureSnackbarPreview() {
    KaleyraTheme {
        AudioOutputGenericFailureSnackbar()
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun AudioOutputInSystemCallFailureSnackbarPreview() {
    KaleyraTheme {
        AudioOutputInSystemCallFailureSnackbar()
    }
}