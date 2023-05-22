package com.kaleyra.collaboration_suite_phone_ui.call.compose.snackbar

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme

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
internal fun CallParticipantMutedPreview() {
    KaleyraTheme {
        MutedSnackbar()
    }
}