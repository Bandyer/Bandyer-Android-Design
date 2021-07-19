package com.bandyer.sdk_design.new_smartglass.utils.network.model

data class WiFiState(
    val state: State = State.UNKNOWN,
    val level: Level = Level.NO_SIGNAL
) {

    enum class State {
        DISABLING,
        DISABLED,
        ENABLING,
        ENABLED,
        UNKNOWN
    }

    enum class Level {
        NO_SIGNAL,
        POOR,
        FAIR,
        GOOD,
        EXCELLENT;

        companion object {
            fun getValue(rssi: Int) = when {
                rssi <= 0 && rssi >= -50 -> EXCELLENT
                rssi < -50 && rssi >= -70 -> GOOD
                rssi < -70 && rssi >= -80 -> FAIR
                rssi < -80 && rssi >= -100 -> POOR
                else -> NO_SIGNAL
            }
        }
    }
}