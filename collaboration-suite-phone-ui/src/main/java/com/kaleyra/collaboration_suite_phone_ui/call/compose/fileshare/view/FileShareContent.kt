package com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.SharedFileUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.mockDownloadSharedFile
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.mockUploadSharedFile
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import com.kaleyra.collaboration_suite_core_ui.theme.KaleyraTheme

const val FileShareItemTag = "FileShareItemTag"
const val FileShareItemDividerTag = "FileShareItemDividerTag"
private val ContentBottomPadding = 72.dp

@Composable
internal fun FileShareContent(
    items: ImmutableList<SharedFileUi>,
    onItemClick: (SharedFileUi) -> Unit,
    onItemActionClick: (SharedFileUi) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 600.dp),
        contentPadding = PaddingValues(bottom = ContentBottomPadding),
        modifier = modifier
    ) {
        itemsIndexed(items = items.value, key = { _, item -> item.id }) { index, item ->
            FileShareItem(
                sharedFile = item,
                modifier = Modifier
                    .clickable(
                        enabled = item.state is SharedFileUi.State.Success,
                        onClickLabel = stringResource(R.string.kaleyra_fileshare_open_file),
                        role = Role.Button,
                        onClick = { onItemClick(item) }
                    )
                    .testTag(FileShareItemTag),
                onActionClick = { onItemActionClick(item) }
            )
            if (index != 0) {
                Divider(
                    color = LocalContentColor.current.copy(.2f),
                    modifier = Modifier.testTag(FileShareItemDividerTag)
                )
            }
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
                items = ImmutableList(listOf(mockDownloadSharedFile, mockUploadSharedFile)),
                onItemClick = {},
                onItemActionClick = {}
            )
        }
    }
}