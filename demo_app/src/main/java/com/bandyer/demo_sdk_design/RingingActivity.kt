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

package com.bandyer.demo_sdk_design

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bandyer.sdk_design.bottom_sheet.BandyerBottomSheet
import com.bandyer.sdk_design.bottom_sheet.items.ActionItem
import com.bandyer.sdk_design.call.bottom_sheet.items.CallAction
import com.bandyer.sdk_design.call.widgets.BandyerCallActionWidget

class RingingActivity : AppCompatActivity() {

    private var callActionWidget: BandyerCallActionWidget<ActionItem, BandyerBottomSheet>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ringing)
        initializeBottomSheetLayout()
    }

    private fun initializeBottomSheetLayout() {
        callActionWidget = BandyerCallActionWidget(this, findViewById(R.id.coordinator_layout), CallAction.getActions(this, cameraToggled = false, micToggled = true, withChat = true, withWhiteboard = true, withFileShare = true, withScreenShare = true))
        callActionWidget!!.showRingingControls()
    }
}