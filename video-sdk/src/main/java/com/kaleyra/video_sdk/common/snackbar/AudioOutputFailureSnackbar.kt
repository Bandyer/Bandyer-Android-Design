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