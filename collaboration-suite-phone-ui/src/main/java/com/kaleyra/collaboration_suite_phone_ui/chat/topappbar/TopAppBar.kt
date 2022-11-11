@file:OptIn(ExperimentalFoundationApi::class)

package com.kaleyra.collaboration_suite_phone_ui.chat.topappbar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp

internal const val ActionsTag = "ActionsTag"

// TODO move in a common package for call and chat
@Composable
internal fun TopAppBar(
    navigationIcon: @Composable RowScope.() -> Unit,
    content: @Composable (RowScope.() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    elevation: Dp = AppBarDefaults.TopAppBarElevation
) {
    androidx.compose.material.TopAppBar(
        modifier = Modifier.focusGroup(),
        elevation = elevation,
        backgroundColor = MaterialTheme.colors.primary,
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
            Row(verticalAlignment = Alignment.CenterVertically, content = navigationIcon)

            if (content != null) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    content = content
                )
            }

            if (actions != null) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .testTag(ActionsTag),
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions
                )
            }
        }
    }
}