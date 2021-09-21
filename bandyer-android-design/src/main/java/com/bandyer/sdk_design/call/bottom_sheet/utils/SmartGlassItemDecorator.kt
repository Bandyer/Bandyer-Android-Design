/*
 * Copyright 2021-2022 Bandyer @ https://www.bandyer.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.bandyer.sdk_design.call.bottom_sheet.utils

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.buttons.BandyerActionButton
import com.bandyer.sdk_design.call.buttons.BandyerAudioRouteActionButton
import com.bandyer.sdk_design.call.widgets.BandyerCallActionWidget
import com.bandyer.sdk_design.extensions.dp2px
import com.bandyer.sdk_design.extensions.getScreenSize
import com.bandyer.sdk_design.extensions.isRtl
import com.bandyer.sdk_design.extensions.scanForFragmentActivity
import com.bandyer.sdk_design.extensions.setPaddingEnd
import com.bandyer.sdk_design.extensions.setPaddingStart
import com.bandyer.sdk_design.utils.AndroidDevice
import com.bandyer.sdk_design.utils.isRealWearHTM1

/**
 * SmartGlassItemDecorator performs optimization for RealWear HMT-1 recyclerview navigation.
 * @property recyclerView RecyclerView to be customized
 * @constructor
 */
@SuppressLint("NewApi")
class SmartGlassItemDecorator(val recyclerView: RecyclerView) : RecyclerView.ItemDecoration() {

    private val halfScreenDivider: Int by lazy { recyclerView.context.getScreenSize().x / 2 }
    private val itemDivider: Int by lazy { recyclerView.context.dp2px(16f) }

    private val tiltController by lazy {
        TiltController(recyclerView.context!!, object : TiltController.TiltListener {
            val tiltMultiplier = recyclerView.context!!.scanForFragmentActivity()!!.resources!!.displayMetrics!!.densityDpi / 5f
            override fun onTilt(x: Float, y: Float) = recyclerView.scrollBy((x * tiltMultiplier).toInt(), 0)
        }).apply {
            with(recyclerView.context!!.scanForFragmentActivity()!!) {
                lifecycle.addObserver(object : LifecycleObserver {

                    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
                    fun onResume() = requestAllSensors()

                    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                    fun onPause() = releaseAllSensors()

                    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                    fun onDestroy() = lifecycle.removeObserver(this)
                })
            }
        }
    }

    init {
        customizeRecyclerView(recyclerView)
    }

    private fun customizeRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context, LinearLayoutManager.HORIZONTAL, recyclerView.context.isRtl())
        if (AndroidDevice.CURRENT.isRealWearHTM1()) tiltController
    }

    /**
     * @suppress
     */
    override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView) {
        customizeAdapterItemView((parent.layoutManager as LinearLayoutManager).findViewByPosition(itemPosition)!!)
        addItemViewDividers(outRect, itemPosition)
    }

    private fun customizeAdapterItemView(adapterItemView: View) {
        recyclerView.adapter ?: return

        adapterItemView.layoutParams.width =
            if (shouldScrollItems()) ViewGroup.LayoutParams.WRAP_CONTENT
            else recyclerView.context.getScreenSize().x / recyclerView.adapter!!.itemCount

        adapterItemView.setPaddingStart(itemDivider)
        adapterItemView.setPaddingEnd(itemDivider)

        (adapterItemView as? BandyerActionButton)?.let {
            it.orientation = LinearLayout.HORIZONTAL
            it.label!!.isClickable = false
        } ?: (adapterItemView as? BandyerAudioRouteActionButton)?.let {
            it.orientation = LinearLayout.HORIZONTAL
            it.label!!.isClickable = false
        }
    }

    private fun addItemViewDividers(outRect: Rect, itemPosition: Int) = when {
        !shouldScrollItems()                                 -> Unit
        itemPosition == 0                                    ->
            outRect.left = if (shouldScrollItems()) halfScreenDivider - (recyclerView.layoutManager!!.findViewByPosition(0)!!.width / 2) else 0
        itemPosition == recyclerView.adapter!!.itemCount - 1 ->
            outRect.right = if (shouldScrollItems()) halfScreenDivider - (recyclerView.layoutManager!!.findViewByPosition(recyclerView.adapter!!.itemCount - 1)!!.width / 2) else 0
        else                                                 -> Unit
    }

    private fun shouldScrollItems() = recyclerView.adapter!!.itemCount > MAX_ITEM_ON_SCREEN + 1

    /**
     * RealWearItemDecoration object configuration
     */
    companion object {
        /**
         * Max number of items that should be considered when dividing the available space for adapter items.
         * When adapter items count is more than this constant, the adapter items' width will be set as wrap content.
         */
        const val MAX_ITEM_ON_SCREEN = 4
    }
}