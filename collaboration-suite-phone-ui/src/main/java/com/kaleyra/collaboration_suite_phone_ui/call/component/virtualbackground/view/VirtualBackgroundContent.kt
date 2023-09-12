package com.kaleyra.collaboration_suite_phone_ui.call.component.virtualbackground.view

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
import com.kaleyra.collaboration_suite_phone_ui.call.component.virtualbackground.model.VirtualBackgroundUi
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.theme.KaleyraTheme

@Composable
internal fun VirtualBackgroundContent(
    items: ImmutableList<VirtualBackgroundUi>,
    currentBackground: VirtualBackgroundUi,
    onItemClick: (VirtualBackgroundUi) -> Unit
) {
    LazyColumn {
        items(items = items.value.distinctBy { it.id }, key = { it.id }) {
            VirtualBackgroundItem(
                background = it,
                selected = it == currentBackground,
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
            VirtualBackgroundContent(
                items = ImmutableList(listOf(VirtualBackgroundUi.None, VirtualBackgroundUi.Blur(id = "id"), VirtualBackgroundUi.Image(id = "id2"))),
                currentBackground = VirtualBackgroundUi.Blur(id = "id"),
                onItemClick = { }
            )
        }
    }
}