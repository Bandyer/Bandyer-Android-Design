package com.kaleyra.collaboration_suite_phone_ui.call.compose.snackbar

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_core_ui.theme.KaleyraTheme

@Composable
internal fun UsbConnectedSnackbar(name: String) {
    val resources = LocalContext.current.resources
    UserMessageInfoSnackbar(
        title = if (name.isBlank()) resources.getString(R.string.kaleyra_generic_external_camera_connected) else
            resources.getString(R.string.kaleyra_external_camera_connected, name)
    )
}

@Composable
internal fun UsbDisconnectedSnackbar() {
    val resources = LocalContext.current.resources
    UserMessageInfoSnackbar(title = resources.getString(R.string.kaleyra_external_camera_disconnected))
}

@Composable
internal fun UsbNotSupportedSnackbar() {
    val resources = LocalContext.current.resources
    UserMessageInfoSnackbar(title = resources.getString(R.string.kaleyra_external_camera_unsupported))
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun UsbConnectedSnackbarPreview() {
    KaleyraTheme {
        UsbConnectedSnackbar("name")
    }
}


@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun UsbDisconnectedSnackbarPreview() {
    KaleyraTheme {
        UsbDisconnectedSnackbar()
    }
}


@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun UsbNotSupportedSnackbarPreview() {
    KaleyraTheme {
        UsbNotSupportedSnackbar()
    }
}