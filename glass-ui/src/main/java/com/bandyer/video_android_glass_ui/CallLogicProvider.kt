package com.bandyer.video_android_glass_ui

import kotlinx.coroutines.flow.Flow

internal interface CallLogicProvider {

    /**
     * CallLogicProvider Factory
     */
    companion object Factory {
        fun create(controllerCall: CallUIController): CallLogicProvider = CallLogicProviderImpl(controllerCall)
    }

    val call: Flow<Call>

    fun hangup()

    fun enableCamera(enable: Boolean)

    fun enableMic(enable: Boolean)

    fun switchCamera()

    fun setVolume(value: Int)

    fun setZoom(value: Int)
}

private class CallLogicProviderImpl(private val controllerCall: CallUIController) : CallLogicProvider {

    override val call: Flow<Call> = controllerCall.call

    override fun hangup() = controllerCall.hangup()

    override fun enableCamera(enable: Boolean) = controllerCall.enableCamera(enable)

    override fun enableMic(enable: Boolean) = controllerCall.enableMic(enable)

    override fun switchCamera() = controllerCall.switchCamera()

    override fun setVolume(value: Int) = controllerCall.setVolume(value)

    override fun setZoom(value: Int) = controllerCall.setZoom(value)
}
