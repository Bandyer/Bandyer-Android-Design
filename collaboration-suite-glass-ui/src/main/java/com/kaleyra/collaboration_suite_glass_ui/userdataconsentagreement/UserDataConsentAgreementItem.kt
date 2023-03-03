package com.kaleyra.collaboration_suite_glass_ui.userdataconsentagreement

import android.view.View
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassUserDataConsentAgreementItemLayoutBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

internal class UserDataConsentAgreementItem(val text: String) : AbstractItem<UserDataConsentAgreementItem.ViewHolder>() {

    override var identifier: Long = text.hashCode().toLong()
    /**
     * The layout for the given item
     */
    override val layoutRes: Int
        get() = R.layout.kaleyra_glass_user_data_consent_agreement_item_layout

    /**
     * The type of the Item. Can be a hardcoded INT, but preferred is a defined id
     */
    override val type: Int
        get() = R.id.id_glass_user_data_consent_agreement_item

    /**
     * This method returns the ViewHolder for our item, using the provided View.
     *
     * @return the ViewHolder for this Item
     */
    override fun getViewHolder(v: View) = ViewHolder(v)

    /**
     * The view holder for a chat item
     *
     * @constructor
     */
    class ViewHolder(view: View) : FastAdapter.ViewHolder<UserDataConsentAgreementItem>(view) {

        private val binding: KaleyraGlassUserDataConsentAgreementItemLayoutBinding = KaleyraGlassUserDataConsentAgreementItemLayoutBinding.bind(view)

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: UserDataConsentAgreementItem, payloads: List<Any>) {
            itemView.isClickable = false
            binding.kaleyraText.text = item.text
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: UserDataConsentAgreementItem) {
            itemView.isClickable = true
            binding.kaleyraText.text = null
        }
    }
}