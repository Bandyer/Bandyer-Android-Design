/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_glasses_sdk

import android.annotation.SuppressLint
import android.view.View
import com.kaleyra.video.whiteboard.Whiteboard
import com.kaleyra.video_glasses_sdk.databinding.KaleyraGlassWhiteboardItemLayoutBinding
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
    override fun getViewHolder(v: View) = WhiteboardItem.ViewHolder(v)

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
            item.whiteboard.view.value = null
        }
    }
}