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

package com.kaleyra.collaboration_suite_glass_ui.chat.menu

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.chat.ChatAction
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassChatMenuItemLayoutBinding
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.ContextExtensions.getAttributeResourceId
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

/**
 * A menu option item in chat fragment.
 *
 * @property action The action chat action
 * @constructor
 */
internal class ChatMenuItem(val action: ChatAction): AbstractItem<ChatMenuItem.ViewHolder>() {

    /**
     * Set an unique identifier for the identifiable which do not have one set already
     */
    override var identifier: Long = action.hashCode().toLong()

    /**
     * The type of the Item. Can be a hardcoded INT, but preferred is a defined id
     */
    override val type: Int
        get() = action.hashCode()

    /**
     * The layout for the given item
     */
    override val layoutRes: Int
        get() = action.layoutRes

    /**
     * This method returns the ViewHolder for our item, using the provided View.
     *
     * @return the ViewHolder for this Item
     */
    override fun getViewHolder(v: View) = ViewHolder(v)

    override fun createView(ctx: Context, parent: ViewGroup?): View =
        LayoutInflater.from(
            ContextThemeWrapper(ctx, ctx.theme.getAttributeResourceId(action.styleAttr))
        ).inflate(layoutRes, parent, false).apply { id = action.viewId }

    /**
     *
     *
     * @property binding [@androidx.annotation.NonNull] KaleyraChatMenuItemLayoutBinding
     * @constructor
     */
    class ViewHolder(view: View): FastAdapter.ViewHolder<ChatMenuItem>(view) {

        private val binding = KaleyraGlassChatMenuItemLayoutBinding.bind(view)

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: ChatMenuItem, payloads: List<Any>) = Unit

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: ChatMenuItem) = Unit
    }
}