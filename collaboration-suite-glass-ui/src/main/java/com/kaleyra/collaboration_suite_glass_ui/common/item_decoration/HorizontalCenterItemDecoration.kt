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

package com.kaleyra.collaboration_suite_glass_ui.common.item_decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * An item decoration to center the first and last element a the recycler view
 */
internal open class HorizontalCenterItemDecoration : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val itemPosition: Int = parent.getChildAdapterPosition(view)
        if (itemPosition == RecyclerView.NO_POSITION) return

        when (itemPosition) {
            0 -> outRect.set(getOffsetPixelSize(parent, view), 0, 0, 0)
            parent.adapter!!.itemCount - 1 -> outRect.set(0, 0, getOffsetPixelSize(parent, view), 0)
        }
    }

    private fun getOffsetPixelSize(parent: RecyclerView, view: View): Int {
        val orientationHelper = OrientationHelper.createHorizontalHelper(parent.layoutManager)
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        return ((orientationHelper.totalSpace - view.measuredWidth) / 2).coerceAtLeast(0)
    }
}