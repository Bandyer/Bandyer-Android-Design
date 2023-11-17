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

package com.kaleyra.video_sdk.call.fileshare.model

import android.net.Uri
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri

val mockUploadSharedFile = SharedFileUi(
    id = "1",
    name = "upload.txt",
    uri = ImmutableUri(Uri.EMPTY),
    size = 23333L,
    sender = "Mario",
    time = 324234L,
    state = SharedFileUi.State.InProgress(progress = .7f),
    isMine = true
)

val mockDownloadSharedFile = SharedFileUi(
    id = "2",
    name = "download.txt",
    uri = ImmutableUri(Uri.EMPTY),
    size = 40000L,
    sender = "Keanu",
    time = 3254234L,
    state = SharedFileUi.State.InProgress(progress = .4f),
    isMine = false
)