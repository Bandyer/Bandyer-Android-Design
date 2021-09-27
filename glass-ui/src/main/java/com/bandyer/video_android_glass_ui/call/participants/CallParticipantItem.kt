package com.bandyer.video_android_glass_ui.call.participants

import android.view.View
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.databinding.BandyerParticipantItemLayoutBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

/**
 * A participant item.
 *
 * @property text The participant's name
 * @constructor
 */
class CallParticipantItem(val text: String): AbstractItem<CallParticipantItem.ViewHolder>() {

    /**
     * The layout for the given item
     */
    override val layoutRes: Int
        get() = R.layout.bandyer_glass_participant_item_layout

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
    class ViewHolder(view: View): FastAdapter.ViewHolder<CallParticipantItem>(view) {

        private val binding: BandyerParticipantItemLayoutBinding = BandyerParticipantItemLayoutBinding.bind(view)

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: CallParticipantItem, payloads: List<Any>) {
            binding.bandyerText.text = item.text
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: CallParticipantItem) {
            binding.bandyerText.text = null
        }
    }
}