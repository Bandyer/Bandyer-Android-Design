package com.bandyer.video_android_glass_ui.call

import android.view.View
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFullScreenDialogItemLayoutBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

class FullScreenDialogItem(val text: String) : AbstractItem<FullScreenDialogItem.ViewHolder>() {

    /**
     * The layout for the given item
     */
    override val layoutRes: Int
        get() = R.layout.bandyer_glass_full_screen_dialog_item_layout

    /**
     * The type of the Item. Can be a hardcoded INT, but preferred is a defined id
     */
    override val type: Int
        get() = R.id.id_glass_full_screen_dialog_item

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
    class ViewHolder(view: View) : FastAdapter.ViewHolder<FullScreenDialogItem>(view) {

        private val binding: BandyerGlassFullScreenDialogItemLayoutBinding =
            BandyerGlassFullScreenDialogItemLayoutBinding.bind(view)

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: FullScreenDialogItem, payloads: List<Any>) {
            binding.bandyerText.text = " ${item.text},"
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: FullScreenDialogItem) {
            binding.bandyerText.text = item.text
        }
    }
}