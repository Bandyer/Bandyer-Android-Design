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

package com.kaleyra.video_sdk.call.kicked

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kaleyra.video_sdk.theme.KaleyraTheme
import com.kaleyra.video_sdk.R
import kotlinx.coroutines.delay

private const val AutoDismissMs = 3000L

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun KickedMessageDialog(adminName: String = "", onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {

        LaunchedEffect(Unit) {
            delay(AutoDismissMs)
            onDismiss()
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .wrapContentSize()
                .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = null,
                    onClickLabel = stringResource(id = R.string.kaleyra_action_dismiss),
                    role = Role.Button,
                    onClick = onDismiss
                )
        ) {
            Text(
                text = pluralStringResource(id = R.plurals.kaleyra_call_participant_removed, count = if (adminName.isNotBlank()) 1 else 0, adminName),
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
internal fun KickedMessagePreview() = KaleyraTheme {
    KickedMessageDialog("admin", {})
}