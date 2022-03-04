/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.demo_collaboration_suite_ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.KaleyraBottomSheet
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.items.ActionItem
import com.kaleyra.collaboration_suite_phone_ui.call.bottom_sheet.items.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.widgets.KaleyraCallActionWidget

class RingingActivity : AppCompatActivity() {

    private var callActionWidget: KaleyraCallActionWidget<ActionItem, KaleyraBottomSheet>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ringing)
        initializeBottomSheetLayout()
    }

    private fun initializeBottomSheetLayout() {
        callActionWidget = KaleyraCallActionWidget(this, findViewById(R.id.coordinator_layout), CallAction.getActions(this, cameraToggled = false, micToggled = true, withChat = true, withWhiteboard = true, withFileShare = true, withScreenShare = true))
        callActionWidget!!.showRingingControls()
    }
}