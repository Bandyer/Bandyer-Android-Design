/*
 *  Copyright (C) 2020 Bandyer S.r.l. All Rights Reserved.
 *  See LICENSE.txt for licensing information
 */

package com.bandyer.sdk_design.call.smartglass

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.bandyer.sdk_design.bottom_sheet.items.ActionItem
import com.bandyer.sdk_design.bottom_sheet.items.AdapterActionItem
import com.bandyer.sdk_design.call.smartglass.menu.adapter.SmartglassActionItemAdapter
import com.bandyer.sdk_design.databinding.BandyerWidgetSmartglassesMenuLayoutBinding
import com.bandyer.sdk_design.utils.GlassGestureDetector
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter

/**
 * Layout used to represent a smartglass swipeable menu
 * @suppress
 * @property items List<ActionItem> the items to be listed in the menu
 * @property fastAdapter FastItemAdapter<AdapterActionItem> the items adapter
 * @property currentMenuItemIndex Int the current menu item index
 * @property onSmartglassMenuSelectionListener OnGoogleGlassMenuItemSelectionListener?
 * @property glassGestureDetector GlassGestureDetector
 * @constructor
 */
@SuppressLint("ViewConstructor")
class SmartglassesMenuLayout(
        context: Context,
        private val items: List<ActionItem>) : ConstraintLayout(context, null,  com.bandyer.sdk_design.R.attr.bandyer_smartglassMenuStyle) {

    /**
     * Smartglass menu selection listener
     */
    interface OnSmartglassMenuSelectionListener {
        /**
         * Called when an item has been selected
         * @param item ActionItem selected item
         */
        fun onSelected(item: ActionItem)

        /**
         * Called when the smartglass swipeable menu has been dismissed
         */
        fun onDismiss()
    }

    /**
     * Smartglass menu selection listener
     */
    var onSmartglassMenuSelectionListener: OnSmartglassMenuSelectionListener? = null

    private val fastAdapter: FastItemAdapter<AdapterActionItem> = FastItemAdapter()

    private var currentMenuItemIndex = 0

    private val binding: BandyerWidgetSmartglassesMenuLayoutBinding by lazy { BandyerWidgetSmartglassesMenuLayoutBinding.inflate(LayoutInflater.from(context), this) }

    private val glassGestureDetector: GlassGestureDetector = GlassGestureDetector(
            context,
            object : GlassGestureDetector.OnGestureListener {
                override fun onGesture(gesture: GlassGestureDetector.Gesture?): Boolean {
                    return when (gesture) {
                        GlassGestureDetector.Gesture.SWIPE_DOWN -> {
                            val currentOnGoogleGlassMenuItemSelectionListener = onSmartglassMenuSelectionListener
                            dispose()
                            currentOnGoogleGlassMenuItemSelectionListener?.onDismiss()
                            true
                        }
                        else -> false
                    }
                }
            })

    init {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.bandyerSmartglassesMenuRecyclerview.layoutManager = layoutManager
        binding.bandyerSmartglassesMenuRecyclerview.itemAnimator = null
        binding.bandyerSmartglassesMenuRecyclerview.setHasFixedSize(true)

//        binding.bandyerSmartglassesMenuRecyclerview.adapter = fastAdapter
//        fastAdapter.set(items.map { AdapterActionItem(it) })

        val snapHelper: SnapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.bandyerSmartglassesMenuRecyclerview)


        binding.bandyerSmartglassesMenuRecyclerview.adapter = SmartglassActionItemAdapter(object : SmartglassActionItemAdapter.OnActionItemClickedListener {
            override fun onActionItemClicked(actionItem: ActionItem) {
                onSmartglassMenuSelectionListener?.onSelected(items[currentMenuItemIndex])
            }
        }).apply {
            setHasStableIds(true)
            setItems(items)
        }

        binding.bandyerSmartglassesMenuRecyclerviewIndicator.attachToRecyclerView(binding.bandyerSmartglassesMenuRecyclerview)

        binding.bandyerSmartglassesMenuRecyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState != RecyclerView.SCROLL_STATE_IDLE) return
                val foundView = snapHelper.findSnapView(layoutManager) ?: return
                currentMenuItemIndex = layoutManager.getPosition(foundView)
            }
        })
    }

    private fun dispose() {
        onSmartglassMenuSelectionListener = null
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        glassGestureDetector.onTouchEvent(event)
        return super.dispatchTouchEvent(event)
    }
}