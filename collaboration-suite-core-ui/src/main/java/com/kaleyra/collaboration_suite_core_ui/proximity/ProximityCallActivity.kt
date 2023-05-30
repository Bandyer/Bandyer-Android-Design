package com.kaleyra.collaboration_suite_core_ui.proximity

interface ProximityCallActivity : WindowTouchDelegate {

    val isInForeground: Boolean

    val isInPip: Boolean

    val isFileShareDisplayed: Boolean

    val isWhiteboardDisplayed: Boolean
}