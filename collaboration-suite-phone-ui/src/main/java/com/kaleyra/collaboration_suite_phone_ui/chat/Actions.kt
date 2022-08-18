package com.kaleyra.collaboration_suite_phone_ui.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.kaleyra.collaboration_suite_core_ui.Action
import com.kaleyra.collaboration_suite_phone_ui.R

internal typealias ClickableAction = Pair<Action, () -> Unit>

@Composable
internal fun Actions(actions: Set<ClickableAction>) {
    actions.firstOrNull { (action, _) -> action is Action.AudioCall }?.let { (_, onClick) ->
        MenuIcon(
            painter = painterResource(R.drawable.ic_kaleyra_audio_call),
            onClick = onClick,
            contentDescription = stringResource(id = R.string.kaleyra_start_audio_call)
        )
    }

    actions.firstOrNull { (action, _) -> action is Action.AudioUpgradableCall }?.let { (_, onClick) ->
        MenuIcon(
            painter = painterResource(R.drawable.ic_kaleyra_audio_upgradable_call),
            onClick = onClick,
            contentDescription = stringResource(id = R.string.kaleyra_start_audio_upgradable_call)
        )
    }

    actions.firstOrNull { (action, _) -> action is Action.VideoCall }?.let { (_, onClick) ->
        MenuIcon(
            painter = painterResource(R.drawable.ic_kaleyra_video_call),
            onClick = onClick,
            contentDescription = stringResource(id = R.string.kaleyra_start_video_call)
        )
    }
}