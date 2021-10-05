package com.bandyer.video_android_glass_ui.call

import android.view.View
import com.bandyer.video_android_core_ui.extensions.ContextExtensions.dp2px
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
    class ViewHolder(val view: View) : FastAdapter.ViewHolder<FullScreenDialogItem>(view) {

        private val binding: BandyerGlassFullScreenDialogItemLayoutBinding =
            BandyerGlassFullScreenDialogItemLayoutBinding.bind(view)

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: FullScreenDialogItem, payloads: List<Any>) = with(binding) {
            val context = view.context
            when(absoluteAdapterPosition) {
                0 -> {
                    root.setPadding(context.dp2px(32f), 0, 0, 0)
                    bandyerText.text = item.text
                }
                bindingAdapter!!.itemCount - 1 -> {
                    root.setPadding(0, 0, context.dp2px(32f), 0)
                    bandyerText.text = context.resources.getString(R.string.bandyer_glass_ringing_pattern, item.text)
                }
            }
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: FullScreenDialogItem) = with(binding) {
            bandyerText.text = null
            root.setPadding(0,0,0,0)
        }
    }
}