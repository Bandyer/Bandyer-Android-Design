package com.bandyer.video_android_glass_ui

import android.view.View
import android.view.ViewGroup
import com.bandyer.video_android_glass_ui.databinding.BandyerCallParticipantItemLayoutBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

class CallParticipantItem(val view: View): AbstractItem<CallParticipantItem.ViewHolder>() {

    /**
     * The layout for the given item
     */
    override val layoutRes: Int
        get() = R.layout.bandyer_call_participant_item_layout

    /**
     * The type of the Item. Can be a hardcoded INT, but preferred is a defined id
     */
    override val type: Int
        get() = R.id.id_glass_call_participant_item

    /**
     * This method returns the ViewHolder for our item, using the provided View.
     *
     * @return the ViewHolder for this Item
     */
    override fun getViewHolder(v: View) = ViewHolder(v)

    /**
     * @suppress
     */
    class ViewHolder(view: View): FastAdapter.ViewHolder<CallParticipantItem>(view) {

        private val binding = BandyerCallParticipantItemLayoutBinding.bind(view)
        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: CallParticipantItem, payloads: List<Any>) {
            (binding.root as ViewGroup).addView(item.view)
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: CallParticipantItem) {
            (binding.root as ViewGroup).removeView(item.view)
        }
    }
}