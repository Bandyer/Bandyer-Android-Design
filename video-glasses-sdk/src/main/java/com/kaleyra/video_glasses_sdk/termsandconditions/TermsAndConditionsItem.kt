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

package com.kaleyra.video_glasses_sdk.termsandconditions

import android.view.View
import com.kaleyra.video_glasses_sdk.R
import com.kaleyra.video_glasses_sdk.databinding.KaleyraGlassTermsAndConditionsItemLayoutBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

internal class TermsAndConditionsItem(val text: String) : AbstractItem<TermsAndConditionsItem.ViewHolder>() {

    override var identifier: Long = text.hashCode().toLong()
    /**
     * The layout for the given item
     */
    override val layoutRes: Int
        get() = R.layout.kaleyra_glass_terms_and_conditions_item_layout

    /**
     * The type of the Item. Can be a hardcoded INT, but preferred is a defined id
     */
    override val type: Int
        get() = R.id.id_glass_terms_and_conditions_item

    /**
     * This method returns the ViewHolder for our item, using the provided View.
     *
     * @return the ViewHolder for this Item
     */
    override fun getViewHolder(v: View) = ViewHolder(v)

    /**
     * The view holder for a chat item
     *
     * @constructor
     */
    class ViewHolder(view: View) : FastAdapter.ViewHolder<TermsAndConditionsItem>(view) {

        private val binding: KaleyraGlassTermsAndConditionsItemLayoutBinding = KaleyraGlassTermsAndConditionsItemLayoutBinding.bind(view)

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: TermsAndConditionsItem, payloads: List<Any>) {
            itemView.isClickable = false
            binding.kaleyraText.text = item.text
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: TermsAndConditionsItem) {
            itemView.isClickable = true
            binding.kaleyraText.text = null
        }
    }
}