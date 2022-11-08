package com.kaleyra.collaboration_suite_phone_ui.call.compose.submenu

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme

private val FabSize = 56.dp
private val FabIconPadding = 16.dp
private val FabPadding = 20.dp

@Composable
internal fun FileShare(items: ImmutableList<Int>, onClick: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        if (items.count < 1) EmptyList()
        else {
            LazyColumn(contentPadding = PaddingValues(bottom = 72.dp)) {
                items(items = items.value) {

                }
            }
        }
        ExtendedFloatingActionButton(
            text = if (items.count < 1) { { Text(text = stringResource(id = R.string.kaleyra_fileshare_add).uppercase()) } } else null,
            onClick = onClick,
            icon = { Icon(painter = painterResource(id = R.drawable.ic_kaleyra_add), contentDescription = null) },
            contentColor = MaterialTheme.colors.surface,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
        )
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
            minWidth = FabSize,
            minHeight = FabSize
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
            modifier = Modifier.padding(horizontal = padding),
            verticalAlignment = Alignment.CenterVertically
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
internal fun FileShareItem() {

}

@Preview
@Composable
internal fun FileSharePreview() {
    KaleyraTheme {
        Surface {
            FileShare(items = ImmutableList(listOf(0))) {
                
            }
        }
    }
}
