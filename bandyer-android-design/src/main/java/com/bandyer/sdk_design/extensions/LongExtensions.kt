package com.bandyer.sdk_design.extensions

fun Long.parseToHHmm(): String {
    val min = (this / (1000 * 60) % 60)
    val hr = (this / (1000 * 60 * 60) % 24)
    return "$min:$hr"
}