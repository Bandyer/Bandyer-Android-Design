package com.bandyer.video_android_glass_ui.utils.extensions

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Scroll horizontally the recycler view to the next element. If the next element is not visible, it scrolls by a display width by default.
 *
 * @receiver RecyclerView
 * @param currentIndex The index of the current item
 */
internal fun RecyclerView.horizontalSmoothScrollToNext(currentIndex: Int) {
    if (currentIndex >= adapter!!.itemCount - 1) return
    val target = findViewHolderForAdapterPosition(currentIndex + 1)?.itemView
    if (target == null) smoothScrollBy(context.resources.displayMetrics.widthPixels, 0)
    else scrollToTarget(target)
}

/**
 * Scroll horizontally the recycler view to the previous element. If the previous element is not visible, it scrolls by a negative display width by default.
 *
 * @receiver RecyclerView
 * @param currentIndex The index of the current item
 */
internal fun RecyclerView.horizontalSmoothScrollToPrevious(currentIndex: Int) {
    if (currentIndex <= 0) return
    val target = findViewHolderForAdapterPosition(currentIndex - 1)?.itemView
    if (target == null) smoothScrollBy(-context.resources.displayMetrics.widthPixels, 0)
    else scrollToTarget(target)
}

private fun RecyclerView.scrollToTarget(target: View) {
    val targetX = (target.left + target.right) / 2f
    val half = context.resources.displayMetrics.widthPixels / 2
    smoothScrollBy((targetX - half).toInt(), 0)
}