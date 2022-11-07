package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme

@Composable
internal fun ScreenShare(
    items: ImmutableList<ScreenShare>,
    onItemClick: (ScreenShare) -> Unit,
    onBackPressed: () -> Unit
) {
    Column(modifier = Modifier.padding(top = 12.dp, bottom = 24.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.kaleyra_screenshare_picker_title),
                color = MaterialTheme.colors.onSurface,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            NavigationIcon(onBackPressed = onBackPressed)
        }
        LazyColumn {
            items(items = items.value) {
                val title = titleFor(it)
                ScreenShareItem(
                    title = title,
                    icon = painterFor(it),
                    modifier = Modifier
                        .clickable(
                            onClickLabel = title,
                            role = Role.Button,
                            onClick = { onItemClick(it) }
                        )
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }
        }
    }

}

@Composable
private fun titleFor(screenShare: ScreenShare): String =
    when (screenShare) {
        ScreenShare.DEVICE -> stringResource(R.string.kaleyra_screenshare_full_device)
        ScreenShare.APPLICATION -> stringResource(R.string.kaleyra_screenshare_app_only)
    }

@Composable
private fun painterFor(screenShare: ScreenShare): Painter = painterResource(
    id = when (screenShare) {
        ScreenShare.DEVICE -> R.drawable.ic_kaleyra_screen_share_device
        ScreenShare.APPLICATION -> R.drawable.ic_kaleyra_screen_share_app
    }
)

@Composable
internal fun ScreenShareItem(
    title: String,
    icon: Painter,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = icon,
            contentDescription = null,
            colorFilter = ColorFilter.tint(color = MaterialTheme.colors.onSurface)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            color = MaterialTheme.colors.onSurface
        )
    }
}

@Preview
@Composable
internal fun ScreenSharePreview() {
    KaleyraTheme {
        ScreenShare(
            items = ImmutableList(listOf(ScreenShare.DEVICE, ScreenShare.APPLICATION)),
            onItemClick = { },
            onBackPressed = { }
        )
    }
}