package com.bandyer.sdk_design.filesharing

import android.view.View
import com.bandyer.sdk_design.R
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textview.MaterialTextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

class BandyerFileShareItem(var data: FileShareData) : AbstractItem<BandyerFileShareItem, BandyerFileShareItem.ViewHolder>() {

    override fun getType(): Int = R.id.bandyer_id_file_share_item

    override fun getLayoutRes(): Int =  R.layout.bandyer_file_share_item

    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(val view: View) : FastAdapter.ViewHolder<BandyerFileShareItem>(view) {

        val fileType: BandyerFileTypeImageView = view.findViewById(R.id.bandyer_file_type)
        val fileSize: MaterialTextView = view.findViewById(R.id.bandyer_file_size)
        val action: BandyerFileShareActionButton = view.findViewById(R.id.bandyer_action)
        val fileName: MaterialTextView = view.findViewById(R.id.bandyer_file_name)
        val progressBar: LinearProgressIndicator = view.findViewById(R.id.bandyer_progress_bar)
        val operation: BandyerFileShareOpTypeImageView = view.findViewById(R.id.bandyer_operation)
        val user: MaterialTextView = view.findViewById(R.id.bandyer_username)
        val error: MaterialTextView = view.findViewById(R.id.bandyer_error)
        val progressText: MaterialTextView = view.findViewById(R.id.bandyer_progress_text)

        override fun bindView(item: BandyerFileShareItem, payloads: MutableList<Any>) {
            fileSize.text = "${item.data.fileSize}"
            user.text = item.data.sender
            error.text = view.context.resources.getString(R.string.bandyer_fileshare_error_message)
            fileName.text = item.data.fileName
            operation.type = if(item.data.isUpload) BandyerFileShareOpTypeImageView.Type.UPLOAD else BandyerFileShareOpTypeImageView.Type.DOWNLOAD
            progressBar.progress = item.data.progress
            fileType.type = when(item.data.fileType) {
                FileType.FILE -> BandyerFileTypeImageView.Type.FILE
                FileType.MEDIA -> BandyerFileTypeImageView.Type.MEDIA
                FileType.ARCHIVE -> BandyerFileTypeImageView.Type.ARCHIVE
            }
            progressText.text =  view.context.resources.getString(R.string.bandyer_fileshare_progress, item.data.progress)
        }

        override fun unbindView(item: BandyerFileShareItem) {
            fileSize.text = null
            user.text = null
            error.text = null
            fileName.text = null
            operation.type = null
            progressBar.progress = 0
            fileType.type = null
            progressText.text =  null
        }

    }

}