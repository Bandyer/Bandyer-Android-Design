/*
 * Copyright 2021-2022 Bandyer @ https://www.bandyer.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.bandyer.sdk_design.filesharing.adapter_items

import android.Manifest
import android.content.Context
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
import com.bandyer.sdk_design.filesharing.imageviews.BandyerFileTypeImageView
import com.bandyer.sdk_design.filesharing.imageviews.BandyerTransferTypeImageView
import com.bandyer.sdk_design.filesharing.model.TransferData
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.listeners.ClickEventHook
import kotlin.math.roundToInt

/**
 * BandyerFileShareItem
 *
 * @property data The data to bind to the item view
 * @constructor
 */
class BandyerFileTransferItem(val data: TransferData) : AbstractItem<BandyerFileTransferItem.ViewHolder>() {

    /**
     * @suppress
     */
    override var identifier: Long = data.hashCode().toLong()

    /**
     * @suppress
     */
    override val type: Int
        get() = R.id.bandyer_id_file_share_item

    /**
     * @suppress
     */
    override val layoutRes: Int
        get() = R.layout.bandyer_file_share_item

    /**
     * @suppress
     */
    override fun getViewHolder(v: View) = ViewHolder(v)

    /**
     * The view holder for a BandyerFileShareItem
     * @suppress
     */
    inner class ViewHolder(view: View) : FastAdapter.ViewHolder<BandyerFileTransferItem>(view) {

        val binding: BandyerFileShareItemBinding = BandyerFileShareItemBinding.bind(view)

        private val buttonAreaClickDefaultBackground = binding.bandyerActionClickArea.background

        override fun bindView(item: BandyerFileTransferItem, payloads: List<Any>) = with(binding) {
            bandyerError.visibility = View.GONE
            bandyerFileName.text = item.data.name
            bandyerFileType.type = when (item.data.mimeType.getFileTypeFromMimeType()) {
                "image"   -> BandyerFileTypeImageView.Type.IMAGE
                "archive" -> BandyerFileTypeImageView.Type.ARCHIVE
                else      -> BandyerFileTypeImageView.Type.FILE
            }
            updateItemByStatus(item)
            updateItemByType(item)
        }

        private fun updateItemByStatus(item: BandyerFileTransferItem) = with(binding) {
            root.isEnabled = item.data.state is TransferData.State.Success
            val progress = item.data.size.let { if(it > 0) (item.data.bytesTransferred * 100f / it).roundToInt() else 0 }
            bandyerProgressBar.progress = progress
            bandyerProgressText.text = itemView.context.resources.getString(
                R.string.bandyer_fileshare_progress,
                progress
            )

            root.setPaddingBottom(binding.root.context.resources.getDimensionPixelSize(R.dimen.bandyer_dimen_space18))
            root.setPaddingTop(binding.root.context.resources.getDimensionPixelSize(R.dimen.bandyer_dimen_space18))

            when (item.data.state) {
                is TransferData.State.Available                                 -> {
                    bandyerAction.type = BandyerFileTransferActionButton.Type.DOWNLOAD
                    bandyerProgressText.text = item.data.creationTime.parseToHHmm()
                }
                is TransferData.State.Pending, is TransferData.State.OnProgress -> bandyerAction.type = BandyerFileTransferActionButton.Type.CANCEL
                is TransferData.State.Success                                   -> {
                    bandyerAction.type = BandyerFileTransferActionButton.Type.SUCCESS
                    bandyerActionClickArea.background = null
                    bandyerActionClickArea.isClickable = false
                    bandyerProgressText.text = item.data.creationTime.parseToHHmm()
                }
                is TransferData.State.Error                                     -> {
                    bandyerAction.type = BandyerFileTransferActionButton.Type.RETRY
                    bandyerError.visibility = View.VISIBLE
                    root.setPaddingBottom(binding.root.context.resources.getDimensionPixelSize(R.dimen.bandyer_dimen_space8))
                    root.setPaddingTop(binding.root.context.resources.getDimensionPixelSize(R.dimen.bandyer_dimen_space26))
                }
                TransferData.State.Cancelled                                    -> Unit
            }
        }

        private fun updateItemByType(item: BandyerFileTransferItem) = with(binding) {
            if (item.data.type is TransferData.Type.Upload) {
                bandyerOperation.type = BandyerTransferTypeImageView.Type.UPLOAD
                bandyerUsername.text = itemView.context.resources.getString(R.string.bandyer_fileshare_you)
                bandyerFileSize.text = Formatter.formatShortFileSize(itemView.context, item.data.size)
                bandyerError.text = itemView.context.resources.getString(R.string.bandyer_fileshare_upload_error)
                return
            }

            bandyerOperation.type = BandyerTransferTypeImageView.Type.DOWNLOAD
            bandyerUsername.text = item.data.sender
            bandyerFileSize.text = if (item.data.state is TransferData.State.OnProgress || item.data.state is TransferData.State.Success)
                Formatter.formatShortFileSize(itemView.context, item.data.size)
            else itemView.context.resources.getString(R.string.bandyer_fileshare_na)
            bandyerError.text = itemView.context.resources.getString(R.string.bandyer_fileshare_download_error)
        }

        override fun unbindView(item: BandyerFileTransferItem) = with(binding) {
            bandyerActionClickArea.background = buttonAreaClickDefaultBackground
            bandyerActionClickArea.isClickable = true
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
    internal class ItemClickEvent(private val viewModel: FileShareViewModel, private val askPermissionCallback: ((() -> Unit) -> Unit)? = null) : ClickEventHook<BandyerFileTransferItem>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            //return the views on which you want to bind this event
            return if (viewHolder is ViewHolder) viewHolder.binding.bandyerActionClickArea
            else null
        }

        override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<BandyerFileTransferItem>, item: BandyerFileTransferItem) {
            if (item.data.state is TransferData.State.Available) {
                checkPermissionAndStartDownload(v.context, item.data)
                return
            }

            when (item.data.state) {
                is TransferData.State.Pending, is TransferData.State.OnProgress -> {
                    if (item.data.type is TransferData.Type.Upload) viewModel.cancelFileUpload(item.data.id)
                    else viewModel.cancelFileDownload(item.data.id)
                }
                is TransferData.State.Success                                   -> (v.parent as View).apply { isPressed = true; performClick(); isPressed = false }
                is TransferData.State.Error                                     -> {
                    if (item.data.type is TransferData.Type.Upload) viewModel.uploadFile(v.context, item.data.id, item.data.uri, item.data.sender)
                    else checkPermissionAndStartDownload(v.context, item.data)
                }
                else                                                            -> Unit
            }
        }

        private fun checkPermissionAndStartDownload(context: Context, data: TransferData) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            ) viewModel.downloadFile(context = context, data.id, data.uri, data.sender)
            else askPermissionCallback?.invoke { viewModel.downloadFile(context = context, data.id, data.uri, data.sender) }
        }
    }
}