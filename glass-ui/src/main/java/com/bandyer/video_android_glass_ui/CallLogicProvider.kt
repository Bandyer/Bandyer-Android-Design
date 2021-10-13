package com.bandyer.video_android_glass_ui

import kotlinx.coroutines.flow.StateFlow
import java.io.Serializable

internal interface CallLogicProvider: Serializable {

    /**
     * CallLogicProvider Factory
     */
    companion object Factory {
        fun create(controllerCall: CallUIController): CallLogicProvider = CallLogicProviderImpl(controllerCall)
    }

    fun getCallState(): StateFlow<CallState>

    fun getRecordingState(): StateFlow<Boolean>

    fun getDuration(): StateFlow<Long>

    fun getParticipants(): StateFlow<List<CallParticipant>>

    fun hangup()

    fun disableCamera(disable: Boolean)

    fun disableMic(disable: Boolean)

    fun switchCamera()

    fun setVolume(value: Int)

    fun setZoom(value: Int)
}

private class CallLogicProviderImpl(private val controllerCall: CallUIController) : CallLogicProvider {

    override fun getCallState(): StateFlow<CallState> = controllerCall.state

    override fun getRecordingState(): StateFlow<Boolean> = controllerCall.recording

    override fun getDuration(): StateFlow<Long> = controllerCall.duration

    override fun getParticipants(): StateFlow<List<CallParticipant>> = controllerCall.participants

    override fun hangup() = controllerCall.hangup()

    override fun disableCamera(disable: Boolean) = controllerCall.disableCamera(disable)

    override fun disableMic(disable: Boolean) = controllerCall.disableMic(disable)

    override fun switchCamera() = controllerCall.switchCamera()

    override fun setVolume(value: Int) = controllerCall.setVolume(value)

    override fun setZoom(value: Int) = controllerCall.setZoom(value)
}
