package com.bandyer.sdk_design.filesharing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bandyer.sdk_design.R

@Composable
fun FileShare(onBackPressed: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                topAppBarText = stringResource(id = R.string.bandyer_fileshare),
                onBackPressed = { onBackPressed() }
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
            onClick = { })
        },
        floatingActionButtonPosition = FabPosition.Center,
        content = {
            FilesList(modifier = Modifier.fillMaxSize()) {
              FileShareItem(
                  titleText = "documenti_ident.jpg",
                  fileSize = "58 Mb",
                  fileType = FileType.MISC,
                  user = "Mario Draghi",
                  progress = 0.6f,
                  modifier = Modifier
                      .fillMaxWidth()
                      .padding(16.dp)) {}
            }
        }
    )
}

@Composable
fun FilesList(
    modifier: Modifier = Modifier,
    content: @Composable() () -> Unit
) {
    LazyColumn(modifier = modifier) {
        item {
            content()
            Divider(color = Color.Black)
        }
    }
}

@Composable
private fun TopAppBar(topAppBarText: String, onBackPressed: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = topAppBarText,
                textAlign = TextAlign.Start
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = stringResource(id = R.string.bandyer_back)
                )
            }
        },
        backgroundColor = MaterialTheme.colors.primary,
    )
}

@Preview
@Composable
fun FileSharePreview() {
    FileShareComposeExperimentalTheme {
        FileShare {}
    }
}
