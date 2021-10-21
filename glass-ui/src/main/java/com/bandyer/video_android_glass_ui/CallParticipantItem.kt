package com.bandyer.video_android_glass_ui

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.*
import com.bandyer.video_android_glass_ui.databinding.BandyerCallParticipantItemLayoutBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

class CallParticipantItem(val streamId: String, val streamView: View, val lifecycle: Lifecycle) : AbstractItem<CallParticipantItem.ViewHolder>() {

    /**
     * Set an unique identifier for the identifiable which do not have one set already
     */
    override var identifier: Long = streamId.hashCode().toLong()

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
    class ViewHolder(view: View) : FastAdapter.ViewHolder<CallParticipantItem>(view), LifecycleEventObserver {

        private var binding = BandyerCallParticipantItemLayoutBinding.bind(itemView)

        private var item: CallParticipantItem? = null

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: CallParticipantItem, payloads: List<Any>) {
            this.item = item
            item.lifecycle.addObserver(this)
            (binding.bandyerWrapper as ViewGroup).addView(item.streamView.also { it.id = View.generateViewId() })
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: CallParticipantItem) {
            (binding.bandyerWrapper as ViewGroup).removeView(item.streamView)
            item.lifecycle.removeObserver(this)
            binding.unbind()
            this.item = null
        }

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if(event == Lifecycle.Event.ON_DESTROY)
                unbindView(item!!)
        }
    }
}