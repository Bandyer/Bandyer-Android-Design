package com.bandyer.sdk_design.filesharing.adapter_items

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerFileShareItemBinding
import com.bandyer.sdk_design.extensions.getFileTypeFromMimeType
import com.bandyer.sdk_design.extensions.getMimeType
import com.bandyer.sdk_design.extensions.parseToHHmm
import com.bandyer.sdk_design.filesharing.FileShareViewModel
import com.bandyer.sdk_design.filesharing.buttons.BandyerFileShareActionButton
import com.bandyer.sdk_design.filesharing.imageviews.BandyerFileShareOpTypeImageView
import com.bandyer.sdk_design.filesharing.imageviews.BandyerFileTypeImageView
import com.bandyer.sdk_design.filesharing.model.DownloadData
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook

class DownloadItem(val data: DownloadData, val viewModel: FileShareViewModel): BandyerFileShareItem<DownloadItem.ViewHolder>(data.startTime) {

    override var identifier: Long = data.hashCode().toLong()

    override val type: Int
        get() = R.id.bandyer_id_download_item

    override val layoutRes: Int
        get() = R.layout.bandyer_file_share_item

    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(view: View) : FastAdapter.ViewHolder<DownloadItem>(view) {

        val binding: BandyerFileShareItemBinding = BandyerFileShareItemBinding.bind(view)

        override fun bindView(item: DownloadItem, payloads: List<Any>) {
            binding.bandyerFileSize.text = itemView.context.resources.getString(R.string.bandyer_fileshare_na)
            binding.bandyerUsername.text = item.data.sender
            binding.bandyerError.text = itemView.context.resources.getString(R.string.bandyer_fileshare_error_message)
            binding.bandyerError.visibility = View.GONE
            binding.bandyerFileName.text = item.data.fileName
            binding.bandyerOperation.type = BandyerFileShareOpTypeImageView.Type.DOWNLOAD

            when(item.data) {
                is DownloadData.Pending -> {
                    binding.bandyerAction.type = BandyerFileShareActionButton.Type.CANCEL
                    binding.bandyerProgressText.text = item.data.startTime.parseToHHmm()
                }
                is DownloadData.OnProgress -> {
                    val progress = (item.data.downloadBytes * 100f / item.data.totalBytes).toInt()
                    binding.bandyerProgressBar.progress = progress
                    binding.bandyerProgressText.text = itemView.context.resources.getString(R.string.bandyer_fileshare_progress, progress)
                    binding.bandyerAction.type = BandyerFileShareActionButton.Type.CANCEL
                }
                is DownloadData.Success -> {
                    binding.bandyerAction.type = BandyerFileShareActionButton.Type.CHECK
                    binding.bandyerProgressBar.progress = 100
                    binding.bandyerProgressText.text = item.data.startTime.parseToHHmm()
                }
                is DownloadData.Error -> {
                    binding.bandyerError.visibility = View.VISIBLE
                    binding.bandyerAction.type = BandyerFileShareActionButton.Type.RETRY
                }
            }

            val mimeType = item.data.endpoint.getMimeType()
            binding.bandyerFileType.type = when(mimeType.getFileTypeFromMimeType()) {
                "image" -> BandyerFileTypeImageView.Type.IMAGE
                "archive" -> BandyerFileTypeImageView.Type.ARCHIVE
                else -> BandyerFileTypeImageView.Type.FILE
            }
        }

        override fun unbindView(item: DownloadItem) {
            binding.bandyerFileType.type = null
            binding.bandyerFileSize.text = null
            binding.bandyerAction.type = null
            binding.bandyerFileName.text = null
            binding.bandyerProgressBar.progress = 0
            binding.bandyerOperation.type = null
            binding.bandyerUsername.text = null
            binding.bandyerError.text = null
            binding.bandyerProgressText.text =  null
        }
    }

    class DownloadItemClickEvent: ClickEventHook<DownloadItem>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            //return the views on which you want to bind this event
            return if (viewHolder is ViewHolder) {
                viewHolder.binding.bandyerActionClickArea
            } else {
                null
            }
        }

        override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<DownloadItem>, item: DownloadItem) {
            when(item.data) {
                is DownloadData.Pending -> item.viewModel.cancelDownload(item.data)
                is DownloadData.OnProgress -> item.viewModel.cancelDownload(item.data)
                is DownloadData.Success -> item.openFile(v.context, item.data.uri, v)
                is DownloadData.Error -> item.viewModel.download(v.context, item.data)
            }
        }
    }
}