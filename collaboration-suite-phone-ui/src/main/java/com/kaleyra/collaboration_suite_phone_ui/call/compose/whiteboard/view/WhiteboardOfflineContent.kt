package com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.view

import android.content.res.Configuration
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme

@Composable
internal fun WhiteboardOfflineContent(
    loading: Boolean,
    onReloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition()
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(
                animation = tween(500, easing = LinearEasing)
            )
        )
        IconButton(onClick = onReloadClick) {
            Icon(painter = painterResource(id = R.drawable.ic_kaleyra_reload),
                contentDescription = stringResource(id = R.string.kaleyra_error_button_reload),
                tint = Color.Black,
                modifier = Modifier
                    .size(96.dp)
                    .graphicsLayer {
                        rotationZ = if (loading) rotation else 0f
                    })
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(id = R.string.kaleyra_error_title), color = Color.Black
        )
        Text(
            text = stringResource(id = R.string.kaleyra_error_subtitle),
            color = Color.DarkGray,
            fontSize = 12.sp
        )
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun OfflineContentPreview() {
    KaleyraTheme {
        Surface {
            WhiteboardOfflineContent(loading = false, onReloadClick = {})
        }
    }
}