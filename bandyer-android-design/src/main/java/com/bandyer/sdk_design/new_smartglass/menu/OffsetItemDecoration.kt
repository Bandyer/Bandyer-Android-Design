package com.bandyer.sdk_design.new_smartglass.menu

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView

internal class OffsetItemDecoration : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val itemPosition: Int = parent.getChildAdapterPosition(view)
        if (itemPosition == RecyclerView.NO_POSITION) return

        when (itemPosition) {
            0 ->
                outRect.set(getOffsetPixelSize(parent, view), 0, 0, 0)
            parent.adapter!!.itemCount - 1 ->
                outRect.set(0, 0, getOffsetPixelSize(parent, view), 0)
        }
    }

    private fun getOffsetPixelSize(parent: RecyclerView, view: View): Int {
        val orientationHelper = OrientationHelper.createHorizontalHelper(parent.layoutManager)
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        return ((orientationHelper.totalSpace - view.measuredWidth) / 2).coerceAtLeast(0)
    }
}