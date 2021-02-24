/*
 *  Copyright (C) 2020 Bandyer S.r.l. All Rights Reserved.
 *  See LICENSE.txt for licensing information
 */

package com.bandyer.sdk_design.smartglass.call.menu

import android.annotation.SuppressLint
import android.content.Context
import android.view.KeyEvent
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.bandyer.sdk_design.bottom_sheet.items.ActionItem
import com.bandyer.sdk_design.call.bottom_sheet.items.CallAction
import com.bandyer.sdk_design.databinding.BandyerWidgetSmartglassesMenuLayoutBinding
import com.bandyer.sdk_design.smartglass.call.menu.adapter.SmartGlassActionItemAdapter
import com.bandyer.sdk_design.utils.isConfirmButton

/**
 * Layout used to represent a smart glass swipeable menu
 * @suppress
 * @property onSmartglassMenuSelectionListener OnGoogleGlassMenuItemSelectionListener?
 * @constructor
 */
@SuppressLint("ViewConstructor")
class SmartGlassMenuLayout(
        context: Context,
        private val items: List<CallAction>) : ConstraintLayout(context, null, com.bandyer.sdk_design.R.attr.bandyer_smartGlassDialogMenuStyle) {

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
     * Smart glass menu selection listener
     */
    var onSmartglassMenuSelectionListener: OnSmartglassMenuSelectionListener? = null

//    private val fastAdapter: FastItemAdapter<AdapterActionItem> = FastItemAdapter()

    private var currentMenuItemIndex = 0

    private val binding: BandyerWidgetSmartglassesMenuLayoutBinding by lazy { BandyerWidgetSmartglassesMenuLayoutBinding.inflate(LayoutInflater.from(context), this) }

    private val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

    private val snapHelper: SnapHelper = PagerSnapHelper()

    init {
        binding.bandyerSmartglassesMenuRecyclerview.layoutManager = layoutManager
        binding.bandyerSmartglassesMenuRecyclerview.itemAnimator = null
        binding.bandyerSmartglassesMenuRecyclerview.setHasFixedSize(true)

//        binding.bandyerSmartglassesMenuRecyclerview.adapter = fastAdapter
//        fastAdapter.set(items.map { AdapterActionItem(it) })

        snapHelper.attachToRecyclerView(binding.bandyerSmartglassesMenuRecyclerview)


        binding.bandyerSmartglassesMenuRecyclerview.adapter = SmartGlassActionItemAdapter(object : SmartGlassActionItemAdapter.OnActionItemClickedListener {
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

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event != null && event.action == KeyEvent.ACTION_UP && event.isConfirmButton()) {
            snapHelper.findSnapView(layoutManager)?.performClick()
            return true
        }
        return super.dispatchKeyEvent(event)
    }
}