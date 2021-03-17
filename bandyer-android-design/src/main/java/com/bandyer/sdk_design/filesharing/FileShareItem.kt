package com.bandyer.sdk_design.filesharing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.bandyer.sdk_design.R

@Composable
fun FileShareItem(modifier: Modifier = Modifier,
                  titleText: String,
                  fileSize: String,
                  fileType: FileType,
                  user: String,
                  progress: Float,
                  onClick: () -> Unit) {
    ConstraintLayout(modifier = modifier) {

        val (fileInfo, title, progressBar, subTitle, error, button) = createRefs()

        FileInfo(text = fileSize, fileType = fileType, Modifier
            .constrainAs(fileInfo) {
                start.linkTo(parent.start)
                top.linkTo(title.top)
            })

        Text(text = titleText,
            style = MaterialTheme.typography.subtitle1,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .constrainAs(title) {
                    start.linkTo(fileInfo.end)
                    top.linkTo(parent.top)
                    end.linkTo(button.start)
                    width = Dimension.fillToConstraints
                })

        LinearProgressIndicator(
            progress = progress,
            color = MaterialTheme.colors.secondary,
            backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .constrainAs(progressBar) {
                    start.linkTo(fileInfo.end)
                    top.linkTo(title.bottom)
                    end.linkTo(button.start)
                    width = Dimension.fillToConstraints
                })

        SubTitle(userText = user,
            isUpload = false,
            timeProgressText = "13:23",
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .constrainAs(subTitle) {
                    start.linkTo(fileInfo.end)
                    top.linkTo(progressBar.bottom)
                    end.linkTo(button.start)
                    width = Dimension.fillToConstraints
                })

        Text(text = stringResource(id = R.string.bandyer_fileshare_error_text),
            color = Color.Red,
            style = MaterialTheme.typography.subtitle2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                .constrainAs(error) {
                    start.linkTo(fileInfo.end)
                    top.linkTo(subTitle.bottom)
                    end.linkTo(button.start)
                    width = Dimension.fillToConstraints
                })

        ActionButton(
            Modifier
                .constrainAs(button) {
                    top.linkTo(progressBar.top)
                    bottom.linkTo(progressBar.bottom)
                    end.linkTo(parent.end)
                }) {}

    }
}

@Composable
private fun SubTitle(userText: String, isUpload: Boolean, timeProgressText: String, modifier: Modifier = Modifier) {

    val iconDrawable = if(isUpload) R.drawable.bandyer_add else R.drawable.bandyer_add
    val iconDescription = if(isUpload) R.string.bandyer_fileshare_upload else R.string.bandyer_fileshare_download

    ConstraintLayout(modifier = modifier) {

        val (icon, user, time) = createRefs()

        Icon(
            painter = painterResource(iconDrawable),
            contentDescription = stringResource(iconDescription),
            modifier = Modifier
                .size(10.dp)
                .constrainAs(icon) {
                    start.linkTo(parent.start)
                    centerVerticallyTo(parent)
                }
        )

        Text(text = userText,
            style = MaterialTheme.typography.subtitle2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .constrainAs(user) {
                    start.linkTo(icon.end)
                    centerVerticallyTo(parent)
                })

        Text(text = timeProgressText,
            style = MaterialTheme.typography.subtitle2,
            modifier = Modifier
                .constrainAs(time) {
                    end.linkTo(parent.end)
                    centerVerticallyTo(parent)
                })
    }
}

@Composable
private fun FileInfo(text: String, fileType: FileType, modifier: Modifier = Modifier) {

    val boxBackgroundColor = if(fileType == FileType.MEDIA) MaterialTheme.colors.onSurface.copy(alpha = 0.9f) else MaterialTheme.colors.onSurface.copy(alpha = 0.2f)

    val iconTint = if(fileType == FileType.ARCHIVE) MaterialTheme.colors.onSurface else MaterialTheme.colors.surface

    val iconDrawable = when(fileType) {
        FileType.FILE -> R.drawable.bandyer_add
        FileType.MEDIA -> R.drawable.bandyer_add
        FileType.ARCHIVE -> R.drawable.bandyer_add
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier
            .background(boxBackgroundColor, RoundedCornerShape(12.dp))
            .size(40.dp)) {
            Icon(
                painter = painterResource(iconDrawable),
                contentDescription = stringResource(id = R.string.app_name),
                tint = iconTint,
                modifier = Modifier
                    .size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = text, style = MaterialTheme.typography.subtitle2)
    }
}

@Composable
private fun ActionButton(modifier: Modifier = Modifier, errorOccurred: Boolean = false, onClick: () -> Unit) {
//    val iconColor =
//        val iconDrawable =
    Column(modifier = modifier) {
        BandyerIconButton(
            size = 32.dp,
            modifier = Modifier.border(width = 2.dp, Color.Blue, CircleShape),
            onClick = onClick
        ) {
            Icon(
                painter = painterResource(R.drawable.bandyer_add),
                contentDescription = stringResource(id = R.string.app_name),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}