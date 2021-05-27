package com.bandyer.demo_communication_center.utils

/**
 * @suppress
 * @author kristiyan
 */
internal fun String?.prepend(value: String): String? {
    this ?: return this
    return "$value$this"
}