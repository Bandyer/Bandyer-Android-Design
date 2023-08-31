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

package com.kaleyra.collaboration_suite_phone_ui.utils

import com.kaleyra.collaboration_suite.Collaboration
import com.kaleyra.collaboration_suite_core_ui.CollaborationUI
import com.kaleyra.collaboration_suite_phone_ui.call.compose.PhoneCallActivity
import com.kaleyra.collaboration_suite_phone_ui.call.compose.termsandconditions.PhoneTermsAndConditionsActivity
import com.kaleyra.collaboration_suite_phone_ui.chat.PhoneChatActivity

/**
 * Configure with phone u i
 *
 * @param configuration representing a set of info necessary to instantiate the communication
 */
fun CollaborationUI.configure(
    configuration: Collaboration.Configuration
) = configure(
    configuration,
    PhoneCallActivity::class.java,
    PhoneChatActivity::class.java,
    PhoneTermsAndConditionsActivity::class.java,
    chatNotificationActivityClazz = null
)