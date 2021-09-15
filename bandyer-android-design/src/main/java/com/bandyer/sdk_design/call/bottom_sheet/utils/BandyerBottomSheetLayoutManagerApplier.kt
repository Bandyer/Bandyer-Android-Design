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

package com.bandyer.sdk_design.call.bottom_sheet.utils

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.sdk_design.bottom_sheet.view.BottomSheetLayoutType
import com.bandyer.sdk_design.utils.AndroidDevice
import com.bandyer.sdk_design.utils.SupportedSmartGlasses

/**
 * Utils class that applies layout manager on input recyclerview based on input BottomSheetLayoutType
 * @constructor
 */
class BandyerBottomSheetLayoutManagerApplier(recyclerView: RecyclerView, bottomSheetLayoutType: BottomSheetLayoutType) {

    init {
        if (AndroidDevice.CURRENT !in SupportedSmartGlasses.list) {
            when (bottomSheetLayoutType) {
                is BottomSheetLayoutType.GRID -> {
                    recyclerView.layoutManager = GridLayoutManager(recyclerView.context, bottomSheetLayoutType.spanSize)
                }
                is BottomSheetLayoutType.LIST -> {
                    recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
                }
            }
        } else recyclerView.addItemDecoration(SmartGlassItemDecorator(recyclerView))
    }
}