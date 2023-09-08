package com.kaleyra.collaboration_suite_phone_ui.call.screenshare.view

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.call.screenshare.model.ScreenShareTargetUi
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.theme.KaleyraTheme

@Composable
internal fun ScreenShareContent(
    items: ImmutableList<ScreenShareTargetUi>,
    onItemClick: (ScreenShareTargetUi) -> Unit
) {
    LazyColumn {
        items(items = items.value, key = { it.name }) {
            ScreenShareItem(
                screenShareTarget = it,
                modifier = Modifier
                    .clickable(
                        onClickLabel = clickLabelFor(it),
                        role = Role.Button,
                        onClick = { onItemClick(it) }
                    )
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ScreenShareContentPreview() {
    KaleyraTheme {
        Surface {
            ScreenShareContent(
                items = ImmutableList(listOf(ScreenShareTargetUi.Device, ScreenShareTargetUi.Application)),
                onItemClick = { }
            )
        }
    }
}