package com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CollapseIcon
import com.kaleyra.collaboration_suite_phone_ui.R

@Composable
fun FileShareAppBar(
    backgroundColor: Color,
    elevation: Dp,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        elevation = elevation,
        backgroundColor = backgroundColor,
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                CollapseIcon(onClick = onBackPressed, modifier = Modifier.padding(4.dp))
                Text(
                    text = stringResource(id = R.string.kaleyra_fileshare),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}