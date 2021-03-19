package com.bandyer.sdk_design.filesharing

import android.view.View
import com.bandyer.sdk_design.R
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textview.MaterialTextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

class BandyerFileShareItem(var item: FileShareData) : AbstractItem<BandyerFileShareItem, BandyerFileShareItem.ViewHolder>() {

    override fun getType(): Int = R.id.bandyer_id_file_share_item

    override fun getLayoutRes(): Int =  R.layout.bandyer_file_share_item

    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(view: View) : FastAdapter.ViewHolder<BandyerFileShareItem>(view) {

        val fileType: BandyerFileTypeImageView = view.findViewById(R.id.bandyer_file_type)
        val fileSize: MaterialTextView = view.findViewById(R.id.bandyer_file_size)
        val action: BandyerFileShareActionButton = view.findViewById(R.id.bandyer_action)
        val fileName: MaterialTextView = view.findViewById(R.id.bandyer_file_name)
        val progressBar: LinearProgressIndicator = view.findViewById(R.id.bandyer_progress_bar)
        val operation: BandyerFileShareOpTypeImageView = view.findViewById(R.id.bandyer_operation)
        val user: MaterialTextView = view.findViewById(R.id.bandyer_user)
        val error: MaterialTextView = view.findViewById(R.id.bandyer_error)
        val progressText: MaterialTextView = view.findViewById(R.id.bandyer_progress_text)

        override fun bindView(item: BandyerFileShareItem, payloads: MutableList<Any>) {
            fileSize.text = "${item.item.fileSize}"
            user.text = item.item.sender
            error.text = "Upload failed - Retry"
        }

        override fun unbindView(item: BandyerFileShareItem) {
            fileSize.text = null
            user.text = null
            error.text = null
        }

    }

}