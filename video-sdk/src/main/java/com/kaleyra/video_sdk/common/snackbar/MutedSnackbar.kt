package com.kaleyra.video_sdk.common.snackbar

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
internal fun MutedSnackbar(adminDisplayName: String? = null) {
    val resources = LocalContext.current.resources
    UserMessageInfoSnackbar(
        title = resources.getQuantityString(
            R.plurals.kaleyra_call_participant_muted_by_admin,
            if (adminDisplayName.isNullOrBlank()) 0 else 1,
            adminDisplayName
        )
    )
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun MutedSnackbarPreview() {
    KaleyraTheme {
        MutedSnackbar()
    }
}