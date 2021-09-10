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
import com.bandyer.sdk_design.extensions.isRtl
import com.bandyer.sdk_design.extensions.scanForFragmentActivity
import com.bandyer.sdk_design.utils.isRealWearHTM1

/**
 * RealWearItemDecorator performs optimization for RealWear HMT-1 recyclerview navigation.
 */
@SuppressLint("NewApi")
class RealWearItemDecorator : RecyclerView.ItemDecoration() {

    private var recyclerView: RecyclerView? = null

    private val tiltController by lazy {
        TiltController(recyclerView!!.context!!, object : TiltController.TiltListener {
            val tiltMultiplier = recyclerView!!.context!!.scanForFragmentActivity()!!.resources!!.displayMetrics!!.densityDpi / 5f
            override fun onTilt(x: Float, y: Float) = recyclerView!!.scrollBy((x * tiltMultiplier).toInt(), 0)
        })
    }

    /**
     * @suppress
     */
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        if (!isRealWearHTM1() || recyclerView != null) return
        customizeRecyclerView(parent)
    }

    private fun customizeRecyclerView(recyclerView: RecyclerView) {
        this@RealWearItemDecorator.recyclerView = recyclerView

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

        val horizontalSpacing = parent.context.dp2px(32f)
        outRect.left = horizontalSpacing
        if (itemPosition != parent.adapter!!.itemCount - 1) return
        outRect.right = horizontalSpacing
    }

    private fun customizeAdapterItemView(adapterItemView: View) {
        adapterItemView.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT

        (adapterItemView as? BandyerActionButton)?.let {
            it.orientation = LinearLayout.HORIZONTAL
            it.label!!.isEnabled = false
        } ?: (adapterItemView as? BandyerAudioRouteActionButton)?.let{
            it.orientation = LinearLayout.HORIZONTAL
            it.label!!.isEnabled = false
        }
    }
}