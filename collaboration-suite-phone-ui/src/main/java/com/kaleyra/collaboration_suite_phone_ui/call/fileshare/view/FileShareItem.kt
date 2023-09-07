package com.kaleyra.collaboration_suite_phone_ui.call.fileshare.view

import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.text.format.Formatter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.UriExtensions.getMimeType
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.Ellipsize
import com.kaleyra.collaboration_suite_phone_ui.call.EllipsizeText
import com.kaleyra.collaboration_suite_phone_ui.call.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.call.fileshare.ProgressIndicatorTag
import com.kaleyra.collaboration_suite_phone_ui.call.fileshare.model.SharedFileUi
import com.kaleyra.collaboration_suite_phone_ui.call.fileshare.model.mockDownloadSharedFile
import com.kaleyra.collaboration_suite_phone_ui.call.fileshare.model.mockUploadSharedFile
import com.kaleyra.collaboration_suite_core_ui.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.extensions.isArchiveMimeType
import com.kaleyra.collaboration_suite_phone_ui.extensions.isImageMimeType
import kotlin.math.roundToInt

private const val FileMediaType = "MediaType"
private const val FileArchiveType = "ArchiveType"
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
            fileUri = sharedFile.uri,
            fileSize = sharedFile.size,
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
                ErrorMessage(sharedFile.isMine)
            }
        }
    }
}

@Composable
private fun FileTypeAndSize(
    fileUri: ImmutableUri,
    fileSize: Long?,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        val fileType = getFileType(LocalContext.current, fileUri.value)
        Icon(
            painter = painterResource(
                id = when (fileType) {
                    FileMediaType -> R.drawable.ic_kaleyra_image
                    FileArchiveType -> R.drawable.ic_kaleyra_zip
                    else -> R.drawable.ic_kaleyra_file
                }
            ),
            contentDescription = stringResource(
                id = when (fileType) {
                    FileMediaType -> R.string.kaleyra_fileshare_media
                    FileArchiveType -> R.string.kaleyra_fileshare_archive
                    else -> R.string.kaleyra_fileshare_miscellaneous
                }
            ),
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (fileSize != null) {
                Formatter.formatShortFileSize(LocalContext.current, fileSize)
            } else {
                // The file size is NA when is Download && state != InProgress && state != Success
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
        val progress by animateFloatAsState(targetValue = when (sharedFile.state) {
            is SharedFileUi.State.InProgress -> sharedFile.state.progress
            is SharedFileUi.State.Success -> 1f
            else -> 0f
        })

        EllipsizeText(
            text = sharedFile.name,
            color = MaterialTheme.colors.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            ellipsize = Ellipsize.Middle
        )

        LinearProgressIndicator(
            progress = progress,
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
                    id = if (sharedFile.isMine) R.drawable.ic_kaleyra_upload else R.drawable.ic_kaleyra_download
                ),
                contentDescription = stringResource(id = if (sharedFile.isMine) R.string.kaleyra_fileshare_upload else R.string.kaleyra_fileshare_download),
                tint = LocalContentColor.current.copy(alpha = .8f),
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (sharedFile.isMine) stringResource(id = R.string.kaleyra_fileshare_you) else sharedFile.sender,
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

private fun getFileType(context: Context, uri: Uri): String? {
    val mimeType = uri.getMimeType(context) ?: ""
    return when {
        mimeType.isImageMimeType()-> FileMediaType
        mimeType.isArchiveMimeType() -> FileArchiveType
        else -> null
    }
}

@Composable
private fun ErrorMessage(isMyMessage: Boolean) {
    Text(
        text = stringResource(
            id = if (isMyMessage) R.string.kaleyra_fileshare_upload_error else R.string.kaleyra_fileshare_download_error
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
    FileShareItemPreview(sharedFile = mockDownloadSharedFile.copy(state = SharedFileUi.State.Success(ImmutableUri(Uri.EMPTY))))
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