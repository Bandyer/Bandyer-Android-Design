/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.common.snackbar

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.theme.KaleyraTheme

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