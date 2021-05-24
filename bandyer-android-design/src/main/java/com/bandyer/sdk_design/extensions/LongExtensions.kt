package com.bandyer.sdk_design.extensions

import java.text.SimpleDateFormat
import java.util.*

fun Long.parseToHHmm(): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    formatter.timeZone = TimeZone.getDefault()
    return formatter.format(this)
}