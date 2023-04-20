package com.kaleyra.collaboration_suite_phone_ui.call.compose.pointer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.call.shadow

val PointerSize = 16.dp

@Composable
internal fun TextPointer(
    username: String,
    onTextWidth: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val textStyle = LocalTextStyle.current
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Pointer()
        Text(
            text = username,
            color = MaterialTheme.colors.secondary,
            modifier = Modifier.onGloballyPositioned { onTextWidth(it.size.width) },
            style = textStyle.shadow()
        )
    }
}

@Composable
internal fun Pointer(modifier: Modifier = Modifier) {
    Spacer(
        modifier
            .size(PointerSize)
            .background(
                color = MaterialTheme.colors.secondary,
                shape = CircleShape
            )
    )
}