package com.bandyer.video_android_glass_ui.call.participants

import android.view.View
import com.bandyer.video_android_glass_ui.model.CallParticipant
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.UserDetails
import com.bandyer.video_android_glass_ui.UserDetailsWrapper
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassParticipantItemLayoutBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import kotlinx.coroutines.flow.StateFlow

/**
 * A participant item.
 *
 * @property participant The call participant
 * @property callUserDetails The call's userDetails
 * @constructor
 */
internal class CallParticipantItem(val participant: CallParticipant, val userDetailsWrapper: StateFlow<UserDetailsWrapper>): AbstractItem<CallParticipantItem.ViewHolder>() {

    /**
     * Set an unique identifier for the identifiable which do not have one set already
     */
    override var identifier: Long = participant.userAlias.hashCode().toLong()

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

        private val binding: BandyerGlassParticipantItemLayoutBinding = BandyerGlassParticipantItemLayoutBinding.bind(view)

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: CallParticipantItem, payloads: List<Any>) {
            val userDetailsWrapper = item.userDetailsWrapper.value
            val userAlias = item.participant.userAlias
            val userDetails = userDetailsWrapper.data.firstOrNull { it.userAlias == userAlias } ?: UserDetails(userAlias)
            binding.bandyerText.text = userDetailsWrapper.formatters.callFormatter.singleDetailsFormat.invoke(userDetails)
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: CallParticipantItem) {
            binding.unbind()
            binding.bandyerText.text = null
        }
    }
}