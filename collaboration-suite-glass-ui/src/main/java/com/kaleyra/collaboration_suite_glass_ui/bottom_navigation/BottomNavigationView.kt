/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_glass_ui.bottom_navigation

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.view.ContextThemeWrapper
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassBottomNavigationLayoutBinding
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.ContextExtensions.getAttributeResourceId

/**
 * Bottom action bar view, it describes the actions the user performs
 * It is made of three inline [BottomNavigationItemView]
 *
 * @constructor
 */
internal class BottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var binding: KaleyraGlassBottomNavigationLayoutBinding

    init {
        val themeResId = context.theme.getAttributeResourceId(
            if (DeviceUtils.isRealWear) R.attr.kaleyra_bottomNavigationVoiceStyle else R.attr.bottomNavigationStyle
        )
        binding = KaleyraGlassBottomNavigationLayoutBinding.inflate(
            LayoutInflater.from(context).cloneInContext(ContextThemeWrapper(context, themeResId)),
            this,
            true
        ).apply {
            if (!DeviceUtils.isRealWear) return@apply
            kaleyraFirstItem.capitalizeActionText()
            kaleyraSecondItem.capitalizeActionText()
            kaleyraThirdItem.capitalizeActionText()
        }
    }

    /**
     * Set on click listeners on the first element. It also has an optional secondary callback for a secondary command. Needed for realwear glasses.
     *
     * @param callback function
     */
    fun setFirstItemListeners(callback: () -> Unit, secondaryCallBack: (() -> Unit)? = null) =
        with(binding.kaleyraFirstItem) {
            setOnClickListener { callback.invoke() }
            secondaryCallBack?.also { cb -> setSecondaryOnClickListener { cb.invoke() } }
        }


    /**
     * Set an on click listener on the second element. Needed for realwear glasses.
     *
     * @param callback function
     */
    fun setSecondItemListener(callback: () -> Unit) =
        binding.kaleyraSecondItem.setOnClickListener {
            callback.invoke()
        }

    /**
     * Set an on click listener on the third element. Needed for realwear glasses.
     *
     * @param callback function
     */
    fun setThirdItemListener(callback: () -> Unit) =
        binding.kaleyraThirdItem.setOnClickListener {
            callback.invoke()
        }


    /**
     * Set the action text on the second element
     *
     * @param text String
     */
    fun setSecondItemActionText(text: String) {
        binding.kaleyraSecondItem.setActionText(text)
    }

    /**
     * Set the action text on the third element
     *
     * @param text String
     */
    fun setThirdItemActionText(text: String) {
        binding.kaleyraThirdItem.setActionText(text)
    }

    /**
     * Set the content description on the second element. Needed for realwear glasses commands.
     *
     * @param text String
     */
    fun setSecondItemContentDescription(text: String) {
        binding.kaleyraSecondItem.setRootContentDescription(text)
    }

    /**
     * Set the content description on the third element. Needed for realwear glasses commands.
     *
     * @param text String
     */
    fun setThirdItemContentDescription(text: String) {
        binding.kaleyraThirdItem.setRootContentDescription(text)
    }

    /**
     * Show the first item
     */
    fun showFirstItem() {
        binding.kaleyraFirstItem.visibility = View.VISIBLE
    }

    /**
     * Hide the first item
     */
    fun hideFirstItem() {
        binding.kaleyraFirstItem.visibility = View.GONE
    }

    /**
     * Hide the second item
     */
    fun hideSecondItem() {
        binding.kaleyraSecondItem.visibility = View.GONE
    }

    /**
     * Show second item
     */
    fun showSecondItem() {
        binding.kaleyraSecondItem.visibility = View.VISIBLE
    }

}