package com.kaleyra.collaboration_suite_phone_ui.call.compose.recording.model

data class RecordingUi(
    val type: RecordingTypeUi,
    val state: RecordingStateUi
) {
    fun isRecording() = state == RecordingStateUi.Started
}