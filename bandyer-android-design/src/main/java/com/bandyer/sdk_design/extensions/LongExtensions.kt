package com.bandyer.sdk_design.extensions

import java.text.SimpleDateFormat
import java.util.*

/**
 * Parse the millis time in a HH:mm format
 *
 * @receiver The long millis time to parse
 * @return The string representing the time in a HH:mm format
 */
fun Long.parseToHHmm(): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    formatter.timeZone = TimeZone.getDefault()
    return formatter.format(this)
}