/*
 *  Copyright (C) 2020 Bandyer S.r.l. All Rights Reserved.
 *  See LICENSE.txt for licensing information
 */

package com.bandyer.sdk_design.smartglass.call.menu

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.bottom_sheet.items.ActionItem
import com.bandyer.sdk_design.bottom_sheet.items.AdapterActionItem
import com.bandyer.sdk_design.call.bottom_sheet.items.CallAction
import com.bandyer.sdk_design.databinding.BandyerWidgetSmartglassesMenuLayoutBinding
import com.bandyer.sdk_design.extensions.isRtl
import com.bandyer.sdk_design.smartglass.call.menu.utils.MotionEventInterceptableViewGroup
import com.bandyer.sdk_design.smartglass.call.menu.utils.motionEventInterceptor
import com.bandyer.sdk_design.utils.isConfirmButton
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter

/**
 * Layout used to represent a smart glass swipeable menu
 * @suppress
 * @property onSmartglassMenuSelectionListener OnGoogleGlassMenuItemSelectionListener?
 * @constructor
 */
class SmartGlassMenuLayout @kotlin.jvm.JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.bandyer_rootLayoutStyle) : ConstraintLayout(context, attrs, defStyleAttr), MotionEventInterceptableViewGroup<ConstraintLayout> {

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
    var items: List<CallAction>? = null
        set(value) {
            field = value
            value ?: return
            fastAdapter.set((if (context.isRtl()) items!!.reversed() else items!!).map { AdapterActionItem(it) })
        }

    /**
     * Smart glass menu selection listener
     */
    var onSmartglassMenuSelectionListener: OnSmartglassMenuSelectionListener? = null

    private val fastAdapter: FastItemAdapter<AdapterActionItem> = FastItemAdapter()

    private var currentMenuItemIndex = 0

    private val binding: BandyerWidgetSmartglassesMenuLayoutBinding by lazy { BandyerWidgetSmartglassesMenuLayoutBinding.inflate(LayoutInflater.from(context), this) }

    private val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, context.isRtl()).apply {
        stackFromEnd = context.isRtl()
    }

    private val snapHelper: SnapHelper = PagerSnapHelper()

    private val gestureDetector = GestureDetector(context, object : GestureDetector.OnGestureListener {
        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            onSmartglassMenuSelectionListener?.onSelected(items!![currentMenuItemIndex])
            return true
        }

        override fun onDown(e: MotionEvent?) = false
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float) = false
        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float) = false
        override fun onLongPress(e: MotionEvent?) = Unit
        override fun onShowPress(e: MotionEvent?) = Unit
    })

    init {
        binding.bandyerSmartGlassMenuRecyclerview.layoutManager = layoutManager
        binding.bandyerSmartGlassMenuRecyclerview.itemAnimator = null
        binding.bandyerSmartGlassMenuRecyclerview.setHasFixedSize(true)
        binding.bandyerSmartGlassMenuRecyclerview.adapter = fastAdapter

        snapHelper.attachToRecyclerView(binding.bandyerSmartGlassMenuRecyclerview)

        binding.bandyerSmartGlassMenuIndicator.attachToRecyclerView(binding.bandyerSmartGlassMenuRecyclerview)

        binding.bandyerSmartGlassMenuRecyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState != RecyclerView.SCROLL_STATE_IDLE) return
                val foundView = snapHelper.findSnapView(layoutManager) ?: return
                currentMenuItemIndex = layoutManager.getPosition(foundView)
            }
        })
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event != null && event.action == KeyEvent.ACTION_UP && event.isConfirmButton()) {
            snapHelper.findSnapView(layoutManager)?.performClick()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onDetachedFromWindow() {
        motionEventInterceptor = null
        super.onDetachedFromWindow()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        val hasClicked = gestureDetector.onTouchEvent(ev)
        if (!hasClicked) dispatchMotionEventToInterceptor(ev)
        return super.onInterceptTouchEvent(ev)
    }
}