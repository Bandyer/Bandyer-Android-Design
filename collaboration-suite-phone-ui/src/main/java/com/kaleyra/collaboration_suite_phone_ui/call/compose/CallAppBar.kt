package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.topappbar.TopAppBar

@Composable
internal fun FileShareAppBar(onBackPressed: () -> Unit) {
    CallAppBar(
        onBackPressed = onBackPressed,
        title = stringResource(id = R.string.kaleyra_fileshare),
        actions = { Spacer(Modifier.width(56.dp)) }
    )
}

@Preview
@Composable
internal fun FileShareAppBarTest() {
    KaleyraTheme {
        FileShareAppBar(onBackPressed = { })
    }
}

@Composable
internal fun WhiteboardAppBar(
    onBackPressed: () -> Unit,
    onUploadClick: () -> Unit
) {
    CallAppBar(
        onBackPressed = onBackPressed,
        title = stringResource(id = R.string.kaleyra_whiteboard),
        actions = {
            IconButton(
                icon = painterResource(id = R.drawable.ic_kaleyra_image),
                iconDescription = stringResource(id = R.string.kaleyra_upload_file),
                onClick = onUploadClick,
                modifier = Modifier.padding(4.dp)
            )
        }
    )
}

@Preview
@Composable
internal fun WhiteboardAppBarTest() {
    KaleyraTheme {
        WhiteboardAppBar(
            onBackPressed = {},
            onUploadClick = {}
        )
    }
}

@Composable
internal fun CallAppBar(
    title: String,
    onBackPressed: () -> Unit,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    TopAppBar(
        navigationIcon = {
            IconButton(
                icon = painterResource(id = R.drawable.ic_kaleyra_back_down),
                iconDescription = stringResource(id = R.string.kaleyra_close),
                onClick = onBackPressed,
                modifier = Modifier.padding(4.dp)
            )
        },
        content = {
            Text(
                text = title,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )
        },
        actions = {
            if (actions != null) {
                actions()
            }
        },
        elevation = 0.dp
    )
}