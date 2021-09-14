/*
 * Copyright 2021-2022 Bandyer @ https://www.bandyer.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.bandyer.sdk_design.extensions

/**
 * Give the type of file given a mime type
 *
 * @receiver The file mime type
 * @return The type of file
 */
fun String.getFileTypeFromMimeType(): String {
    val imageTypes = listOf("image/gif", "image/vnd.microsoft.icon", "image/jpeg", "image/png", "image/svg+xml", "image/tiff", "image/webp", "image/x-photoshop")
    val archiveType = listOf("application/zip", "application/x-7z-compressed", "application/x-bzip", "application/x-bzip2", "application/gzip", "application/vnd.rar")
    return when {
        imageTypes.contains(this) -> "image"
        archiveType.contains(this) -> "archive"
        else -> "file"
    }
}