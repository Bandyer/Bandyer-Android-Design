package com.bandyer.sdk_design.filesharing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.bandyer.sdk_design.R

@Composable
fun FileShareItem(modifier: Modifier = Modifier,
                  data: FileShareData,
                  onButtonEvent: (FileShareButtonEvent) -> Unit,
                  onClick: () -> Unit) {
    ConstraintLayout(modifier = modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(16.dp)
        ) {

        val (fileDetails, title, progressBar, subTitle, error, button) = createRefs()

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .constrainAs(fileDetails) {
                start.linkTo(parent.start)
                top.linkTo(title.top)
            }) {
            when(data.fileType) {
                //TODO Add formatter for file size
                FileType.MISC -> MiscFile(text = data.fileSize.toString())
                FileType.MEDIA -> MediaFile(text = data.fileSize.toString())
                FileType.ARCHIVE -> ArchiveFile(text = data.fileSize.toString())
            }
        }

        Text(text = data.fileName,
            style = MaterialTheme.typography.subtitle1,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .constrainAs(title) {
                    start.linkTo(fileDetails.end)
                    top.linkTo(parent.top)
                    end.linkTo(button.start)
                    width = Dimension.fillToConstraints
                })

        LinearProgressIndicator(
            progress = data.progress,
            color = MaterialTheme.colors.secondary,
            backgroundColor = LocalContentColor.current.copy(alpha = 0.2f),
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .constrainAs(progressBar) {
                    start.linkTo(fileDetails.end)
                    top.linkTo(title.bottom)
                    end.linkTo(button.start)
                    width = Dimension.fillToConstraints
                })

        ConstraintLayout(modifier = Modifier
            .padding(horizontal = 16.dp)
            .constrainAs(subTitle) {
                start.linkTo(fileDetails.end)
                top.linkTo(progressBar.bottom)
                end.linkTo(button.start)
                width = Dimension.fillToConstraints
            }) {

            val (icon, user, time) = createRefs()

            Icon(
                painter = painterResource(if(data.isUpload) R.drawable.ic_upload_user else R.drawable.ic_download_user),
                contentDescription = stringResource(if(data.isUpload) R.string.bandyer_fileshare_upload else R.string.bandyer_fileshare_download),
                tint = if(!data.isUpload) MaterialTheme.colors.secondary else LocalContentColor.current,
                modifier = Modifier
                    .size(10.dp)
                    .constrainAs(icon) {
                        start.linkTo(parent.start)
                        centerVerticallyTo(parent)
                    }
            )

            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(text = data.sender,
                    style = MaterialTheme.typography.subtitle2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .constrainAs(user) {
                            start.linkTo(icon.end)
                            centerVerticallyTo(parent)
                        })

                //TODO change text
                Text(text = data.progress.toString(),
                    style = MaterialTheme.typography.subtitle2,
                    modifier = Modifier
                        .constrainAs(time) {
                            end.linkTo(parent.end)
                            centerVerticallyTo(parent)
                        })
            }
        }

        Text(text = stringResource(id = R.string.bandyer_fileshare_error_text),
            color = MaterialTheme.colors.error,
            style = MaterialTheme.typography.subtitle2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                .constrainAs(error) {
                    start.linkTo(fileDetails.end)
                    top.linkTo(subTitle.bottom)
                    end.linkTo(button.start)
                    width = Dimension.fillToConstraints
                })

        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .constrainAs(button) {
                    top.linkTo(progressBar.top)
                    bottom.linkTo(progressBar.bottom)
                    end.linkTo(parent.end)
                }) {
//            TODO add condition
//            when() {
                  CancelButton(onClick = { onButtonEvent(FileShareButtonEvent.Cancel) })
//                DownloadButton(onClick = { onButtonEvent(FileShareButtonEvent.Download) })
//                ReDownloadButton(onClick = { onButtonEvent(FileShareButtonEvent.Download) })
//                RetryButton(onClick = { onButtonEvent(FileShareButtonEvent.Retry) })
//            }
        }
    }
}

@Preview(name = "File Share Item in light theme")
@Composable
fun FileShareItemPreview() {
    BandyerSdkDesignComposeTheme {
        Surface(color = MaterialTheme.colors.background) {
            FileShareItem(data = FileShareData(
                "documento_ident.jpg",
                0.6f,
                false,
                "Gianfranco",
                FileType.MEDIA,
                500
            ),
                onButtonEvent = {},
                onClick = {})
        }
    }
}

@Preview(name = "File Share Item in dark theme")
@Composable
fun FileShareItemPreviewDark() {
    BandyerSdkDesignComposeTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colors.background) {
            FileShareItem(data = FileShareData(
                "documento_ident.jpg",
                0.6f,
                false,
                "Gianfranco",
                FileType.MEDIA,
                500
            ),
                onButtonEvent = {},
                onClick = {})
        }
    }
}