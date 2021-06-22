package com.bandyer.sdk_design.filesharing.adapter_items

import android.text.format.Formatter
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerFileShareItemBinding
import com.bandyer.sdk_design.extensions.getFileTypeFromMimeType
import com.bandyer.sdk_design.extensions.parseToHHmm
import com.bandyer.sdk_design.filesharing.*
import com.bandyer.sdk_design.filesharing.buttons.BandyerFileShareActionButton
import com.bandyer.sdk_design.filesharing.imageviews.BandyerFileShareOpTypeImageView
import com.bandyer.sdk_design.filesharing.imageviews.BandyerFileTypeImageView
import com.bandyer.sdk_design.filesharing.model.FileTransfer
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook

class UploadItem(data: FileTransfer, viewModel: FileShareViewModel): BandyerFileShareItem<UploadItem.ViewHolder>(data, viewModel) {

    override var identifier: Long = data.hashCode().toLong()

    override val type: Int
        get() = R.id.bandyer_id_upload_item

    override val layoutRes: Int
        get() = R.layout.bandyer_file_share_item

    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(view: View) : FastAdapter.ViewHolder<UploadItem>(view) {

        val binding: BandyerFileShareItemBinding = BandyerFileShareItemBinding.bind(view)

        override fun bindView(item: UploadItem, payloads: List<Any>) {
            val bytesFormatted = Formatter.formatShortFileSize(itemView.context, item.data.info.size)
            binding.bandyerFileSize.text = if(bytesFormatted == "") itemView.context.resources.getString(R.string.bandyer_fileshare_na) else bytesFormatted
            binding.bandyerUsername.text = itemView.context.resources.getString(R.string.bandyer_fileshare_you)
            binding.bandyerError.text = itemView.context.resources.getString(R.string.bandyer_fileshare_error_message)
            binding.bandyerError.visibility = View.GONE
            binding.bandyerFileName.text = item.data.info.name
            binding.bandyerOperation.type = BandyerFileShareOpTypeImageView.Type.UPLOAD

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
                    binding.bandyerProgressText.text = itemView.context.resources.getString(R.string.bandyer_fileshare_progress, 0)
                }
            }

            val mimeType = item.data.info.mimeType
            binding.bandyerFileType.type = when(mimeType.getFileTypeFromMimeType()) {
                "image" -> BandyerFileTypeImageView.Type.IMAGE
                "archive" -> BandyerFileTypeImageView.Type.ARCHIVE
                else -> BandyerFileTypeImageView.Type.FILE
            }
        }

        override fun unbindView(item: UploadItem) {
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

    class UploadItemClickEvent: ClickEventHook<UploadItem>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            //return the views on which you want to bind this event
            return if (viewHolder is ViewHolder) {
                viewHolder.binding.bandyerActionClickArea
            } else {
                null
            }
        }

        override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<UploadItem>, item: UploadItem) {
            when (item.data.state) {
                is FileTransfer.State.Pending -> item.viewModel.cancelUpload(item.data.info.id)
                is FileTransfer.State.OnProgress -> item.viewModel.cancelUpload(item.data.info.id)
                is FileTransfer.State.Success -> item.openFile(item.data.info.uri, v)
                else -> item.viewModel.upload(v.context, item.data.info.uri, item.data.info.sender)
            }
        }
    }
}