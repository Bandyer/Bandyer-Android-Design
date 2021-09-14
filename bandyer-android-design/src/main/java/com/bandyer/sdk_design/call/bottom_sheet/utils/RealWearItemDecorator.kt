package com.bandyer.sdk_design.call.bottom_sheet.utils

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.sdk_design.buttons.BandyerActionButton
import com.bandyer.sdk_design.call.buttons.BandyerAudioRouteActionButton
import com.bandyer.sdk_design.extensions.dp2px
import com.bandyer.sdk_design.extensions.getScreenSize
import com.bandyer.sdk_design.extensions.isRtl
import com.bandyer.sdk_design.extensions.scanForFragmentActivity
import com.bandyer.sdk_design.utils.isRealWearHTM1

/**
 * RealWearItemDecorator performs optimization for RealWear HMT-1 recyclerview navigation.
 */
@SuppressLint("NewApi")
class RealWearItemDecorator(val recyclerView: RecyclerView) : RecyclerView.ItemDecoration() {

    private val halfScreenDivider: Int by lazy { recyclerView.context.getScreenSize().x / 2 }
    private val itemDivider: Int by lazy { recyclerView.context.dp2px(32f) }

    private val tiltController by lazy {
        TiltController(recyclerView.context!!, object : TiltController.TiltListener {
            val tiltMultiplier = recyclerView.context!!.scanForFragmentActivity()!!.resources!!.displayMetrics!!.densityDpi / 5f
            override fun onTilt(x: Float, y: Float) = recyclerView.scrollBy((x * tiltMultiplier).toInt(), 0)
        })
    }

    init {
        if (isRealWearHTM1()) customizeRecyclerView(recyclerView)
    }

    private fun customizeRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context, LinearLayoutManager.HORIZONTAL, recyclerView.context.isRtl())

        val fragmentActivity = recyclerView.context!!.scanForFragmentActivity()!!
        fragmentActivity.lifecycle.addObserver(object : LifecycleObserver {

            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun onResume() = tiltController.requestAllSensors()

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            fun onPause() = tiltController.releaseAllSensors()

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDispose() = fragmentActivity.lifecycle.removeObserver(this)
        })
    }

    /**
     * @suppress
     */
    override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView) {
        if (!isRealWearHTM1()) return
        customizeAdapterItemView((parent.layoutManager as LinearLayoutManager).findViewByPosition(itemPosition)!!)
        addItemViewDividers(outRect, itemPosition)
    }

    private fun customizeAdapterItemView(adapterItemView: View) {
        recyclerView.adapter ?: return

        adapterItemView.layoutParams.width =
            if (recyclerView.adapter!!.itemCount > MAX_ITEM_ON_SCREEN) ViewGroup.LayoutParams.WRAP_CONTENT
            else recyclerView.context.getScreenSize().x / recyclerView.adapter!!.itemCount

        (adapterItemView as? BandyerActionButton)?.let {
            it.orientation = LinearLayout.HORIZONTAL
            it.label!!.isEnabled = false
        } ?: (adapterItemView as? BandyerAudioRouteActionButton)?.let {
            it.orientation = LinearLayout.HORIZONTAL
            it.label!!.isEnabled = false
        }
    }

    private fun addItemViewDividers(outRect: Rect, itemPosition: Int) = when {
        recyclerView.adapter!!.itemCount < MAX_ITEM_ON_SCREEN + 1 -> Unit
        itemPosition in 1..recyclerView.adapter!!.itemCount - 2 -> outRect.right = itemDivider
        itemPosition == 0 -> {
            outRect.left = if (recyclerView.adapter!!.itemCount >= MAX_ITEM_ON_SCREEN) halfScreenDivider else itemDivider
            outRect.right = itemDivider
        }
        // last position
        else -> outRect.right = if (recyclerView.adapter!!.itemCount >= MAX_ITEM_ON_SCREEN) halfScreenDivider else itemDivider
    }

    companion object {
        const val MAX_ITEM_ON_SCREEN = 4
    }
}