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

package com.bandyer.video_android_phone_ui.smartglass.call.menu

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.bandyer.video_android_phone_ui.R
import com.bandyer.video_android_phone_ui.bottom_sheet.items.ActionItem
import com.bandyer.video_android_phone_ui.bottom_sheet.items.AdapterActionItem
import com.bandyer.video_android_phone_ui.call.bottom_sheet.items.CallAction
import com.bandyer.video_android_phone_ui.databinding.BandyerWidgetSmartglassesMenuLayoutBinding
import com.bandyer.video_android_phone_ui.extensions.isRtl
import com.bandyer.video_android_phone_ui.extensions.performTap
import com.bandyer.video_android_phone_ui.smartglass.call.menu.utils.MotionEventInterceptableView
import com.bandyer.video_android_phone_ui.smartglass.call.menu.utils.dispatchMotionEventToInterceptor
import com.bandyer.video_android_phone_ui.utils.isConfirmButton
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

/**
 * Layout used to represent a smart glass swipeable menu
 * @suppress
 * @property onSmartglassMenuSelectionListener OnGoogleGlassMenuItemSelectionListener?
 * @constructor
 */
class SmartGlassMenuLayout @kotlin.jvm.JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.bandyer_rootLayoutStyle) : ConstraintLayout(context, attrs, defStyleAttr), MotionEventInterceptableView<ConstraintLayout> {

    /**
     * Smart glass menu selection listener
     */
    interface OnSmartglassMenuSelectionListener {
        /**
         * Called when an item has been selected
         * @param item ActionItem selected item
         */
        fun onSelected(item: ActionItem)

        /**
         * Called when the smart glass swipeable menu has been dismissed
         */
        fun onDismiss()
    }

    /**
     * Smart glass menu action items
     */
    var items: List<CallAction> = listOf()
        set(value) {
            field = value
            fastItemAdapter.set((if (isRtl()) items.reversed() else items).map { AdapterActionItem(it) })
        }

    /**
     * Smart glass menu selection listener
     */
    var onSmartglassMenuSelectionListener: OnSmartglassMenuSelectionListener? = null

    private val fastItemAdapter = ItemAdapter<AdapterActionItem>()
    private val fastAdapter = FastAdapter.with(fastItemAdapter)

    private val binding: BandyerWidgetSmartglassesMenuLayoutBinding by lazy { BandyerWidgetSmartglassesMenuLayoutBinding.inflate(LayoutInflater.from(context), this) }

    private val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, isRtl()).apply {
        stackFromEnd = isRtl()
    }

    private val gestureDetector = GestureDetector(context, object : GestureDetector.OnGestureListener {
        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            getCurrentMenuItemIndex().takeIf { it != -1 }?.let { currentMenuItemIndex ->
                onSmartglassMenuSelectionListener?.onSelected(fastItemAdapter.getAdapterItem(currentMenuItemIndex).item)
            }
            return true
        }

        override fun onDown(e: MotionEvent?) = false
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float) = false
        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float) = false
        override fun onLongPress(e: MotionEvent?) = Unit
        override fun onShowPress(e: MotionEvent?) = Unit
    })

    private val snapHelper: SnapHelper = PagerSnapHelper()

    init {
        with(binding.bandyerSmartGlassMenuRecyclerview) {
            layoutManager = linearLayoutManager
            itemAnimator = null
            setHasFixedSize(true)
            adapter = fastAdapter
            snapHelper.attachToRecyclerView(this)
            binding.bandyerSmartGlassMenuIndicator.attachToRecyclerView(this)
        }
    }

    private fun getCurrentMenuItemIndex(): Int = snapHelper.findSnapView(linearLayoutManager)?.let { linearLayoutManager.getPosition(it) } ?: -1

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        val hasClicked = gestureDetector.onTouchEvent(ev)
        if (!hasClicked) dispatchMotionEventToInterceptor(ev)
        return super.onInterceptTouchEvent(ev)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean =
        if (event != null && event.action == KeyEvent.ACTION_UP && event.isConfirmButton()) performTap()
        else super.dispatchKeyEvent(event)
}