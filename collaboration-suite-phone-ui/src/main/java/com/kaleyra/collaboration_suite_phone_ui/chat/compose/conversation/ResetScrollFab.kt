@file:OptIn(ExperimentalAnimationApi::class)

package com.kaleyra.collaboration_suite_phone_ui.chat.compose.conversation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.utility.highlightOnFocus

@Composable
internal fun ResetScrollFab(
    counter: Int,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    AnimatedVisibility(
        visible = enabled,
        enter = scaleIn(),
        exit = scaleOut()
    ) {
        FloatingActionButton(
            onClick = onClick,
            backgroundColor = MaterialTheme.colors.primary,
            modifier = Modifier
                .defaultMinSize(32.dp, 32.dp)
                .highlightOnFocus(interactionSource),
            interactionSource = interactionSource
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (counter > 0) {
                    Text(
                        text = "$counter",
                        modifier = Modifier
                            .paddingFromBaseline(bottom = 6.dp)
                            .padding(end = 4.dp)
                    )
                }
                Icon(
                    painter = painterResource(id = R.drawable.ic_kaleyra_double_arrow_down),
                    contentDescription = stringResource(id = R.string.kaleyra_chat_scroll_to_last_message),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Preview
@Composable
internal fun ResetScrollFabPreview() {
    KaleyraTheme {
//        ResetScrollFab(counter = 5, onClick = { }, enabled = true)
    }
}

@Preview
@Composable
internal fun ResetScrollFabDarkPreview() {
    KaleyraTheme(isDarkTheme = true) {
//        ResetScrollFab(counter = 5, onClick = { }, enabled = true)
    }
}