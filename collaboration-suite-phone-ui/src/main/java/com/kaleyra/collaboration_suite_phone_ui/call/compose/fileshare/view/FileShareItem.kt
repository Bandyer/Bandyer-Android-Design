package com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view

import android.content.res.Configuration
import android.net.Uri
import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.collaboration_suite_core_ui.utils.TimestampUtils
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.Ellipsize
import com.kaleyra.collaboration_suite_phone_ui.call.compose.EllipsizeText
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.ProgressIndicatorTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.FileUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.SharedFileUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.mockDownloaSharedFile
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.mockUploadSharedFile
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import kotlin.math.roundToInt

private val LinearProgressIndicatorWidth = 3000.dp

@Composable
internal fun FileShareItem(
    sharedFile: SharedFileUi,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FileTypeAndSize(
            file = sharedFile.file,
            modifier = Modifier.padding(start = 6.dp)
        )

        Spacer(Modifier.width(28.dp))

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SharedFileInfoAndProgress(
                    sharedFile = sharedFile,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(16.dp))
                ActionButton(
                    sharedFileState = sharedFile.state,
                    onActionClick = onActionClick
                )
            }

            if (sharedFile.state is SharedFileUi.State.Error) {
                ErrorMessage(sharedFile.type)
            }
        }
    }
}

@Composable
private fun FileTypeAndSize(file: FileUi, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(
                id = when (file.type) {
                    FileUi.Type.Media -> R.drawable.ic_kaleyra_image
                    FileUi.Type.Archive -> R.drawable.ic_kaleyra_zip
                    FileUi.Type.Miscellaneous -> R.drawable.ic_kaleyra_file
                }
            ),
            contentDescription = stringResource(
                id = when (file.type) {
                    FileUi.Type.Media -> R.string.kaleyra_fileshare_media
                    FileUi.Type.Archive -> R.string.kaleyra_fileshare_archive
                    FileUi.Type.Miscellaneous -> R.string.kaleyra_fileshare_miscellaneous
                }
            ),
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (file.size != null) {
                Formatter.formatShortFileSize(LocalContext.current, file.size)
            } else {
                // If it is Download && state != InProgress && state != Success
                stringResource(id = R.string.kaleyra_fileshare_na)
            },
            color = LocalContentColor.current.copy(alpha = .5f),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SharedFileInfoAndProgress(
    sharedFile: SharedFileUi,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {

        EllipsizeText(
            text = sharedFile.file.name,
            color = MaterialTheme.colors.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            ellipsize = Ellipsize.Middle
        )

        LinearProgressIndicator(
            progress = if (sharedFile.state is SharedFileUi.State.InProgress) sharedFile.state.progress else 0f,
            modifier = Modifier
                .padding(vertical = 2.dp)
                .size(LinearProgressIndicatorWidth, ProgressIndicatorDefaults.StrokeWidth)
                .clip(RoundedCornerShape(percent = 50))
                .testTag(ProgressIndicatorTag),
            color = MaterialTheme.colors.secondaryVariant,
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(
                    id = when (sharedFile.type) {
                        SharedFileUi.Type.Upload -> R.drawable.ic_kaleyra_upload
                        SharedFileUi.Type.Download -> R.drawable.ic_kaleyra_download
                    }
                ),
                contentDescription = stringResource(
                    id = when (sharedFile.type) {
                        SharedFileUi.Type.Upload -> R.string.kaleyra_fileshare_upload
                        SharedFileUi.Type.Download -> R.string.kaleyra_fileshare_download
                    }
                ),
                tint = LocalContentColor.current.copy(alpha = .8f),
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (sharedFile.type == SharedFileUi.Type.Download) sharedFile.sender else stringResource(
                    id = R.string.kaleyra_fileshare_you
                ),
                maxLines = 1,
                color = LocalContentColor.current.copy(alpha = .5f),
                fontSize = 12.sp,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = when (sharedFile.state) {
                    is SharedFileUi.State.InProgress -> stringResource(
                        id = R.string.kaleyra_fileshare_progress,
                        (sharedFile.state.progress * 100).roundToInt()
                    )
                    else -> TimestampUtils.parseTime(sharedFile.time)
                },
                color = LocalContentColor.current.copy(alpha = .5f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ActionButton(
    sharedFileState: SharedFileUi.State,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onActionClick,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(
                id = when (sharedFileState) {
                    is SharedFileUi.State.Available -> R.drawable.ic_kaleyra_download
                    is SharedFileUi.State.Success -> R.drawable.ic_kaleyra_check
                    is SharedFileUi.State.Error -> R.drawable.ic_kaleyra_retry
                    else -> R.drawable.ic_kaleyra_fileshare_cancel
                }
            ),
            contentDescription = stringResource(
                id = when (sharedFileState) {
                    is SharedFileUi.State.Available -> R.string.kaleyra_fileshare_download_descr
                    is SharedFileUi.State.Success -> R.string.kaleyra_fileshare_open_file
                    is SharedFileUi.State.Error -> R.string.kaleyra_fileshare_retry
                    else -> R.string.kaleyra_fileshare_cancel
                }
            ),
            tint = when (sharedFileState) {
                is SharedFileUi.State.Success, is SharedFileUi.State.Error -> MaterialTheme.colors.surface
                else -> LocalContentColor.current.copy(alpha = .3f)
            },
            modifier = Modifier
                .size(32.dp)
                .border(
                    width = 2.dp,
                    color = when (sharedFileState) {
                        is SharedFileUi.State.Success -> MaterialTheme.colors.secondaryVariant
                        is SharedFileUi.State.Error -> MaterialTheme.colors.error
                        else -> LocalContentColor.current.copy(alpha = .3f)
                    },
                    shape = CircleShape
                )
                .background(
                    color = when (sharedFileState) {
                        is SharedFileUi.State.Success -> MaterialTheme.colors.secondaryVariant
                        is SharedFileUi.State.Error -> MaterialTheme.colors.error
                        else -> Color.Transparent
                    },
                    shape = CircleShape
                )
                .padding(8.dp)
        )
    }
}

@Composable
private fun ErrorMessage(sharedFileType: SharedFileUi.Type) {
    Text(
        text = stringResource(
            id = when (sharedFileType) {
                SharedFileUi.Type.Upload -> R.string.kaleyra_fileshare_upload_error
                else -> R.string.kaleyra_fileshare_download_error
            }
        ),
        color = MaterialTheme.colors.error,
        fontSize = 12.sp
    )
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun FileShareItemInProgressPreview() {
    FileShareItemPreview(sharedFile = mockUploadSharedFile)
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun FileShareItemCancelledPreview() {
    FileShareItemPreview(sharedFile = mockUploadSharedFile.copy(state = SharedFileUi.State.Cancelled))
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun FileShareItemErrorPreview() {
    FileShareItemPreview(sharedFile = mockUploadSharedFile.copy(state = SharedFileUi.State.Error))
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun FileShareItemAvailablePreview() {
    FileShareItemPreview(sharedFile = mockUploadSharedFile.copy(state = SharedFileUi.State.Available))
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun FileShareItemPendingPreview() {
    FileShareItemPreview(sharedFile = mockUploadSharedFile.copy(state = SharedFileUi.State.Pending))
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun FileShareItemSuccessPreview() {
    FileShareItemPreview(sharedFile = mockDownloaSharedFile.copy(state = SharedFileUi.State.Success(Uri.EMPTY)))
}

@Composable
private fun FileShareItemPreview(sharedFile: SharedFileUi) {
    KaleyraTheme {
        Surface {
            FileShareItem(
                sharedFile = sharedFile,
                onActionClick = {}
            )
        }
    }
}