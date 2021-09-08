package com.bandyer.sdk_design.new_smartglass

import android.view.View
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerParticipantItemLayoutBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

/**
 * A participant item.
 *
 * @property text The participant's name
 * @constructor
 */
class BandyerParticipantItem(val text: String): AbstractItem<BandyerParticipantItem.ViewHolder>() {

    override val layoutRes: Int
        get() = R.layout.bandyer_participant_item_layout

    override val type: Int
        get() = R.id.id_participants_item

    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(view: View): FastAdapter.ViewHolder<BandyerParticipantItem>(view) {

        private val binding: BandyerParticipantItemLayoutBinding = BandyerParticipantItemLayoutBinding.bind(view)

        override fun bindView(item: BandyerParticipantItem, payloads: List<Any>) {
            binding.bandyerText.text = item.text
        }

        override fun unbindView(item: BandyerParticipantItem) {
            binding.bandyerText.text = null
        }
    }
}