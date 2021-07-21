package com.bandyer.sdk_design.new_smartglass.menu

import android.content.Context
import android.view.View
import androidx.core.view.updatePaddingRelative
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// ***Known problem***
// The padding is wrong if both the first and the last item are visible
class CenterLinearLayoutManager constructor(
    context: Context, @RecyclerView.Orientation orientation: Int, reverseLayout: Boolean
) : LinearLayoutManager(context, orientation, reverseLayout) {

    private lateinit var recyclerView: RecyclerView

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        // always measure first item, its size determines starting offset
        // this must be done before super.onLayoutChildren
        if (childCount == 0 && state.itemCount > 0) {
            val firstChild = recycler.getViewForPosition(0)
            measureChildWithMargins(firstChild, 0, 0)
            recycler.recycleView(firstChild)
        }
        super.onLayoutChildren(recycler, state)
    }

    override fun measureChildWithMargins(child: View, widthUsed: Int, heightUsed: Int) {
        val lp = (child.layoutParams as RecyclerView.LayoutParams).absoluteAdapterPosition
        super.measureChildWithMargins(child, widthUsed, heightUsed)
        if (lp != 0 && lp != itemCount - 1) return
        // after determining first and/or last items size use it to update recycler view padding
        when (orientation) {
            RecyclerView.HORIZONTAL -> {
                val hPadding = ((width - (child.measuredWidth)) / 2).coerceAtLeast(0)
                recyclerView.updatePaddingRelative(start = hPadding, end = hPadding)
            }
            RecyclerView.VERTICAL -> {
                val vPadding = ((height - (child.measuredHeight)) / 2).coerceAtLeast(0)
                recyclerView.updatePaddingRelative(top = vPadding, bottom = vPadding)
            }
        }
    }

    override fun onAttachedToWindow(view: RecyclerView) {
        recyclerView = view
        super.onAttachedToWindow(view)
    }
}