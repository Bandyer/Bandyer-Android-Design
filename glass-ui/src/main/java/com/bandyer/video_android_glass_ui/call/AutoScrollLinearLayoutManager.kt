package com.bandyer.video_android_glass_ui.call

import android.content.Context
import android.util.DisplayMetrics
import android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.video_android_core_ui.extensions.ContextExtensions.isRTL

/**
 * A [LinearLayoutManager] which enables an ellipsis marquee like behavior.
 * It automatically scrolls the recycler view towards the last item and then towards the first.
 *
 * @constructor
 */
class AutoScrollLinearLayoutManager constructor(
    context: Context,
    @RecyclerView.Orientation orientation: Int,
    reverseLayout: Boolean
) : LinearLayoutManager(context, orientation, reverseLayout) {

    private val isRTL = context.isRTL()

    /**
     * Smooth scroller which override speed per pixel
     */
    private var smoothScroller = object : LinearSmoothScroller(context) {
        override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float =
            SPEED / displayMetrics!!.densityDpi
    }

    /**
     * Scroll listener which starts a new scroll everytime the recycler view hits the left/right side
     */
    private var scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState != SCROLL_STATE_IDLE) return

            val lastVisibleItemPosition = findLastVisibleItemPosition()
            // Check if the last visible item is fully visible
            val isLastFullyVisible =
                if(isRTL) findViewByPosition(lastVisibleItemPosition)!!.left == recyclerView.left
                else findViewByPosition(lastVisibleItemPosition)!!.right == recyclerView.right

            // Start a new scroll to the opposite side of the current one
            recyclerView.smoothScrollToPosition(
                // If the last visible item is the last adapter item and it is fully visible, then scroll to the first ..
                if (lastVisibleItemPosition == itemCount - 1 && isLastFullyVisible) 0
                // .. else scroll to the last
                else itemCount - 1
            )
        }
    }

    /**
     * @suppress
     */
    override fun onAttachedToWindow(view: RecyclerView?) {
        super.onAttachedToWindow(view)
        view?.apply {
            addOnScrollListener(scrollListener)
            smoothScrollToPosition(itemCount - 1)
        }
    }

    /**
     * @suppress
     */
    override fun onDetachedFromWindow(view: RecyclerView?, recycler: RecyclerView.Recycler?) {
        super.onDetachedFromWindow(view, recycler)
        view?.removeOnScrollListener(scrollListener)
    }

    /**
     * @suppress
     */
    override fun smoothScrollToPosition(
        recyclerView: RecyclerView?,
        state: RecyclerView.State?,
        position: Int
    ) {
        smoothScroller.apply {
            targetPosition = position
            startSmoothScroll(this)
        }
    }

    private companion object {
        /**
         * Lower is faster (default is 25f)
         */
        const val SPEED = 4000f
    }
}