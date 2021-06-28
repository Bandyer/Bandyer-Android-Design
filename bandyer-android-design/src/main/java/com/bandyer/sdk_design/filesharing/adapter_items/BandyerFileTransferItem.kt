package com.bandyer.sdk_design.filesharing.adapter_items

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.text.format.Formatter
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerFileShareItemBinding
import com.bandyer.sdk_design.extensions.getFileTypeFromMimeType
import com.bandyer.sdk_design.extensions.parseToHHmm
import com.bandyer.sdk_design.extensions.setPaddingBottom
import com.bandyer.sdk_design.extensions.setPaddingTop
import com.bandyer.sdk_design.filesharing.FileShareViewModel
import com.bandyer.sdk_design.filesharing.buttons.BandyerFileTransferActionButton
import com.bandyer.sdk_design.filesharing.imageviews.BandyerTransferTypeImageView
import com.bandyer.sdk_design.filesharing.imageviews.BandyerFileTypeImageView
import com.bandyer.sdk_design.filesharing.model.TransferData
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.listeners.ClickEventHook

/**
 * BandyerFileShareItem
 *
 * @property data The data to bind to the item view
 * @property viewModel A [FileShareViewModel] instance
 * @property askPermissionCallback The callback which perform the permission request on the activity
 * @constructor
 */
class BandyerFileTransferItem(val data: TransferData, val viewModel: FileShareViewModel, val askPermissionCallback: (() -> Unit)? = null) : AbstractItem<BandyerFileTransferItem.ViewHolder>() {

    override var identifier: Long = data.hashCode().toLong()

    override val type: Int
        get() = R.id.bandyer_id_file_share_item

    override val layoutRes: Int
        get() = R.layout.bandyer_file_share_item

    override fun getViewHolder(v: View) = ViewHolder(v)

    /**
     * The view holder for a BandyerFileShareItem
     * @suppress
     */
    inner class ViewHolder(view: View) : FastAdapter.ViewHolder<BandyerFileTransferItem>(view) {

        val binding: BandyerFileShareItemBinding = BandyerFileShareItemBinding.bind(view)

        override fun bindView(item: BandyerFileTransferItem, payloads: List<Any>) = with(binding) {
            bandyerFileName.text = item.data.data.name
            bandyerFileType.type = when (item.data.data.mimeType.getFileTypeFromMimeType()) {
                "image"   -> BandyerFileTypeImageView.Type.IMAGE
                "archive" -> BandyerFileTypeImageView.Type.ARCHIVE
                else      -> BandyerFileTypeImageView.Type.FILE
            }
            updateItemByStatus(item)
            updateItemByType(item)
        }

        private fun updateItemByStatus(item: BandyerFileTransferItem) = with(binding) {
            root.isEnabled = item.data.state is TransferData.State.Success
            val progress = (item.data.bytesTransferred * 100f / item.data.data.size).toInt()
            bandyerProgressBar.progress = progress
            bandyerProgressText.text = itemView.context.resources.getString(
                R.string.bandyer_fileshare_progress,
                progress
            )

            when (item.data.state) {
                is TransferData.State.Available                                 -> {
                    bandyerAction.type = BandyerFileTransferActionButton.Type.DOWNLOAD
                    bandyerProgressText.text = item.data.data.creationTime.parseToHHmm()
                }
                is TransferData.State.Pending, is TransferData.State.OnProgress -> bandyerAction.type = BandyerFileTransferActionButton.Type.CANCEL
                is TransferData.State.Success                                   -> {
                    bandyerAction.type = BandyerFileTransferActionButton.Type.SUCCESS
                    bandyerActionClickArea.background = null
                    bandyerProgressText.text = item.data.data.creationTime.parseToHHmm()
                }
                is TransferData.State.Error                                     -> {
                    bandyerAction.type = BandyerFileTransferActionButton.Type.RETRY
                    bandyerError.visibility = View.VISIBLE
                    root.setPaddingBottom(binding.root.context.resources.getDimensionPixelSize(R.dimen.bandyer_dimen_space8))
                    root.setPaddingTop(binding.root.context.resources.getDimensionPixelSize(R.dimen.bandyer_dimen_space28))
                }
                TransferData.State.Cancelled                                    -> Unit
            }
        }

        private fun updateItemByType(item: BandyerFileTransferItem) = with(binding) {
            if (item.data.type is TransferData.Type.Upload) {
                bandyerOperation.type = BandyerTransferTypeImageView.Type.UPLOAD
                bandyerUsername.text = itemView.context.resources.getString(R.string.bandyer_fileshare_you)
                bandyerFileSize.text = Formatter.formatShortFileSize(itemView.context, item.data.data.size)
                bandyerError.text = itemView.context.resources.getString(R.string.bandyer_fileshare_upload_error)
                return
            }

            bandyerOperation.type = BandyerTransferTypeImageView.Type.DOWNLOAD
            bandyerUsername.text = item.data.data.sender
            bandyerFileSize.text = if (item.data.state is TransferData.State.OnProgress || item.data.state is TransferData.State.Success)
                Formatter.formatShortFileSize(itemView.context, item.data.data.size)
            else itemView.context.resources.getString(R.string.bandyer_fileshare_na)
            bandyerError.text = itemView.context.resources.getString(R.string.bandyer_fileshare_download_error)
        }

        override fun unbindView(item: BandyerFileTransferItem) = with(binding) {
            bandyerFileType.type = null
            bandyerFileSize.text = null
            bandyerAction.type = null
            bandyerFileName.text = null
            bandyerProgressBar.progress = 0
            bandyerOperation.type = null
            bandyerUsername.text = null
            bandyerError.text = null
            bandyerProgressText.text = null
        }
    }

    /**
     * Item click event
     * @suppress
     */
    internal class ItemClickEvent : ClickEventHook<BandyerFileTransferItem>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            //return the views on which you want to bind this event
            return if (viewHolder is ViewHolder) viewHolder.binding.bandyerActionClickArea
            else null
        }

        override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<BandyerFileTransferItem>, item: BandyerFileTransferItem) {
            if (item.data.state is TransferData.State.Available) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ||
                    ContextCompat.checkSelfPermission(v.context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                )
                    item.viewModel.downloadFile(context = v.context, item.data.data.id, item.data.data.uri, item.data.data.sender)
                else item.askPermissionCallback?.invoke()
                return
            }

            when (item.data.state) {
                is TransferData.State.Pending, is TransferData.State.OnProgress -> {
                    if (item.data.type is TransferData.Type.Upload) item.viewModel.cancelFileUpload(item.data.data.id)
                    else item.viewModel.cancelFileDownload(item.data.data.id)
                }
                is TransferData.State.Success                                   -> (v.parent as View).apply { isPressed = true; performClick(); isPressed = false }
                is TransferData.State.Error                                     -> {
                    if (item.data.type is TransferData.Type.Upload) item.viewModel.uploadFile(v.context, item.data.data.id, item.data.data.uri, item.data.data.sender)
                    else item.viewModel.downloadFile(v.context, item.data.data.id, item.data.data.uri, item.data.data.sender)
                }
                else                                                            -> Unit
            }
        }
    }
}