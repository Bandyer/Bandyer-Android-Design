package com.kaleyra.video_common_ui.utils.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

internal object CoroutineExtensions {
    fun CoroutineScope.launchBlocking(block: suspend () -> Unit) = launch { runBlocking { block() } }
}