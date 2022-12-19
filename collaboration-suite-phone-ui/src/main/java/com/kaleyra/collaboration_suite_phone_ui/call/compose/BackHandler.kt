package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.runtime.*

// TODO move common package between chat and call
@Composable
fun BackPressHandler(onBackPressed: () -> Unit) {
    val currentOnBackPressed by rememberUpdatedState(onBackPressed)

    val backCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                currentOnBackPressed()
            }
        }
    }

    val backDispatcher = LocalBackPressedDispatcher.current

    DisposableEffect(backDispatcher) {
        backDispatcher.addCallback(backCallback)

        onDispose {
            backCallback.remove()
        }
    }
}

/**
 * This [CompositionLocal] is used to provide an [OnBackPressedDispatcher]:

 * CompositionLocalProvider(
 *     LocalBackPressedDispatcher provides requireActivity().onBackPressedDispatcher
 * ) { }

 * and setting up the callbacks with [BackPressHandler].
 */
val LocalBackPressedDispatcher = staticCompositionLocalOf<OnBackPressedDispatcher> { error("No Back Dispatcher provided") }