package com.bandyer.sdk_design.call.bottom_sheet.utils

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.sdk_design.bottom_sheet.view.BottomSheetLayoutType
import com.bandyer.sdk_design.utils.isRealWearHTM1

/**
 * Utils class that applies layout manager on input recyclerview based on input BottomSheetLayoutType
 * @constructor
 */
class BandyerBottomSheetLayoutManagerApplier(recyclerView: RecyclerView, bottomSheetLayoutType: BottomSheetLayoutType) {

    init {
        if (!isRealWearHTM1()) {
            when (bottomSheetLayoutType) {
                is BottomSheetLayoutType.GRID -> {
                    recyclerView.layoutManager = GridLayoutManager(recyclerView.context, bottomSheetLayoutType.spanSize)
                }
                is BottomSheetLayoutType.LIST -> {
                    recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
                }
            }
        } else recyclerView.addItemDecoration(RealWearItemDecorator(recyclerView))
    }
}