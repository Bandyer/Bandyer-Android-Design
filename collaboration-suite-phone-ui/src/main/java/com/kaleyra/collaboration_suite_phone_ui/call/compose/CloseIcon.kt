package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.highlightOnFocus
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.supportRtl

@Composable
internal fun CloseIcon(modifier: Modifier = Modifier, onClosePressed: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    IconButton(
        onClick = onClosePressed,
        modifier = modifier.highlightOnFocus(interactionSource),
        interactionSource = interactionSource
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_kaleyra_close),
            contentDescription = stringResource(id = R.string.kaleyra_close),
            modifier = Modifier.supportRtl()
        )
    }
}