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

package com.kaleyra.video_common_ui.texttospeech

import android.content.Context
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.utils.AppLifecycle
import com.kaleyra.video_utils.ContextRetainer
import com.kaleyra.video_utils.proximity_listener.ProximitySensor
import kotlinx.coroutines.CoroutineScope

internal interface TextToSpeechNotifier {

    val call: CallUI

    val proximitySensor: ProximitySensor

    val callTextToSpeech: CallTextToSpeech

    val context: Context
        get() = ContextRetainer.context

    val shouldNotify: Boolean
        get() = !AppLifecycle.isInForeground.value || proximitySensor.isNear()

    fun start(scope: CoroutineScope)

    fun dispose()

}