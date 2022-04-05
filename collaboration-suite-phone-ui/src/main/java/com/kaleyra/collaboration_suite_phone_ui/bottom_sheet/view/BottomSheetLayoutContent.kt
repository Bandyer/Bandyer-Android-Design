/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.view

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.getActivity
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.buttons.KaleyraLineButton
import com.kaleyra.collaboration_suite_phone_ui.databinding.KaleyraBottomSheetWidgetLayoutBinding
import com.kaleyra.collaboration_suite_phone_ui.utils.systemviews.SystemViewLayoutOffsetListener
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView

/**
 * The bottomSheet Layout
 * @author kristiyan
 */
class BottomSheetLayoutContent @kotlin.jvm.JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

    /**
     * Title of the bottomSheet
     */
    var titleView: MaterialTextView? = null

    /**
     * Header line of the bottomSheet
     */
    var lineView: KaleyraLineButton? = null

    /**
     * RecyclerView of the bottomSheet
     */
    var recyclerView: RecyclerView? = null

    /**
     * Background of the bottomSheet
     */
    var backgroundView: MaterialCardView? = MaterialCardView(context, attrs, R.attr.materialCardViewStyle)

    /**
     * Height of the navigation bar
     */
    var navigationBarHeight: Int = 0
        set(value) {
            field = value
            updateBackgroundView()
        }

    private val binding: KaleyraBottomSheetWidgetLayoutBinding by lazy { KaleyraBottomSheetWidgetLayoutBinding.inflate(LayoutInflater.from(context), this) }

    init {
        id = R.id.kaleyra_id_bottom_sheet_layout_content
        titleView = binding.kaleyraTitle
        lineView = binding.kaleyraLine
        recyclerView = binding.kaleyraRecyclerView
        recyclerView!!.overScrollMode = View.OVER_SCROLL_NEVER
        recyclerView!!.isFocusable = false
        recyclerView!!.isFocusableInTouchMode = false
        recyclerView!!.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) recyclerView?.layoutManager?.findViewByPosition(0)?.requestFocus()
        }
    }

    /**
     * Set visibility of bottom sheet layout and its background view
     */
    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        backgroundView?.visibility = visibility
    }

    /**
     * Called when device configuration changes
     * @param newConfig new device configuration
     */
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        post { updateBackgroundView() }
        SystemViewLayoutOffsetListener.getOffsets(context.getActivity()!!)
    }

    /**
     * Update background view position and height
     */
    fun updateBackgroundView() {
        val backgroundHeight = this.height + this.paddingBottom + this.paddingTop + navigationBarHeight
        if (backgroundView?.translationY == y && backgroundView?.layoutParams?.height == backgroundHeight) return
        backgroundView?.translationY = y
        backgroundView?.layoutParams?.height = backgroundHeight
        backgroundView?.requestLayout()
    }
}


