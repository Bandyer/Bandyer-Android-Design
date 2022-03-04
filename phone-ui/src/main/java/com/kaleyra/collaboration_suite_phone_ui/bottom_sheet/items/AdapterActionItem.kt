/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.items

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

/**
 * View holder for the action items
 * @suppress
 */
class AdapterActionItem(var item: ActionItem) : AbstractItem<AdapterActionItem.Holder>() {

    override fun getViewHolder(v: View): Holder = Holder(v)

    override val layoutRes= item.viewLayoutRes
    override val type: Int = item.hashCode()
    override var identifier: Long = item.hashCode().toLong()

    override fun createView(ctx: Context, parent: ViewGroup?): View = LayoutInflater.from(ContextThemeWrapper(ctx, item.viewStyle)).inflate(layoutRes, parent, false).apply {
        id = item.viewId
    }

    override fun equals(other: Any?): Boolean = item == other

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + item.hashCode()
        result = 31 * result + layoutRes
        result = 31 * result + type
        result = 31 * result + identifier.hashCode()
        return result
    }

    class Holder internal constructor(view: View) : FastAdapter.ViewHolder<AdapterActionItem>(view) {

        override fun bindView(adapterItem: AdapterActionItem, payloads: List<Any>) {
            adapterItem.item.itemView = itemView
            adapterItem.item.onReady()
        }

        override fun unbindView(item: AdapterActionItem) {
            item.item.itemView = null
        }
    }
}