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

import android.content.Context
import android.view.View

/**
 * KaleyraCollaborationSuiteUI prefs that can be used as an alternative default values to some views
 * @author kristiyan
 */
const val DESIGN_PREFS = "KaleyraCollaborationSuiteUIPrefs"

internal fun View.kaleyraCollaborationSuiteUIPrefs() = context.getSharedPreferences(DESIGN_PREFS, Context.MODE_PRIVATE)