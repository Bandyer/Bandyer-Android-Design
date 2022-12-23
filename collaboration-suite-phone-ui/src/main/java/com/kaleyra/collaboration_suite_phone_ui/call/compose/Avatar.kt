package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage

// TODO move common package between chat and call
@Composable
internal fun Avatar(
    uri: ImmutableUri?,
    contentDescription: String,
    @DrawableRes placeholder: Int,
    @DrawableRes error: Int,
    contentColor: Color,
    backgroundColor: Color,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val placeholderFilter = ColorFilter.tint(color = contentColor)
    var colorFilter by remember { mutableStateOf<ColorFilter?>(placeholderFilter) }
    AsyncImage(
        model = uri?.value,
        contentDescription = contentDescription,
        modifier = modifier
            .clip(CircleShape)
            .background(color = backgroundColor)
            .size(size),
        placeholder = painterResource(id = placeholder),
        error = painterResource(id = error),
        contentScale = ContentScale.Crop,
        onSuccess = { colorFilter = null },
        colorFilter = colorFilter
    )
}