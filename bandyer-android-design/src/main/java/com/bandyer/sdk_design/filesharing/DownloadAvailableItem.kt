package com.bandyer.sdk_design.filesharing

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.extensions.getFileNameFromUrl
import com.bandyer.sdk_design.extensions.getMimeType
import com.bandyer.sdk_design.extensions.parseToHHmm
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textview.MaterialTextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook

class DownloadAvailableItem(var data: DownloadAvailableData, val viewModel: FileShareViewModel): BandyerFileShareItem<DownloadAvailableItem, DownloadAvailableItem.ViewHolder>(data.startTime, data.file) {

    override fun getIdentifier(): Long = data.hashCode().toLong()

    override fun getType(): Int = R.id.bandyer_id_download_available_item

    override fun getLayoutRes(): Int =  R.layout.bandyer_file_share_item

    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(view: View) : FastAdapter.ViewHolder<DownloadAvailableItem>(view) {

        val fileType: BandyerFileTypeImageView = view.findViewById(R.id.bandyer_file_type)
        val fileSize: MaterialTextView = view.findViewById(R.id.bandyer_file_size)
        val action: BandyerFileShareActionButton = view.findViewById(R.id.bandyer_action)
        val fileName: MaterialTextView = view.findViewById(R.id.bandyer_file_name)
        val progressBar: LinearProgressIndicator = view.findViewById(R.id.bandyer_progress_bar)
        val operation: BandyerFileShareOpTypeImageView = view.findViewById(R.id.bandyer_operation)
        val user: MaterialTextView = view.findViewById(R.id.bandyer_username)
        val error: MaterialTextView = view.findViewById(R.id.bandyer_error)
        val progressText: MaterialTextView = view.findViewById(R.id.bandyer_progress_text)

        override fun bindView(item: DownloadAvailableItem, payloads: MutableList<Any>) {
            fileSize.text = itemView.context.resources.getString(R.string.bandyer_fileshare_na)
            user.text = item.data.sender
            error.text = itemView.context.resources.getString(R.string.bandyer_fileshare_error_message)
            fileName.text = item.data.endpoint.getFileNameFromUrl()
            operation.type = BandyerFileShareOpTypeImageView.Type.DOWNLOAD
            progressBar.progress = 0
            progressText.text = item.data.startTime.parseToHHmm()
            action.type = BandyerFileShareActionButton.Type.DOWNLOAD

            val mimeType = item.data.endpoint.getMimeType()
            fileType.type = when(mimeType) {
                "image/gif", "image/vnd.microsoft.icon", "image/jpeg", "image/png", "image/svg+xml", "image/tiff", "image/webp" -> BandyerFileTypeImageView.Type.IMAGE
                "application/zip", "application/x-7z-compressed", "application/x-bzip", "application/x-bzip2", "application/gzip", "application/vnd.rar"-> BandyerFileTypeImageView.Type.ARCHIVE
                else -> BandyerFileTypeImageView.Type.FILE
            }
        }

        override fun unbindView(item: DownloadAvailableItem) {
            fileType.type = null
            fileSize.text = null
            action.type = null
            fileName.text = null
            progressBar.progress = 0
            operation.type = null
            user.text = null
            error.text = null
            progressText.text =  null
        }
    }

    class DownloadAvailableItemClickEvent: ClickEventHook<DownloadAvailableItem>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            //return the views on which you want to bind this event
            return if (viewHolder is ViewHolder) {
                viewHolder.action
            } else {
                null
            }
        }

        override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<DownloadAvailableItem>, item: DownloadAvailableItem) {
            item.viewModel.download(item.data.downloadId, item.data.endpoint, item.data.file)
        }
    }

}