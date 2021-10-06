package com.bandyer.video_android_glass_ui.call

import android.view.View
import com.bandyer.video_android_core_ui.extensions.ContextExtensions.dp2px
import com.bandyer.video_android_core_ui.extensions.ContextExtensions.isRTL
import com.bandyer.video_android_core_ui.extensions.ViewExtensions.setPaddingEnd
import com.bandyer.video_android_core_ui.extensions.ViewExtensions.setPaddingStart
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
        override fun bindView(item: FullScreenDialogItem, payloads: List<Any>): Unit = with(binding) {
            val context = itemView.context
            val isRTL = context.isRTL()
            val isFirstItem = absoluteAdapterPosition == 0
            val isLastItem = absoluteAdapterPosition == bindingAdapter!!.itemCount - 1

            bandyerText.text =
                when {
                    isFirstItem -> item.text
                    isRTL -> context.resources.getString(R.string.bandyer_glass_ringing_rtl_pattern, item.text)
                    else -> context.resources.getString(R.string.bandyer_glass_ringing_pattern, item.text)
                }

            root.apply {
                // Two separate whens because an item can be both first and last
                when {
                    isRTL && isFirstItem -> setPaddingEnd(context.dp2px(32f))
                    isFirstItem -> setPaddingStart(context.dp2px(32f))
                }

                when {
                    isRTL && isLastItem -> setPaddingStart(context.dp2px(32f))
                    isLastItem -> setPaddingEnd(context.dp2px(32f))
                }
            }
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: FullScreenDialogItem) = with(binding) {
            bandyerText.text = null
            root.setPadding(0, 0, 0, 0)
        }
    }
}