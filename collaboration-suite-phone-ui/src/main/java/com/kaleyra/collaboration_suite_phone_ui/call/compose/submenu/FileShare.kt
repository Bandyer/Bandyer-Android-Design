package com.kaleyra.collaboration_suite_phone_ui.call.compose.submenu

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme

@Composable
internal fun FileShare() {

//    LazyColumn {
//
//    }
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
            EmptyList()
        }
    }
}
