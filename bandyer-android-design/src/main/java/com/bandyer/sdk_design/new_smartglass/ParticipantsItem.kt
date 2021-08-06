package com.bandyer.sdk_design.new_smartglass

import android.view.View
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerParticipantsItemLayoutBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

class ParticipantsItem(val data: SmartGlassParticipantData): AbstractItem<ParticipantsItem.ViewHolder>() {

    override val layoutRes: Int
        get() = R.layout.bandyer_participants_item_layout

    override val type: Int
        get() = R.id.id_participants_item

    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(view: View): FastAdapter.ViewHolder<ParticipantsItem>(view) {

        private val binding: BandyerParticipantsItemLayoutBinding = BandyerParticipantsItemLayoutBinding.bind(view)

        override fun bindView(item: ParticipantsItem, payloads: List<Any>) {
            binding.bandyerText.text = item.data.name
        }

        override fun unbindView(item: ParticipantsItem) {
            binding.bandyerText.text = null
        }
    }
}