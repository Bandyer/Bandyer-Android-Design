package com.kaleyra.collaboration_suite_glass_ui

import android.annotation.SuppressLint
import android.view.View
import com.kaleyra.collaboration_suite.phonebox.Whiteboard
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassWhiteboardItemLayoutBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

/**
 * WhiteboardItem
 *
 * @param whiteboard Whiteboard
 * @constructor
 */
class WhiteboardItem(val whiteboard: Whiteboard) : AbstractItem<WhiteboardItem.ViewHolder>() {

    /**
     * @suppress
     */
    override var identifier: Long = whiteboard.hashCode().toLong()

    /**
     * @suppress
     */
    override val type: Int
        get() = R.id.id_glass_whiteboard_item

    /**
     * @suppress
     */
    override val layoutRes: Int
        get() = R.layout.kaleyra_glass_whiteboard_item_layout

    /**
     * @suppress
     */
    override fun getViewHolder(v: View) = ViewHolder(v)

    /**
     * @suppress
     */
    class ViewHolder(view: View) : FastAdapter.ViewHolder<WhiteboardItem>(view) {

        private var binding = KaleyraGlassWhiteboardItemLayoutBinding.bind(itemView)

        /**
         * Binds the data of this item onto the viewHolder
         */
        @SuppressLint("ClickableViewAccessibility")
        override fun bindView(item: WhiteboardItem, payloads: List<Any>) {
            item.whiteboard.view.value = binding.kaleyraWhiteboard.also {
                it.setOnTouchListener { _, _ -> true }
            }
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: WhiteboardItem) {
            binding.unbind()
            item.whiteboard.view.value = null
        }
    }
}