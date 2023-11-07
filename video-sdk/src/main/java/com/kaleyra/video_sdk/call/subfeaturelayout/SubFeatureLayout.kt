package com.kaleyra.video_sdk.call.subfeaturelayout

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.common.button.IconButton
import com.kaleyra.video_sdk.common.spacer.NavigationBarsSpacer

@Composable
internal fun SubFeatureLayout(
    title: String,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 8.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                icon = painterResource(id = R.drawable.ic_kaleyra_close),
                iconDescription = stringResource(id = R.string.kaleyra_close),
                onClick = onCloseClick
            )
        }
        content()
        NavigationBarsSpacer()
    }
}