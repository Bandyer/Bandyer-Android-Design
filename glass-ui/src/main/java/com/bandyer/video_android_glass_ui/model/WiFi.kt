package com.bandyer.video_android_glass_ui.model

/**
 * WiFi
 *
 * @property state WiFi state
 * @property level WiFi signal level
 * @constructor
 */
data class WiFi(
    val state: State = State.UNKNOWN,
    val level: Level = Level.NO_SIGNAL
) {

    /**
     * WiFi state
     */
    enum class State {
        /**
         * d i s a b l i n g
         */
        DISABLING,

        /**
         * d i s ab l e d
         */
        DISABLED,

        /**
         * e n a b l i n g
         */
        ENABLING,

        /**
         * e n a b l e d
         */
        ENABLED,

        /**
         * u n k n o w n
         */
        UNKNOWN
    }

    /**
     * WiFi signal level
     */
    enum class Level {
        /**
         * no_signal
         */
        NO_SIGNAL,

        /**
         * p o o r
         */
        POOR,

        /**
         * f a i r
         */
        FAIR,

        /**
         * g o o d
         */
        GOOD,

        /**
         * e x c  e l l e n t
         */
        EXCELLENT;

        /**
         * Converter
         */
        companion object Converter {
            /**
             * Map rssi value to 5 values
             *
             * @param rssi The rssi value
             * @return Level
             */
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