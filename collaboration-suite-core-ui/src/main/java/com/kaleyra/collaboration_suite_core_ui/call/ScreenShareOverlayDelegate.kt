package com.kaleyra.collaboration_suite_core_ui.call

import android.content.Context
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite_core_ui.overlay.AppViewOverlay
import com.kaleyra.collaboration_suite_core_ui.overlay.StatusBarOverlayView
import com.kaleyra.collaboration_suite_core_ui.overlay.ViewOverlayAttacher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onCompletion

internal interface ScreenShareOverlayDelegate {

    fun syncScreenShareOverlay(context: Context, call: Call, scope: CoroutineScope) {
        var screenShareOverlay: AppViewOverlay? = null
        combine(
            isDeviceScreenShareEnabled(call, scope),
            isApplicationScreenShareEnabled(call, scope)
        ) { device, application ->
            when {
                device -> {
                    screenShareOverlay = AppViewOverlay(
                        StatusBarOverlayView(context),
                        ViewOverlayAttacher.OverlayType.GLOBAL
                    )
                    screenShareOverlay!!.show(context)
                }

                application -> {
                    screenShareOverlay = AppViewOverlay(
                        StatusBarOverlayView(context),
                        ViewOverlayAttacher.OverlayType.CURRENT_APPLICATION
                    )
                    screenShareOverlay!!.show(context)
                }

                else -> {
                    screenShareOverlay?.hide()
                    screenShareOverlay = null
                }
            }
        }.onCompletion {
            screenShareOverlay?.hide()
            screenShareOverlay = null
        }.launchIn(scope)
    }

    fun isDeviceScreenShareEnabled(call: Call, scope: CoroutineScope): Flow<Boolean> =
        call.inputs
            .availableInputs
            .mapLatest { it.filterIsInstance<Input.Video.Screen.My>().firstOrNull() }
            .flatMapLatest { it?.enabled ?: flowOf(false) }


    fun isApplicationScreenShareEnabled(call: Call, scope: CoroutineScope): Flow<Boolean> =
        call.inputs
            .availableInputs
            .mapLatest { it.filterIsInstance<Input.Video.Application>().firstOrNull() }
            .flatMapLatest { it?.enabled ?: flowOf(false) }
}