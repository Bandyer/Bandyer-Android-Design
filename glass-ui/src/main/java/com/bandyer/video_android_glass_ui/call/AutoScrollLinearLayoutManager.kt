package com.bandyer.video_android_glass_ui.call

import android.content.Context
import android.util.DisplayMetrics
import android.widget.AbsListView.OnScrollListener.*
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
        private var target = 0

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            // The recyclerView can enter the idle state also if scrollBy is used
            if (newState == SCROLL_STATE_IDLE) {
                val firstVisibleItemPosition = findFirstVisibleItemPosition()
                val lastVisibleItemPosition = findLastVisibleItemPosition()
                // Check if the last visible item is fully visible
                val isFirstOnEdge =
                    if (isRTL) findViewByPosition(firstVisibleItemPosition)!!.right == recyclerView.right
                    else findViewByPosition(firstVisibleItemPosition)!!.left == recyclerView.left
                val isLastOnEdge =
                    if (isRTL) findViewByPosition(lastVisibleItemPosition)!!.left == recyclerView.left
                    else findViewByPosition(lastVisibleItemPosition)!!.right == recyclerView.right

                when {
                    // If the scroll toward the last item is finished, update the target
                    lastVisibleItemPosition == itemCount - 1 && isFirstOnEdge -> target = 0
                    // If the scroll toward the first item is finished, update the target
                    firstVisibleItemPosition == 0 && isLastOnEdge -> target = itemCount - 1
                }
            }

            recyclerView.smoothScrollToPosition(target)
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