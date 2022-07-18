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

package com.kaleyra.collaboration_suite_glass_ui.call.adapter_items

import android.view.View
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassParticipantItemLayoutBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

internal data class ParticipantItemData(
    val userId: String,
    val userDescription: String
)

/**
 * A participant item.
 *
 * @property data The call participant's data
 * @constructor
 */
internal class ParticipantItem(val data: ParticipantItemData): AbstractItem<ParticipantItem.ViewHolder>() {

    /**
     * Set an unique identifier for the identifiable which do not have one set already
     */
    override var identifier: Long = data.userId.hashCode().toLong()

    /**
     * The layout for the given item
     */
    override val layoutRes: Int
        get() = R.layout.kaleyra_glass_participant_item_layout

    /**
     * The type of the Item. Can be a hardcoded INT, but preferred is a defined id
     */
    override val type: Int
        get() = R.id.id_glass_participants_item

    /**
     * This method returns the ViewHolder for our item, using the provided View.
     *
     * @return the ViewHolder for this Item
     */
    override fun getViewHolder(v: View) = ViewHolder(v)

    /**
     * View holder for the participant item
     *
     * @constructor
     */
    class ViewHolder(view: View): FastAdapter.ViewHolder<ParticipantItem>(view) {

        private val binding: KaleyraGlassParticipantItemLayoutBinding = KaleyraGlassParticipantItemLayoutBinding.bind(view)

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: ParticipantItem, payloads: List<Any>) {
            binding.kaleyraText.text = item.data.userDescription
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: ParticipantItem) {
            binding.kaleyraText.text = null
        }
    }
}