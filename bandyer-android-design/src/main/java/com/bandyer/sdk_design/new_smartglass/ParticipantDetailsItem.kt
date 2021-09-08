package com.bandyer.sdk_design.new_smartglass

import android.view.View
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerParticipantDetailsItemLayoutBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

/**
 * A participant details item.
 *
 * @property text The action text
 * @constructor
 */
class ParticipantDetailsItem(val text: String): AbstractItem<ParticipantDetailsItem.ViewHolder>() {

    override val layoutRes: Int
        get() = R.layout.bandyer_participant_details_item_layout

    override val type: Int
        get() = R.id.id_participant_details_item

    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(view: View): FastAdapter.ViewHolder<ParticipantDetailsItem>(view) {

        private val binding: BandyerParticipantDetailsItemLayoutBinding = BandyerParticipantDetailsItemLayoutBinding.bind(view)

        override fun bindView(item: ParticipantDetailsItem, payloads: List<Any>) {
            binding.bandyerText.text = item.text
        }

        override fun unbindView(item: ParticipantDetailsItem) {
            binding.bandyerText.text = null
        }
    }
}