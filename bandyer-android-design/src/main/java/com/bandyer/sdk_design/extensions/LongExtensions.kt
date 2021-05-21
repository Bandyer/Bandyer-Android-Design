package com.bandyer.sdk_design.extensions

fun Long.parseToHHmm(): String {
    val mm = (this / (1000 * 60) % 60)
    val hh = (this / (1000 * 60 * 60) % 24)
    return "$hh:$mm"
}