package com.kaleyra.video_sdk.common.button

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import com.kaleyra.video_sdk.R

@Composable
internal fun BackIconButton(onClick: () -> Unit) {
    IconButton(
        icon = rememberVectorPainter(image = Icons.Filled.ArrowBack),
        iconDescription = stringResource(id = R.string.kaleyra_back),
        onClick = onClick,
    )
}