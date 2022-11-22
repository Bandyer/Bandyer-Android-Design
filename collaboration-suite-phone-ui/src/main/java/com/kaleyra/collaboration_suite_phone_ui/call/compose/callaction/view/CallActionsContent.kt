package com.kaleyra.collaboration_suite_phone_ui.call.compose.callaction.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callaction.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callaction.model.mockCallActions
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.fadeBelowOfRootBottomBound

@Composable
internal fun CallActionsContent(
    items: ImmutableList<CallAction>,
    itemsPerRow: Int,
    onItemClick: (CallAction) -> Unit,
    enableBottomFade: Boolean = true
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(count = itemsPerRow)
    ) {
        items(items = items.value) { action ->
            var toggled by remember { mutableStateOf((action as? CallAction.Toggleable)?.isToggled ?: false) }
            CallAction(
                action = action,
                enabled = action.isEnabled,
                toggled = toggled,
                onToggle = {
                    onItemClick(action)
                    toggled = action is CallAction.Toggleable && it
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 20.dp, bottom = 8.dp)
                    .then(if (enableBottomFade) Modifier.fadeBelowOfRootBottomBound() else Modifier)
            )
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun CallActionsContentPreview() {
    KaleyraTheme {
        Surface {
            CallActionsContent(
                items = mockCallActions,
                itemsPerRow = 4,
                onItemClick = {},
                enableBottomFade = false
            )
        }
    }
}