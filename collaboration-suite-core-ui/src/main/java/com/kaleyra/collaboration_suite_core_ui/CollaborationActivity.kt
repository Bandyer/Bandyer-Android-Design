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

package com.kaleyra.collaboration_suite_core_ui

import androidx.appcompat.app.AppCompatActivity

/**
 * Collaboration activity
 */
abstract class CollaborationActivity : AppCompatActivity() {

    /**
     * Checking if the CollaborationUI is configured. If it is not, it is requesting a new configuration.
     * @return true if is configured, false otherwise
     **/
    protected suspend fun requestConfigure(): Boolean {
        if (!CollaborationUI.isConfigured) CollaborationService.get()?.onRequestNewCollaborationConfigure()
        return CollaborationUI.isConfigured
    }
}