package com.bandyer.video_android_glass_ui.contact.details

import android.view.View
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.databinding.BandyerContactDetailsItemLayoutBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

/**
 * A participant details item.
 *
 * @property text The action text
 * @constructor
 */
class BandyerContactDetailsItem(val text: String): AbstractItem<BandyerContactDetailsItem.ViewHolder>() {

    override val layoutRes: Int
        get() = R.layout.bandyer_contact_details_item_layout

    override val type: Int
        get() = R.id.id_glass_contact_details_item

    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(view: View): FastAdapter.ViewHolder<BandyerContactDetailsItem>(view) {

        private val binding = BandyerContactDetailsItemLayoutBinding.bind(view)

        override fun bindView(item: BandyerContactDetailsItem, payloads: List<Any>) {
            binding.bandyerText.text = item.text
        }

        override fun unbindView(item: BandyerContactDetailsItem) {
            binding.bandyerText.text = null
        }
    }
}