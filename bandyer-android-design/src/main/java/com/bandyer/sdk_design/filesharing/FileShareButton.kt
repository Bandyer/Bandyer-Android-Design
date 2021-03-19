package com.bandyer.sdk_design.filesharing

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bandyer.sdk_design.R

interface FileShareStyle {
    @Composable
    fun CancelButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
        FileShareButton(
            modifier = modifier,
            drawable = R.drawable.ic_cancel_download,
            iconColor = MaterialTheme.colors.secondary,
            borderColor = MaterialTheme.colors.secondary,
            backgroundColor = Color.Transparent,
            contentDescription = stringResource(id = R.string.bandyer_fileshare_retry),
            onClick = onClick
        )
    }
}

@Composable
fun DownloadButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    FileShareButton(modifier = modifier,
        drawable = R.drawable.ic_download,
        iconColor = MaterialTheme.colors.secondary,
        borderColor = MaterialTheme.colors.secondary,
        backgroundColor = Color.Transparent,
        contentDescription = stringResource(id = R.string.bandyer_fileshare_retry),
        onClick = onClick)
}

@Composable
fun ReDownloadButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    FileShareButton(modifier = modifier,
        drawable = R.drawable.ic_download,
        iconColor = MaterialTheme.colors.onSecondary,
        borderColor = MaterialTheme.colors.secondary,
        backgroundColor = MaterialTheme.colors.secondary,
        contentDescription = stringResource(id = R.string.bandyer_fileshare_retry),
        onClick = onClick)
}

@Composable
fun RetryButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    FileShareButton(modifier = modifier,
        drawable = R.drawable.ic_download_retry,
        iconColor = MaterialTheme.colors.onError,
        borderColor = MaterialTheme.colors.error,
        backgroundColor = MaterialTheme.colors.error,
        contentDescription = stringResource(id = R.string.bandyer_fileshare_retry),
        onClick = onClick)
}

@Composable
private fun FileShareButton(modifier: Modifier = Modifier,
                            @DrawableRes drawable: Int,
                            iconColor: Color,
                            borderColor: Color,
                            backgroundColor: Color,
                            contentDescription: String,
                            onClick: () -> Unit) {
        BandyerIconButton(
            size = 24.dp,
            modifier = modifier
                .border(width = 2.dp, borderColor, CircleShape)
                .background(backgroundColor, CircleShape),
            onClick = onClick
        ) {
            Icon(
                painter = painterResource(drawable),
                contentDescription = contentDescription,
                tint = iconColor,
                modifier = Modifier.size(12.dp)
            )
        }
}