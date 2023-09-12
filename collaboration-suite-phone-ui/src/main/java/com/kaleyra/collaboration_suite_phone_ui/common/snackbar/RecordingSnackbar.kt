package com.kaleyra.collaboration_suite_phone_ui.common.snackbar

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.theme.KaleyraTheme

@Composable
internal fun RecordingStartedSnackbar() {
    val resources = LocalContext.current.resources
    UserMessageInfoSnackbar(
        title = resources.getString(R.string.kaleyra_recording_started),
        subtitle = resources.getString(R.string.kaleyra_recording_started_message)
    )
}

@Composable
internal fun RecordingEndedSnackbar() {
    val resources = LocalContext.current.resources
    UserMessageInfoSnackbar(
        title = resources.getString(R.string.kaleyra_recording_stopped),
        subtitle = resources.getString(R.string.kaleyra_recording_stopped_message)
    )
}

@Composable
internal fun RecordingErrorSnackbar() {
    val resources = LocalContext.current.resources
    UserMessageErrorSnackbar(
        title = resources.getString(R.string.kaleyra_recording_failed),
        subtitle = resources.getString(R.string.kaleyra_recording_failed_message)
    )
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun RecordingStartedSnackbarPreview() {
    KaleyraTheme {
        RecordingStartedSnackbar()
    }
}


@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun RecordingStoppedSnackbarPreview() {
    KaleyraTheme {
        RecordingEndedSnackbar()
    }
}


@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun RecordingErrorSnackbarPreview() {
    KaleyraTheme {
        RecordingErrorSnackbar()
    }
}