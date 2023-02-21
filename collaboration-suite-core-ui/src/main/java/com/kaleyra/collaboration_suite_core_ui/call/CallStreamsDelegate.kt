package com.kaleyra.collaboration_suite_core_ui.call

import android.content.Context
import com.kaleyra.collaboration_suite.phonebox.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.plus

/**
 * CallStreamsDelegate. It is responsible of setting up the call's streams and videos.
 */
interface CallStreamsDelegate : CameraStreamPublisher, CameraStreamInputsDelegate, StreamsOpeningDelegate, StreamsVideoViewDelegate {

    companion object {
        const val SCREEN_SHARE_STREAM_ID = "screenshare"
    }

    /**
     * Set up the call's streams
     *
     * @param context A context
     * @param call The call
     */
    fun setUpCallStreams(context: Context, call: Call, coroutineScope: CoroutineScope) {
        val localScope = MainScope() + CoroutineName("CallStreamDelegateScope(callId = ${call.id})")
        addCameraStream(call)
        updateCameraStreamOnInputs(call, localScope)
        openParticipantsStreams(call.participants, localScope)
        setStreamsVideoView(context, call.participants, localScope)

        call.state
            .takeWhile { it !is Call.State.Disconnected.Ended }
            .onCompletion { localScope.cancel() }
            .launchIn(coroutineScope)
    }

}


