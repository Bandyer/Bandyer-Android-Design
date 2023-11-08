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

package com.kaleyra.video_glasses_sdk.utils.extensions

import com.kaleyra.video.configuration.Configuration
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.utils.DeviceUtils
import com.kaleyra.video_glasses_sdk.call.GlassCallActivity
import com.kaleyra.video_glasses_sdk.chat.GlassChatActivity
import com.kaleyra.video_glasses_sdk.chat.notification.GlassChatNotificationActivity
import com.kaleyra.video_glasses_sdk.termsandconditions.GlassTermsAndConditionsActivity

/**
 * Configure with glass u i
 *
 * @param configuration representing a set of info necessary to instantiate the communication
 */
fun KaleyraVideo.configure(
    configuration: Configuration
) = configure(
    configuration,
    GlassCallActivity::class.java,
    GlassChatActivity::class.java,
    GlassTermsAndConditionsActivity::class.java,
    GlassChatNotificationActivity::class.java.takeIf { DeviceUtils.isGoogleGlass }
)