/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_core_ui.utils

import java.time.Instant

/**
 * @suppress
 * Helper class for handling ISO 8601 strings of the following format:
 * "2008-03-01T13:00:00+01:00". It also supports parsing the "Z" timezone.
 */
object Iso8601 {

    /**
     * Get the current time in ISO8601 format
     *
     * @return String
     */
    fun nowISO8601(): String = Instant.now().toString()

    /**
     * Get the current millis in UTC timezone
     *
     * @return Time in millis
     */
    fun nowUTCMillis(): Long = Instant.now().toEpochMilli()

    /**
     * Get a iso8601 formatted timestamp as millis
     *
     * @param tstamp The timestamp
     * @return Time in millis
     */
    fun getISO8601TstampInMillis(tstamp: String): Long = Instant.parse(tstamp).toEpochMilli()

    /**
     * Parse a millis timestamp into ISO8601 string
     *
     * @param millis The timestamp
     * @return String The ISO8601 pattern string
     */
    fun parseMillisToIso8601(millis: Long): String = Instant.ofEpochMilli(millis).toString()
}
