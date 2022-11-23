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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.collaboration_suite_core_ui.utils.TimestampUtils
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.ProgressIndicatorTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.FileUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.TransferUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.mockDownloadTransfer
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.mockUploadTransfer
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import kotlin.math.roundToInt

private val LinearProgressIndicatorWidth = 3000.dp

@Composable
internal fun FileShareItem(
    transfer: TransferUi,
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
            file = transfer.file,
            modifier = Modifier.padding(start = 6.dp)
        )

        Spacer(Modifier.width(28.dp))

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FileNameAndTransferInfo(
                    transfer = transfer,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(16.dp))
                ActionButton(
                    transferState = transfer.state,
                    onActionClick = onActionClick
                )
            }

            if (transfer.state is TransferUi.State.Error) {
                ErrorMessage(transfer.type)
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
private fun FileNameAndTransferInfo(
    transfer: TransferUi,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {

        Text(
            text = transfer.file.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        LinearProgressIndicator(
            progress = if (transfer.state is TransferUi.State.InProgress) transfer.state.progress else 0f,
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
                    id = when (transfer.type) {
                        TransferUi.Type.Upload -> R.drawable.ic_kaleyra_upload
                        TransferUi.Type.Download -> R.drawable.ic_kaleyra_download
                    }
                ),
                contentDescription = stringResource(
                    id = when (transfer.type) {
                        TransferUi.Type.Upload -> R.string.kaleyra_fileshare_upload
                        TransferUi.Type.Download -> R.string.kaleyra_fileshare_download
                    }
                ),
                tint = LocalContentColor.current.copy(alpha = .8f),
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (transfer.type == TransferUi.Type.Download) transfer.sender else stringResource(
                    id = R.string.kaleyra_fileshare_you
                ),
                color = LocalContentColor.current.copy(alpha = .5f),
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = when (transfer.state) {
                    is TransferUi.State.InProgress -> stringResource(
                        id = R.string.kaleyra_fileshare_progress,
                        (transfer.state.progress * 100).roundToInt()
                    )
                    else -> TimestampUtils.parseTime(transfer.time)
                },
                color = LocalContentColor.current.copy(alpha = .5f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ActionButton(
    transferState: TransferUi.State,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onActionClick,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(
                id = when (transferState) {
                    is TransferUi.State.Available -> R.drawable.ic_kaleyra_download
                    is TransferUi.State.Success -> R.drawable.ic_kaleyra_check
                    is TransferUi.State.Error -> R.drawable.ic_kaleyra_retry
                    else -> R.drawable.ic_kaleyra_fileshare_cancel
                }
            ),
            contentDescription = stringResource(
                id = when (transferState) {
                    is TransferUi.State.Available -> R.string.kaleyra_fileshare_download_descr
                    is TransferUi.State.Success -> R.string.kaleyra_fileshare_open_file
                    is TransferUi.State.Error -> R.string.kaleyra_fileshare_retry
                    else -> R.string.kaleyra_fileshare_cancel
                }
            ),
            tint = when (transferState) {
                is TransferUi.State.Success, is TransferUi.State.Error -> MaterialTheme.colors.surface
                else -> LocalContentColor.current.copy(alpha = .3f)
            },
            modifier = Modifier
                .size(32.dp)
                .border(
                    width = 2.dp,
                    color = when (transferState) {
                        is TransferUi.State.Success -> MaterialTheme.colors.secondaryVariant
                        is TransferUi.State.Error -> MaterialTheme.colors.error
                        else -> LocalContentColor.current.copy(alpha = .3f)
                    },
                    shape = CircleShape
                )
                .background(
                    color = when (transferState) {
                        is TransferUi.State.Success -> MaterialTheme.colors.secondaryVariant
                        is TransferUi.State.Error -> MaterialTheme.colors.error
                        else -> Color.Transparent
                    },
                    shape = CircleShape
                )
                .padding(8.dp)
        )
    }
}

@Composable
private fun ErrorMessage(transferType: TransferUi.Type) {
    Text(
        text = stringResource(
            id = when (transferType) {
                TransferUi.Type.Upload -> R.string.kaleyra_fileshare_upload_error
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
    FileShareItemPreview(transfer = mockUploadTransfer)
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun FileShareItemCancelledPreview() {
    FileShareItemPreview(transfer = mockUploadTransfer.copy(state = TransferUi.State.Cancelled))
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun FileShareItemErrorPreview() {
    FileShareItemPreview(transfer = mockUploadTransfer.copy(state = TransferUi.State.Error))
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun FileShareItemAvailablePreview() {
    FileShareItemPreview(transfer = mockUploadTransfer.copy(state = TransferUi.State.Available))
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun FileShareItemPendingPreview() {
    FileShareItemPreview(transfer = mockUploadTransfer.copy(state = TransferUi.State.Pending))
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun FileShareItemSuccessPreview() {
    FileShareItemPreview(transfer = mockDownloadTransfer.copy(state = TransferUi.State.Success(Uri.EMPTY)))
}

@Composable
private fun FileShareItemPreview(transfer: TransferUi) {
    KaleyraTheme {
        Surface {
            FileShareItem(
                transfer = transfer,
                onActionClick = {}
            )
        }
    }
}