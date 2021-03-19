package com.bandyer.sdk_design.filesharing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
                  item: FileShareItemData,
                  onButtonEvent: (FileShareItemButtonEvent) -> Unit,
                  onEvent: (FileShareItemEvent) -> Unit) {
    ConstraintLayout(modifier = modifier
        .fillMaxWidth()
        .clickable(onClick = { onEvent(FileShareItemEvent(item)) })
        .padding(16.dp)
        ) {

        val (fileDetails, title, progressBar, subTitle, error, button) = createRefs()

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .constrainAs(fileDetails) {
                start.linkTo(parent.start)
                top.linkTo(title.top)
            }) {
            when(item.fileType) {
                //TODO Add formatter for file size
                FileType.MISC -> MiscFile(text = item.fileSize.toString())
                FileType.MEDIA -> MediaFile(text = item.fileSize.toString())
                FileType.ARCHIVE -> ArchiveFile(text = item.fileSize.toString())
            }
        }

        Text(text = item.fileName,
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
            progress = item.progress,
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
                painter = painterResource(if(item.isUpload) R.drawable.ic_upload_user else R.drawable.ic_download_user),
                contentDescription = stringResource(if(item.isUpload) R.string.bandyer_fileshare_upload else R.string.bandyer_fileshare_download),
                tint = if(!item.isUpload) MaterialTheme.colors.secondary else LocalContentColor.current,
                modifier = Modifier
                    .size(10.dp)
                    .constrainAs(icon) {
                        start.linkTo(parent.start)
                        centerVerticallyTo(parent)
                    }
            )

            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(text = item.sender,
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
                Text(text = item.progress.toString(),
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
                  item.CancelButton(onClick = { onButtonEvent(FileShareItemButtonEvent.Cancel(item)) })
//                DownloadButton(onClick = { onButtonEvent(FileShareButtonEvent.Download(data)) })
//                ReDownloadButton(onClick = { onButtonEvent(FileShareButtonEvent.Download(data)) })
//                RetryButton(onClick = { onButtonEvent(FileShareButtonEvent.Retry(data)) })
//            }
        }
    }
}

@Preview(name = "File Share Item in light theme")
@Composable
fun FileShareItemPreview() {
    BandyerSdkDesignComposeTheme {
        Surface(color = MaterialTheme.colors.background) {
            FileShareItem(item = FileShareItemData(
                0,
                "documento_ident.jpg",
                0.6f,
                false,
                "Gianfranco",
                FileType.MEDIA,
                500
            ),
                onButtonEvent = {},
                onEvent = {})
        }
    }
}

@Preview(name = "File Share Item in dark theme")
@Composable
fun FileShareItemPreviewDark() {
    BandyerSdkDesignComposeTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colors.background) {
            FileShareItem(item = FileShareItemData(
                1,
                "documento_ident.jpg",
                0.6f,
                false,
                "Gianfranco",
                FileType.MEDIA,
                500
            ),
                onButtonEvent = {},
                onEvent = {})
        }
    }
}