package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage

// TODO move common package between chat and call
@Composable
internal fun Avatar(
    uri: Uri?,
    contentDescription: String,
    placeholder: Painter,
    error: Painter,
    contentColor: Color,
    backgroundColor: Color,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val placeholderFilter = ColorFilter.tint(color = contentColor)
    var colorFilter by remember { mutableStateOf<ColorFilter?>(placeholderFilter) }
    AsyncImage(
        model = uri,
        contentDescription = contentDescription,
        modifier = modifier
            .clip(CircleShape)
            .background(color = backgroundColor)
            .size(size),
        placeholder = placeholder,
        error = error,
        contentScale = ContentScale.Crop,
        onSuccess = { colorFilter = null },
        colorFilter = colorFilter
    )
}