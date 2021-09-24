package com.bandyer.video_android_glass_ui.common.item_decoration

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