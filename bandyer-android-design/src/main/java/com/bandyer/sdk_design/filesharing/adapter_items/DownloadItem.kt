package com.bandyer.sdk_design.filesharing.adapter_items

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.extensions.getFileTypeFromMimeType
import com.bandyer.sdk_design.extensions.getMimeType
import com.bandyer.sdk_design.extensions.parseToHHmm
import com.bandyer.sdk_design.filesharing.*
import com.bandyer.sdk_design.filesharing.buttons.BandyerFileShareActionButton
import com.bandyer.sdk_design.filesharing.imageviews.BandyerFileShareOpTypeImageView
import com.bandyer.sdk_design.filesharing.imageviews.BandyerFileTypeImageView
import com.bandyer.sdk_design.filesharing.model.DownloadData
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textview.MaterialTextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook

class DownloadItem(val data: DownloadData, val viewModel: FileShareViewModel): BandyerFileShareItem<DownloadItem, DownloadItem.ViewHolder>(data.startTime) {

    override fun getIdentifier(): Long = data.hashCode().toLong()

    override fun getType(): Int = R.id.bandyer_id_download_item

    override fun getLayoutRes(): Int =  R.layout.bandyer_file_share_item

    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(view: View) : FastAdapter.ViewHolder<DownloadItem>(view) {

        val fileType: BandyerFileTypeImageView = view.findViewById(R.id.bandyer_file_type)
        val fileSize: MaterialTextView = view.findViewById(R.id.bandyer_file_size)
        val action: BandyerFileShareActionButton = view.findViewById(R.id.bandyer_action)
        val fileName: MaterialTextView = view.findViewById(R.id.bandyer_file_name)
        val progressBar: LinearProgressIndicator = view.findViewById(R.id.bandyer_progress_bar)
        val operation: BandyerFileShareOpTypeImageView = view.findViewById(R.id.bandyer_operation)
        val user: MaterialTextView = view.findViewById(R.id.bandyer_username)
        val error: MaterialTextView = view.findViewById(R.id.bandyer_error)
        val progressText: MaterialTextView = view.findViewById(R.id.bandyer_progress_text)
        val clickArea: View = view.findViewById(R.id.bandyer_action_click_area)

        override fun bindView(item: DownloadItem, payloads: MutableList<Any>) {
            fileSize.text = itemView.context.resources.getString(R.string.bandyer_fileshare_na)
            user.text = item.data.sender
            error.text = itemView.context.resources.getString(R.string.bandyer_fileshare_error_message)
            error.visibility = View.GONE
            fileName.text = item.data.fileName
            operation.type = BandyerFileShareOpTypeImageView.Type.DOWNLOAD

            when(item.data) {
                is DownloadData.Pending -> {
                    action.type = BandyerFileShareActionButton.Type.CANCEL
                    progressText.text = item.data.startTime.parseToHHmm()
                }
                is DownloadData.OnProgress -> {
                    val progress = (item.data.downloadBytes * 100f / item.data.totalBytes).toInt()
                    progressBar.progress = progress
                    progressText.text = itemView.context.resources.getString(R.string.bandyer_fileshare_progress, progress)
                    action.type = BandyerFileShareActionButton.Type.CANCEL
                }
                is DownloadData.Success -> {
                    action.type = BandyerFileShareActionButton.Type.CHECK
                    progressBar.progress = 100
                    progressText.text = item.data.startTime.parseToHHmm()
                }
                is DownloadData.Error -> {
                    error.visibility = View.VISIBLE
                    action.type = BandyerFileShareActionButton.Type.RETRY
                }
            }

            val mimeType = item.data.endpoint.getMimeType()
            fileType.type = when(mimeType.getFileTypeFromMimeType()) {
                "image" -> BandyerFileTypeImageView.Type.IMAGE
                "archive" -> BandyerFileTypeImageView.Type.ARCHIVE
                else -> BandyerFileTypeImageView.Type.FILE
            }
        }

        override fun unbindView(item: DownloadItem) {
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

    class DownloadItemClickEvent: ClickEventHook<DownloadItem>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            //return the views on which you want to bind this event
            return if (viewHolder is ViewHolder) {
                viewHolder.clickArea
            } else {
                null
            }
        }

        override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<DownloadItem>, item: DownloadItem) {
            when(item.data) {
                is DownloadData.Pending -> item.viewModel.cancelDownload(item.data.id)
                is DownloadData.OnProgress -> item.viewModel.cancelDownload(item.data.id)
                is DownloadData.Success -> item.openFile(v.context, item.data.uri, v)
                is DownloadData.Error -> item.viewModel.download(item.data.id, item.data.endpoint, v.context)
            }
        }
    }
}