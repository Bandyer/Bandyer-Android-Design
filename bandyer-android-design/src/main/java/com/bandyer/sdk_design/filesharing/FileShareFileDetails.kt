package com.bandyer.sdk_design.filesharing

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bandyer.sdk_design.R

@Composable
fun MiscFile(modifier: Modifier = Modifier, text: String) {
    FileDetails(
        modifier = modifier,
        iconBackgroundColor = LocalContentColor.current.copy(alpha = 0.2f),
        iconDrawable = R.drawable.ic_file,
        iconTint = MaterialTheme.colors.background,
        iconContentDescription = stringResource(id = R.string.bandyer_fileshare_media),
        text = text
    )
}

@Composable
fun ArchiveFile(modifier: Modifier = Modifier, text: String) {
    FileDetails(
        modifier = modifier,
        iconBackgroundColor = LocalContentColor.current.copy(alpha = 0.2f),
        iconDrawable = R.drawable.ic_archive,
        iconTint = LocalContentColor.current,
        iconContentDescription = stringResource(id = R.string.bandyer_fileshare_media),
        text = text
    )
}

@Composable
fun MediaFile(modifier: Modifier = Modifier, text: String) {
    FileDetails(
        modifier = modifier,
        iconBackgroundColor = LocalContentColor.current.copy(alpha = 0.9f),
        iconDrawable = R.drawable.ic_media,
        iconTint = MaterialTheme.colors.background,
        iconContentDescription = stringResource(id = R.string.bandyer_fileshare_media),
        text = text
    )
}

@Composable
private fun FileDetails(modifier: Modifier = Modifier,
                     iconBackgroundColor: Color,
                     @DrawableRes iconDrawable: Int,
                     iconTint: Color,
                     iconContentDescription: String,
                     text: String) {
        Box(contentAlignment = Alignment.Center, modifier = modifier
            .background(iconBackgroundColor, RoundedCornerShape(12.dp))
            .size(40.dp)) {
            Icon(
                painter = painterResource(iconDrawable),
                contentDescription = iconContentDescription,
                tint = iconTint,
                modifier = Modifier
                    .size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(text = text, style = MaterialTheme.typography.subtitle2)
        }
}