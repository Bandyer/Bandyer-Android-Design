package com.bandyer.sdk_design.new_smartglass.utils.network.model

data class BatteryState(val status: Status = Status.UNKNOWN, val plugged: Plugged = Plugged.UNKNOWN, val percentage: Int = -1) {

    enum class Status {
        CHARGING,
        DISCHARGING,
        FULL,
        NOT_CHARGING,
        UNKNOWN
    }

    enum class Plugged {
        AC,
        USB,
        WIRELESS,
        UNKNOWN
    }
}

