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
import android.widget.LinearLayout
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassBottomNavigationItemLayoutBinding
import java.util.*

/**
 * Bottom action bar item view, it describes an action the user performs on a given gesture
 * It's made of:
 * - gesture icon
 * - gesture text
 * - action text
 *
 * @constructor
 */
internal class BottomNavigationItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = KaleyraGlassBottomNavigationItemLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    override fun setOnClickListener(l: OnClickListener?) {
        binding.root.setOnClickListener(l)
    }

    fun setSecondaryOnClickListener(l: OnClickListener?) {
        binding.kaleyraGestureIcon.setOnClickListener(l)
    }

    fun capitalizeActionText() {
        val text = binding.kaleyraActionText.text.toString()
        val capitalizedText = text.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        binding.kaleyraActionText.text = capitalizedText
    }

    fun setActionText(text: String) {
        binding.kaleyraActionText.text = text
    }

    fun setRootContentDescription(text: String) {
        binding.root.contentDescription = text
    }
}