package com.kaleyra.collaboration_suite_phone_ui.chat

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.R

val LazyListState.scrollTopBottomFabEnabled: Boolean
    get() = firstVisibleItemIndex > 0 && firstVisibleItemScrollOffset > 0

@Preview
@Composable
internal fun ScrollToBottomFabPreview() {
    ScrollToBottomFab(counter = 5, onClick = { })
}

@Composable
internal fun ScrollToBottomFab(counter: Int, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        backgroundColor = MaterialTheme.colors.primary,
        modifier = Modifier.defaultMinSize(32.dp, 32.dp)
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