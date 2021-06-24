package com.bandyer.sdk_design.filesharing.adapter_items

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.text.format.Formatter
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerFileShareItemBinding
import com.bandyer.sdk_design.extensions.*
import com.bandyer.sdk_design.filesharing.FileShareViewModel
import com.bandyer.sdk_design.filesharing.buttons.BandyerFileShareActionButton
import com.bandyer.sdk_design.filesharing.imageviews.BandyerFileShareOpTypeImageView
import com.bandyer.sdk_design.filesharing.imageviews.BandyerFileTypeImageView
import com.bandyer.sdk_design.filesharing.model.FileShareItemData
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.listeners.ClickEventHook

class BandyerFileShareItem(val data: FileShareItemData, val viewModel: FileShareViewModel, val askPermissionCallback: (() -> Unit)? = null) : AbstractItem<BandyerFileShareItem.ViewHolder>() {

    override var identifier: Long = data.hashCode().toLong()

    override val type: Int
        get() = R.id.bandyer_id_file_share_item

    override val layoutRes: Int
        get() = R.layout.bandyer_file_share_item

    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(view: View) : FastAdapter.ViewHolder<BandyerFileShareItem>(view) {

        val binding: BandyerFileShareItemBinding = BandyerFileShareItemBinding.bind(view)

        override fun bindView(item: BandyerFileShareItem, payloads: List<Any>) {
            if (item.data.state !is FileShareItemData.State.Success) binding.root.background = null
            else binding.bandyerActionClickArea.background = null

            binding.bandyerProgressBar.progress = 0
            binding.bandyerError.visibility = View.GONE
            binding.bandyerFileName.text = item.data.info.name
            binding.bandyerFileType.type = when (item.data.info.mimeType.getFileTypeFromMimeType()) {
                "image"   -> BandyerFileTypeImageView.Type.IMAGE
                "archive" -> BandyerFileTypeImageView.Type.ARCHIVE
                else      -> BandyerFileTypeImageView.Type.FILE
            }
            binding.root.setPaddingTop(binding.root.context.resources.getDimensionPixelSize(R.dimen.bandyer_dimen_space18))
            binding.root.setPaddingBottom(binding.root.context.resources.getDimensionPixelSize(R.dimen.bandyer_dimen_space18))

            when (val state = item.data.state) {
                is FileShareItemData.State.Pending    -> {
                    binding.bandyerAction.type = BandyerFileShareActionButton.Type.CANCEL
                    binding.bandyerProgressText.text = item.data.info.creationTime.parseToHHmm()
                }
                is FileShareItemData.State.OnProgress -> {
                    val progress = (state.bytesTransferred * 100f / item.data.info.size).toInt()
                    binding.bandyerProgressBar.progress = progress
                    binding.bandyerProgressText.text = itemView.context.resources.getString(
                        R.string.bandyer_fileshare_progress,
                        progress
                    )
                    binding.bandyerAction.type = BandyerFileShareActionButton.Type.CANCEL
                }
                is FileShareItemData.State.Success    -> {
                    binding.bandyerAction.type = BandyerFileShareActionButton.Type.CHECK
                    binding.bandyerProgressBar.progress = 100
                    binding.bandyerProgressText.text = item.data.info.creationTime.parseToHHmm()
                }
                is FileShareItemData.State.Error      -> {
                    binding.bandyerError.visibility = View.VISIBLE
                    binding.bandyerAction.type = BandyerFileShareActionButton.Type.RETRY
                    binding.bandyerProgressText.text = itemView.context.resources.getString(R.string.bandyer_fileshare_progress, 0)
                    binding.root.setPaddingBottom(binding.root.context.resources.getDimensionPixelSize(R.dimen.bandyer_dimen_space8))
                    binding.root.setPaddingTop(binding.root.context.resources.getDimensionPixelSize(R.dimen.bandyer_dimen_space28))
                }
            }

            when (val type = item.data.type) {
                is FileShareItemData.Type.Upload -> {
                    binding.bandyerError.text = itemView.context.resources.getString(R.string.bandyer_fileshare_upload_error)
                    binding.bandyerOperation.type = BandyerFileShareOpTypeImageView.Type.UPLOAD
                    binding.bandyerUsername.text = itemView.context.resources.getString(R.string.bandyer_fileshare_you)
                    binding.bandyerFileSize.text = Formatter.formatShortFileSize(itemView.context, item.data.info.size)
                }
                else                             -> {
                    if (type is FileShareItemData.Type.DownloadAvailable) binding.bandyerAction.type = BandyerFileShareActionButton.Type.DOWNLOAD

                    binding.bandyerError.text = itemView.context.resources.getString(R.string.bandyer_fileshare_download_error)
                    binding.bandyerOperation.type = BandyerFileShareOpTypeImageView.Type.DOWNLOAD
                    binding.bandyerUsername.text = item.data.info.sender
                    binding.bandyerFileSize.text =
                        if (item.data.state is FileShareItemData.State.Success) Formatter.formatShortFileSize(itemView.context, item.data.info.size)
                        else itemView.context.resources.getString(R.string.bandyer_fileshare_na)
                }
            }
        }

        override fun unbindView(item: BandyerFileShareItem) {
            binding.bandyerFileType.type = null
            binding.bandyerFileSize.text = null
            binding.bandyerAction.type = null
            binding.bandyerFileName.text = null
            binding.bandyerProgressBar.progress = 0
            binding.bandyerOperation.type = null
            binding.bandyerUsername.text = null
            binding.bandyerError.text = null
            binding.bandyerProgressText.text = null
        }
    }

    class ItemClickEvent : ClickEventHook<BandyerFileShareItem>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            //return the views on which you want to bind this event
            return if (viewHolder is ViewHolder) viewHolder.binding.bandyerActionClickArea
            else null
        }

        override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<BandyerFileShareItem>, item: BandyerFileShareItem) {
            if (item.data.type is FileShareItemData.Type.DownloadAvailable) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R || ContextCompat.checkSelfPermission(v.context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    item.viewModel.download(v.context, item.data)
                else
                    item.askPermissionCallback?.invoke()
                return
            }

            when (item.data.state) {
                is FileShareItemData.State.Pending -> item.viewModel.cancel(item.data)
                is FileShareItemData.State.OnProgress -> item.viewModel.cancel(item.data)
                is FileShareItemData.State.Error -> {
                    if(item.data.type is FileShareItemData.Type.Upload) item.viewModel.upload(v.context, item.data)
                    else item.viewModel.download(v.context, item.data)
                }
                else -> { }
            }
        }
    }
}