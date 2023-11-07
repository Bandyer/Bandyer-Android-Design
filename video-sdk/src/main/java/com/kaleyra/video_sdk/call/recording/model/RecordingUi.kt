package com.kaleyra.video_sdk.call.recording.model

data class RecordingUi(
    val type: RecordingTypeUi,
    val state: RecordingStateUi
) {
    fun isRecording() = state == RecordingStateUi.Started
}