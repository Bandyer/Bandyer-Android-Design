package com.kaleyra.collaboration_suite_glass_ui

import android.view.View
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassWhiteboardItemLayoutBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

class WhiteboardItem : AbstractItem<WhiteboardItem.ViewHolder>() {

    override val type: Int
        get() = R.id.id_glass_whiteboard_item

    override val layoutRes: Int
        get() = R.layout.kaleyra_glass_whiteboard_item_layout

    override fun getViewHolder(v: View) = ViewHolder(v)

    /**
     * @suppress
     */
    class ViewHolder(view: View) : FastAdapter.ViewHolder<WhiteboardItem>(view) {

        private var binding = KaleyraGlassWhiteboardItemLayoutBinding.bind(itemView)

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: WhiteboardItem, payloads: List<Any>) {
            binding.kaleyraWebView.loadUrl("https://sandbox.bandyer.com/connect/mobile-whiteboard")
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: WhiteboardItem) {
            binding.unbind()
        }
    }
}