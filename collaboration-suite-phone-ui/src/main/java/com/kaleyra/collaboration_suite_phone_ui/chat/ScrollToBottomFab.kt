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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.R

private val ScrollToBottomThreshold = 56.dp

@Composable
internal fun LazyListState.scrollToBottomEnabled(): Boolean {
    val scrollThreshold = with(LocalDensity.current) {
        ScrollToBottomThreshold.toPx()
    }

    return remember {
        derivedStateOf {
            firstVisibleItemIndex != 0 || firstVisibleItemScrollOffset > scrollThreshold
        }
    }.value
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