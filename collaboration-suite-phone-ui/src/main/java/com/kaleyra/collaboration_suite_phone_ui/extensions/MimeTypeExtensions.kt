/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_phone_ui.extensions

internal object MimeTypeExtensions{

    private val imageTypes = listOf("image/gif", "image/vnd.microsoft.icon", "image/jpeg", "image/png", "image/svg+xml", "image/tiff", "image/webp", "image/x-photoshop")
    private val archiveTypes = listOf("application/zip", "application/x-7z-compressed", "application/x-bzip", "application/x-bzip2", "application/gzip", "application/vnd.rar")

    fun String.isImageMimeType(): Boolean {
        return imageTypes.contains(this)
    }

    fun String.isArchiveMimeType(): Boolean {
        return archiveTypes.contains(this)
    }
}
