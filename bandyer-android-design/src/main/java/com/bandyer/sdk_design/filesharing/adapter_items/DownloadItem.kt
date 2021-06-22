package com.bandyer.sdk_design.filesharing.adapter_items

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerFileShareItemBinding
import com.bandyer.sdk_design.extensions.getFileTypeFromMimeType
import com.bandyer.sdk_design.extensions.parseToHHmm
import com.bandyer.sdk_design.filesharing.FileShareViewModel
import com.bandyer.sdk_design.filesharing.buttons.BandyerFileShareActionButton
import com.bandyer.sdk_design.filesharing.imageviews.BandyerFileShareOpTypeImageView
import com.bandyer.sdk_design.filesharing.imageviews.BandyerFileTypeImageView
import com.bandyer.sdk_design.filesharing.model.Download
import com.bandyer.sdk_design.filesharing.model.FileTransfer
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook

class DownloadItem(data: FileTransfer, viewModel: FileShareViewModel): BandyerFileShareItem<DownloadItem.ViewHolder>(data, viewModel) {

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
            binding.bandyerUsername.text = item.data.info.sender
            binding.bandyerError.text = itemView.context.resources.getString(R.string.bandyer_fileshare_error_message)
            binding.bandyerError.visibility = View.GONE
            binding.bandyerFileName.text = item.data.info.name
            binding.bandyerOperation.type = BandyerFileShareOpTypeImageView.Type.DOWNLOAD

            when(val state = item.data.state) {
                is FileTransfer.State.Pending -> {
                    binding.bandyerAction.type = BandyerFileShareActionButton.Type.CANCEL
                    binding.bandyerProgressText.text = item.data.info.creationTime.parseToHHmm()
                }
                is FileTransfer.State.OnProgress -> {
                    val progress = (state.bytesTransferred * 100f / item.data.info.size).toInt()
                    binding.bandyerProgressBar.progress = progress
                    binding.bandyerProgressText.text = itemView.context.resources.getString(R.string.bandyer_fileshare_progress, progress)
                    binding.bandyerAction.type = BandyerFileShareActionButton.Type.CANCEL
                }
                is FileTransfer.State.Success -> {
                    binding.bandyerAction.type = BandyerFileShareActionButton.Type.CHECK
                    binding.bandyerProgressBar.progress = 100
                    binding.bandyerProgressText.text = item.data.info.creationTime.parseToHHmm()
                }
                is FileTransfer.State.Error -> {
                    binding.bandyerError.visibility = View.VISIBLE
                    binding.bandyerAction.type = BandyerFileShareActionButton.Type.RETRY
                }
            }

            val mimeType = item.data.info.mimeType
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
            when (val state = item.data.state) {
                is FileTransfer.State.Pending -> item.viewModel.cancelDownload(item.data.info.id)
                is FileTransfer.State.OnProgress -> item.viewModel.cancelDownload(item.data.info.id)
                is FileTransfer.State.Success -> item.openFile(state.uri, v)
                is FileTransfer.State.Error -> item.viewModel.download(v.context, item.data.info.uri, item.data.info.sender)
            }
        }
    }
}