package com.bandyer.sdk_design.filesharing.adapter_items

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerFileShareItemBinding
import com.bandyer.sdk_design.extensions.getFileTypeFromMimeType
import com.bandyer.sdk_design.extensions.parseToHHmm
import com.bandyer.sdk_design.filesharing.FileShareViewModel
import com.bandyer.sdk_design.filesharing.buttons.BandyerFileShareActionButton
import com.bandyer.sdk_design.filesharing.imageviews.BandyerFileShareOpTypeImageView
import com.bandyer.sdk_design.filesharing.imageviews.BandyerFileTypeImageView
import com.bandyer.sdk_design.filesharing.model.FileTransfer
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook

class DownloadAvailableItem(data: FileTransfer, viewModel: FileShareViewModel, val askPermissionCallback: () -> Unit): BandyerFileShareItem<DownloadAvailableItem.ViewHolder>(data, viewModel) {

    override var identifier: Long = data.hashCode().toLong()

    override val type: Int
        get() = R.id.bandyer_id_download_available_item

    override val layoutRes: Int
        get() = R.layout.bandyer_file_share_item

    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(view: View) : FastAdapter.ViewHolder<DownloadAvailableItem>(view) {

        val binding: BandyerFileShareItemBinding = BandyerFileShareItemBinding.bind(view)

        override fun bindView(item: DownloadAvailableItem, payloads: List<Any>) {
            binding.bandyerFileSize.text = itemView.context.resources.getString(R.string.bandyer_fileshare_na)
            binding.bandyerUsername.text = item.data.info.sender
            binding.bandyerError.text = itemView.context.resources.getString(R.string.bandyer_fileshare_error_message)
            binding.bandyerFileName.text = item.data.info.name
            binding.bandyerOperation.type = BandyerFileShareOpTypeImageView.Type.DOWNLOAD
            binding.bandyerProgressBar.progress = 0
            binding.bandyerProgressText.text = item.data.info.creationTime.parseToHHmm()
            binding.bandyerAction.type = BandyerFileShareActionButton.Type.DOWNLOAD

            val mimeType = item.data.info.mimeType
            binding.bandyerFileType.type = when(mimeType.getFileTypeFromMimeType()) {
                "image" -> BandyerFileTypeImageView.Type.IMAGE
                "archive"-> BandyerFileTypeImageView.Type.ARCHIVE
                else -> BandyerFileTypeImageView.Type.FILE
            }
        }

        override fun unbindView(item: DownloadAvailableItem) {
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

    class DownloadAvailableItemClickEvent: ClickEventHook<DownloadAvailableItem>() {
            override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
                //return the views on which you want to bind this event
                return if (viewHolder is ViewHolder) {
                    viewHolder.binding.bandyerActionClickArea
                } else {
                    null
                }
            }

            override fun onClick(
                v: View,
                position: Int,
                fastAdapter: FastAdapter<DownloadAvailableItem>,
                item: DownloadAvailableItem
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R || ContextCompat.checkSelfPermission(v.context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    item.viewModel.download(v.context, item.data.info.uri, item.data.info.sender)
                } else
                    item.askPermissionCallback.invoke()
            }
    }

}