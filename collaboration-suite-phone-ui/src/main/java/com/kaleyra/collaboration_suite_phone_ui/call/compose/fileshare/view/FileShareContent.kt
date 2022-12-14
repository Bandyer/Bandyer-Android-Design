package com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.TransferUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.mockDownloadTransfer
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.mockUploadTransfer
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme

const val FileShareItemTag = "FileShareItemTag"
private val ContentBottomPadding = 72.dp

@Composable
internal fun FileShareContent(
    items: ImmutableList<TransferUi>,
    onItemClick: (TransferUi) -> Unit,
    onItemActionClick: (TransferUi) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 600.dp),
        contentPadding = PaddingValues(bottom = ContentBottomPadding),
        modifier = modifier
    ) {
        items(items = items.value, key = { it.id }) {
            FileShareItem(
                transfer = it,
                modifier = Modifier
                    .clickable(
                        enabled = it.state is TransferUi.State.Success,
                        onClickLabel = stringResource(R.string.kaleyra_fileshare_open_file),
                        role = Role.Button,
                        onClick = { onItemClick(it) }
                    )
                    .testTag(FileShareItemTag),
                onActionClick = { onItemActionClick(it) }
            )
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun FileShareContentPreview() {
    KaleyraTheme {
        Surface {
            FileShareContent(
                items = ImmutableList(listOf(mockDownloadTransfer, mockUploadTransfer)),
                onItemClick = {},
                onItemActionClick = {}
            )
        }
    }
}