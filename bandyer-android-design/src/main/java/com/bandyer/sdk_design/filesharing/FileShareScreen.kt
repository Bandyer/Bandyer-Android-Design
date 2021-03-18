package com.bandyer.sdk_design.filesharing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.bandyer.sdk_design.R

sealed class FileShareItemButtonEvent {
    data class Cancel(val id: Int) : FileShareItemButtonEvent()
    data class Download(val id: Int) : FileShareItemButtonEvent()
    data class Retry(val id: Int) : FileShareItemButtonEvent()
}

data class FileShareItemEvent(val id: Int)

@Composable
fun FileShare(sharedFiles: List<FileShareData>, onNavIconPressed: () -> Unit = { }, onAddButtonPressed: () -> Unit = { }, onItemButtonEvent: (FileShareItemButtonEvent) -> Unit = { }, onItemEvent: (FileShareItemEvent) -> Unit =  { }) {
    Scaffold(
        topBar = {
            TopAppBar(
                topAppBarText = stringResource(id = R.string.bandyer_fileshare),
                onNavIconPressed = { onNavIconPressed() }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
            text = { Text(text = stringResource(id = R.string.bandyer_fileshare_add)) },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(id = R.string.bandyer_fileshare_add_description)
                )
            },
            onClick = { onAddButtonPressed() })
        },
        backgroundColor = MaterialTheme.colors.background,
        contentColor = MaterialTheme.colors.onBackground,
        floatingActionButtonPosition = FabPosition.Center,
        content = { FilesList(modifier = Modifier.fillMaxSize(), sharedFiles, onItemButtonEvent, onItemEvent) }
    )
}

@Composable
fun FilesList(modifier: Modifier = Modifier, fileShareItems: List<FileShareData>, onItemButtonEvent: (FileShareItemButtonEvent) -> Unit, onItemEvent: (FileShareItemEvent) -> Unit) {
    LazyColumn(modifier = modifier) {
        itemsIndexed(items = fileShareItems) { index, data ->
            FileShareItem(data = data,
                onButtonEvent = onItemButtonEvent,
                onEvent = onItemEvent)
            if(index != fileShareItems.size - 1) Divider(color = LocalContentColor.current.copy(alpha = 0.1f))
        }
    }
}

@Composable
private fun TopAppBar(topAppBarText: String, onNavIconPressed: () -> Unit) {
    TopAppBar(
        backgroundColor = MaterialTheme.colors.surface,
        contentColor = MaterialTheme.colors.onSurface,
        title = {
            Text(
                text = topAppBarText,
                textAlign = TextAlign.Start
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavIconPressed) {
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = stringResource(id = R.string.bandyer_back)
                )
            }
        },
    )
}

private val sharedFiles = listOf(
    FileShareData(0,"documento_ident.jpg", 0.6f, false, "Gianfranco", FileType.MEDIA, 500),
    FileShareData(1,"moto.pdf", 0.8f, false, "Mario", FileType.MISC, 433),
    FileShareData(2,"pasqua.zip", 0f, true, "Luigi", FileType.ARCHIVE, 346)
)

@Preview(name = "File Share in light theme")
@Composable
fun FileSharePreview() {
    BandyerSdkDesignComposeTheme {
        FileShare(sharedFiles = sharedFiles)
    }
}

@Preview(name = "File Share in dark theme")
@Composable
fun FileSharePreviewDark() {
    BandyerSdkDesignComposeTheme(darkTheme = true) {
        FileShare(sharedFiles = sharedFiles)
    }
}
