package com.bandyer.sdk_design.new_smartglass.contact.call_participant

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
class BandyerCallParticipantItem(val text: String): AbstractItem<BandyerCallParticipantItem.ViewHolder>() {

    override val layoutRes: Int
        get() = R.layout.bandyer_participant_item_layout

    override val type: Int
        get() = R.id.id_participants_item

    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(view: View): FastAdapter.ViewHolder<BandyerCallParticipantItem>(view) {

        private val binding: BandyerParticipantItemLayoutBinding = BandyerParticipantItemLayoutBinding.bind(view)

        override fun bindView(item: BandyerCallParticipantItem, payloads: List<Any>) {
            binding.bandyerText.text = item.text
        }

        override fun unbindView(item: BandyerCallParticipantItem) {
            binding.bandyerText.text = null
        }
    }
}