package com.kaleyra.collaboration_suite_core_ui.utils

object TimerParser {
    /**
     * Parse the timestamp into mm:ss or hh:mm:ss format
     *
     * @param timestamp Timestamp expressed in seconds
     */
    fun parseTimestamp(timestamp: Long): String {
        val hours = timestamp / 3600
        val minutes = (timestamp / 60) % 60
        val seconds = timestamp % 60
        return if (hours == 0L) String.format("%02d:%02d", minutes, seconds)
        else String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}