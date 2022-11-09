package com.kaleyra.collaboration_suite_phone_ui.call.compose.submenu

import android.text.format.Formatter
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.collaboration_suite_core_ui.utils.TimestampUtils
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.model.Transfer
import com.kaleyra.collaboration_suite_phone_ui.call.compose.model.mockDownloadTransfer
import com.kaleyra.collaboration_suite_phone_ui.call.compose.model.mockUploadTransfer
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import kotlin.math.roundToInt

private val FabSize = 56.dp
private val FabIconPadding = 16.dp
private val FabPadding = 20.dp

@Composable
internal fun FileShare(items: ImmutableList<Int>, onClick: () -> Unit, onClosePressed: () -> Unit) {
    SubMenuLayout(
        title = "File share", onClosePressed = onClosePressed
    ) {
        Box(Modifier.fillMaxSize()) {
            if (items.count < 1) EmptyList()
            else {
                LazyColumn(contentPadding = PaddingValues(bottom = 72.dp)) {
                    items(items = items.value) {
                        FileShareItem(mockUploadTransfer.copy(state = Transfer.State.Error), {}, {})
                    }
                }
            }
            ExtendedFloatingActionButton(text = if (items.count < 1) {
                {
                    Text(
                        text = stringResource(id = R.string.kaleyra_fileshare_add).uppercase(),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else null,
                onClick = onClick,
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_kaleyra_add),
                        contentDescription = null
                    )
                },
                contentColor = MaterialTheme.colors.surface,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp)
            )
        }
    }
}

@Composable
internal fun ExtendedFloatingActionButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: @Composable (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
    backgroundColor: Color = MaterialTheme.colors.secondary,
    contentColor: Color = contentColorFor(backgroundColor),
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation()
) {
    FloatingActionButton(
        modifier = modifier.sizeIn(
            minWidth = FabSize, minHeight = FabSize
        ),
        onClick = onClick,
        interactionSource = interactionSource,
        shape = shape,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        elevation = elevation
    ) {
        val padding = if (text == null) 0.dp else FabPadding
        Row(
            modifier = Modifier
                .padding(horizontal = padding)
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon()
            if (text != null) {
                Spacer(Modifier.width(FabIconPadding))
                text()
            }
        }
    }
}

@Composable
private fun EmptyList() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.disabled) {
            Icon(
                painter = painterResource(id = R.drawable.ic_kaleyra_folder),
                contentDescription = null,
                modifier = Modifier
                    .padding(20.dp)
                    .size(96.dp)
            )
            Text(
                text = stringResource(id = R.string.kaleyra_no_file_shared),
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(id = R.string.kaleyra_click_to_share_file),
                textAlign = TextAlign.Center,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
internal fun FileShareItem(transfer: Transfer, onActionClick: () -> Unit, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = transfer.state == Transfer.State.Success,
                onClickLabel = stringResource(R.string.kaleyra_open_file),
                role = Role.Button,
                onClick = onClick
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.padding(start = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = iconFor(transfer.fileType),
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formattedFileSize(transfer),
                color = LocalContentColor.current.copy(alpha = .5f),
                fontSize = 12.sp
            )
        }
        Spacer(Modifier.width(28.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(text = transfer.fileName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(
                        progress = transfer.progress,
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(percent = 50)),
                        color = MaterialTheme.colors.secondaryVariant,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = iconFor(transfer),
                            contentDescription = null,
                            tint = LocalContentColor.current.copy(alpha = .8f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (transfer is Transfer.Download) transfer.sender else stringResource(id = R.string.kaleyra_fileshare_you),
                            color = LocalContentColor.current.copy(alpha = .5f),
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = progressTextFor(transfer),
                            color = LocalContentColor.current.copy(alpha = .5f),
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                IconButton(
                    onClick = onActionClick, modifier = Modifier
                ) {
                    Icon(
                        painter = iconFor(transfer.state),
                        contentDescription = "",
                        tint = iconTintFor(transfer.state),
                        modifier = Modifier
                            .size(32.dp)
                            .border(
                                width = 2.dp,
                                color = borderColorFor(transfer.state),
                                shape = CircleShape
                            )
                            .background(
                                color = backgroundColorFor(transfer.state), shape = CircleShape
                            )
                            .padding(8.dp)
                    )
                }
            }
            if (transfer.state == Transfer.State.Error) {
                Text(
                    text = stringResource(id = if (transfer is Transfer.Upload) R.string.kaleyra_fileshare_upload_error else R.string.kaleyra_fileshare_download_error),
                    color = MaterialTheme.colors.error,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun formattedFileSize(transfer: Transfer) = when {
    transfer is Transfer.Download && transfer.state != Transfer.State.InProgress && transfer.state != Transfer.State.Success -> stringResource(R.string.kaleyra_fileshare_na)
    else -> Formatter.formatShortFileSize(LocalContext.current, transfer.fileSize)
}

@Composable
private fun backgroundColorFor(state: Transfer.State) = when (state) {
    Transfer.State.Success -> MaterialTheme.colors.secondaryVariant
    Transfer.State.Error -> MaterialTheme.colors.error
    else -> Color.Transparent
}

@Composable
private fun iconTintFor(state: Transfer.State) = when (state) {
    Transfer.State.Success, Transfer.State.Error -> MaterialTheme.colors.surface
    else -> LocalContentColor.current.copy(alpha = .3f)
}

@Composable
private fun borderColorFor(state: Transfer.State) = when (state) {
    Transfer.State.Success -> MaterialTheme.colors.secondaryVariant
    Transfer.State.Error -> MaterialTheme.colors.error
    else -> LocalContentColor.current.copy(alpha = .3f)
}

@Composable
private fun progressTextFor(transfer: Transfer) = when (transfer.state) {
    Transfer.State.Available, Transfer.State.Success -> TimestampUtils.parseTime(transfer.time)
    else -> stringResource(id = R.string.kaleyra_fileshare_progress, (transfer.progress * 100).roundToInt())
}

@Composable
private fun iconFor(fileType: Transfer.FileType) = painterResource(
    id = when (fileType) {
        Transfer.FileType.Image -> R.drawable.ic_kaleyra_image
        Transfer.FileType.Archive -> R.drawable.ic_kaleyra_zip
        Transfer.FileType.File -> R.drawable.ic_kaleyra_file
    }
)

@Composable
private fun iconFor(transfer: Transfer) = painterResource(
    id = when (transfer) {
        is Transfer.Upload -> R.drawable.ic_kaleyra_upload
        is Transfer.Download -> R.drawable.ic_kaleyra_download
    }
)

@Composable
private fun iconFor(state: Transfer.State) = painterResource(
    id = when (state) {
        Transfer.State.Available -> R.drawable.ic_kaleyra_download
        Transfer.State.Success -> R.drawable.ic_kaleyra_check
        Transfer.State.Error -> R.drawable.ic_kaleyra_retry
        else -> R.drawable.ic_kaleyra_fileshare_cancel
    }
)

@Preview
@Composable
internal fun FileShareItemInProgressPreview() {
    KaleyraTheme {
        FileShareItem(mockUploadTransfer, {}, {})
    }
}

@Preview
@Composable
internal fun FileShareItemCancelledPreview() {
    KaleyraTheme {
        FileShareItem(mockDownloadTransfer.copy(state = Transfer.State.Cancelled), {}, {})
    }
}

@Preview
@Composable
internal fun FileShareItemErrorPreview() {
    KaleyraTheme {
        FileShareItem(mockUploadTransfer.copy(state = Transfer.State.Error), {}, {})
    }
}

@Preview
@Composable
internal fun FileShareItemAvailablePreview() {
    KaleyraTheme {
        FileShareItem(mockDownloadTransfer.copy(state = Transfer.State.Available), {}, {})
    }
}

@Preview
@Composable
internal fun FileSharePendingPreview() {
    KaleyraTheme {
        FileShareItem(mockUploadTransfer.copy(state = Transfer.State.Pending), {}, {})
    }
}

@Preview
@Composable
internal fun FileShareSuccessPreview() {
    KaleyraTheme {
        FileShareItem(mockDownloadTransfer.copy(state = Transfer.State.Success), {}, {})
    }
}
