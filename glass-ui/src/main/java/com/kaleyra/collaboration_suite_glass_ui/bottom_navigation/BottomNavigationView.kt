/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
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
import com.kaleyra.collaboration_suite_glass_ui.databinding.BandyerGlassBottomNavigationLayoutBinding

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

    private var binding: BandyerGlassBottomNavigationLayoutBinding =
        BandyerGlassBottomNavigationLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    /**
     * Set an on click listener on the swipe element. Needed for realwear glasses.
     *
     * @param callback function
     */
    fun setSwipeHorizontalOnClickListener(callback: () -> Unit) =
        binding.bandyerSwipe.setOnClickListener {
            callback.invoke()
        }

    /**
     * Set an on click listener on the tap element. Needed for realwear glasses.
     *
     * @param callback function
     */
    fun setTapOnClickListener(callback: () -> Unit) =
        binding.bandyerTap.setOnClickListener {
            callback.invoke()
        }

    /**
     * Set an on click listener on the swipe down element. Needed for realwear glasses.
     *
     * @param callback function
     */
    fun setSwipeDownOnClickListener(callback: () -> Unit) =
        binding.bandyerSwipeDown.setOnClickListener {
            callback.invoke()
        }

    /**
     * Show the swipe horizontal element
     */
    fun showSwipeHorizontalItem() {
        binding.bandyerSwipe.visibility = View.VISIBLE
    }

    /**
     * Hide the swipe horizontal element
     */
    fun hideSwipeHorizontalItem() {
        binding.bandyerSwipe.visibility = View.GONE
    }

    /**
     * Hide the tap element
     */
    fun hideTapItem() {
        binding.bandyerTap.visibility = View.GONE
    }
}