package com.kaleyra.collaboration_suite_phone_ui.call.appbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.common.button.IconButton
import com.kaleyra.collaboration_suite_phone_ui.common.topappbar.TopAppBar

@Composable
internal fun CallAppBar(
    title: String,
    onBackPressed: () -> Unit,
    actions: @Composable (RowScope.() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        navigationIcon = {
            IconButton(
                icon = painterResource(id = R.drawable.ic_kaleyra_back_down),
                iconDescription = stringResource(id = R.string.kaleyra_close),
                onClick = onBackPressed,
                modifier = Modifier.padding(4.dp)
            )
        },
        content = {
            Text(
                text = title,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )
        },
        actions = {
            if (actions != null) {
                actions()
            }
        },
        elevation = 0.dp,
        contentPadding = WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal).asPaddingValues(),
        modifier = modifier
    )
}