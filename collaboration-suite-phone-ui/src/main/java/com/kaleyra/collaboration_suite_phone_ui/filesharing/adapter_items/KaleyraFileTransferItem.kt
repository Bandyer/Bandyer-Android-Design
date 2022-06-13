/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_phone_ui.filesharing.adapter_items

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import android.text.format.Formatter
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.databinding.KaleyraFileShareItemBinding
import com.kaleyra.collaboration_suite_phone_ui.extensions.getFileTypeFromMimeType
import com.kaleyra.collaboration_suite_phone_ui.extensions.parseToHHmm
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ViewExtensions.setPaddingBottom
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ViewExtensions.setPaddingTop
import com.kaleyra.collaboration_suite_phone_ui.filesharing.FileShareViewModel
import com.kaleyra.collaboration_suite_phone_ui.filesharing.buttons.KaleyraFileTransferActionButton
import com.kaleyra.collaboration_suite_phone_ui.filesharing.imageviews.KaleyraFileTypeImageView
import com.kaleyra.collaboration_suite_phone_ui.filesharing.imageviews.KaleyraTransferTypeImageView
import com.kaleyra.collaboration_suite_phone_ui.filesharing.model.TransferData
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.listeners.ClickEventHook
import kotlin.math.roundToInt

/**
 * KaleyraFileShareItem
 *
 * @property data The data to bind to the item view
 * @constructor
 */
class KaleyraFileTransferItem(val data: TransferData) : AbstractItem<KaleyraFileTransferItem.ViewHolder>() {

    /**
     * @suppress
     */

    override var identifier: Long = data.id.hashCode().toLong()

    override fun equals(other: Any?): Boolean =
        if (other !is KaleyraFileTransferItem) false
        else this.identifier == other.identifier && this.data.bytesTransferred == other.data.bytesTransferred

    override fun hashCode() = data.id.hashCode()

    /**
     * @suppress
     */
    override val type: Int
        get() = R.id.kaleyra_id_file_share_item

    /**
     * @suppress
     */
    override val layoutRes: Int
        get() = R.layout.kaleyra_file_share_item

    /**
     * @suppress
     */
    override fun getViewHolder(v: View) = ViewHolder(v)

    /**
     * The view holder for a KaleyraFileShareItem
     * @suppress
     */
    inner class ViewHolder(view: View) : FastAdapter.ViewHolder<KaleyraFileTransferItem>(view) {

        val binding: KaleyraFileShareItemBinding = KaleyraFileShareItemBinding.bind(view)

        private val buttonAreaClickDefaultBackground = binding.kaleyraActionClickArea.background

        override fun bindView(item: KaleyraFileTransferItem, payloads: List<Any>) = with(binding) {
            kaleyraError.visibility = View.GONE
            kaleyraFileName.text = item.data.name
            kaleyraFileType.type = when (item.data.mimeType.getFileTypeFromMimeType()) {
                "image"   -> KaleyraFileTypeImageView.Type.IMAGE
                "archive" -> KaleyraFileTypeImageView.Type.ARCHIVE
                else      -> KaleyraFileTypeImageView.Type.FILE
            }
            updateItemByStatus(item)
            updateItemByType(item)
        }

        private fun updateItemByStatus(item: KaleyraFileTransferItem) = with(binding) {
            root.isEnabled = item.data.state is TransferData.State.Success
            val progress = item.data.size.let { if (it > 0) (item.data.bytesTransferred * 100f / it).roundToInt() else 0 }
            kaleyraProgressBar.progress = progress
            kaleyraProgressText.text = itemView.context.resources.getString(
                R.string.kaleyra_fileshare_progress,
                progress
            )

            root.setPaddingBottom(binding.root.context.resources.getDimensionPixelSize(R.dimen.kaleyra_dimen_space18))
            root.setPaddingTop(binding.root.context.resources.getDimensionPixelSize(R.dimen.kaleyra_dimen_space18))

            when (item.data.state) {
                is TransferData.State.Available                                 -> {
                    kaleyraAction.type = KaleyraFileTransferActionButton.Type.DOWNLOAD
                    kaleyraProgressText.text = item.data.creationTime.parseToHHmm()
                }
                is TransferData.State.Pending, is TransferData.State.OnProgress -> kaleyraAction.type = KaleyraFileTransferActionButton.Type.CANCEL
                is TransferData.State.Success                                   -> {
                    kaleyraAction.type = KaleyraFileTransferActionButton.Type.SUCCESS
                    kaleyraActionClickArea.background = null
                    kaleyraActionClickArea.isClickable = false
                    kaleyraProgressText.text = item.data.creationTime.parseToHHmm()
                }
                is TransferData.State.Error                                     -> {
                    kaleyraAction.type = KaleyraFileTransferActionButton.Type.RETRY
                    kaleyraError.visibility = View.VISIBLE
                    root.setPaddingBottom(binding.root.context.resources.getDimensionPixelSize(R.dimen.kaleyra_dimen_space8))
                    root.setPaddingTop(binding.root.context.resources.getDimensionPixelSize(R.dimen.kaleyra_dimen_space26))
                }
                TransferData.State.Cancelled                                    -> Unit
            }
        }

        private fun updateItemByType(item: KaleyraFileTransferItem) = with(binding) {
            if (item.data.type is TransferData.Type.Upload) {
                kaleyraOperation.type = KaleyraTransferTypeImageView.Type.UPLOAD
                kaleyraUsername.text = itemView.context.resources.getString(R.string.kaleyra_fileshare_you)
                kaleyraFileSize.text = Formatter.formatShortFileSize(itemView.context, item.data.size)
                kaleyraError.text = itemView.context.resources.getString(R.string.kaleyra_fileshare_upload_error)
                return
            }

            kaleyraOperation.type = KaleyraTransferTypeImageView.Type.DOWNLOAD
            kaleyraUsername.text = item.data.sender
            kaleyraFileSize.text = if (item.data.state is TransferData.State.OnProgress || item.data.state is TransferData.State.Success)
                Formatter.formatShortFileSize(itemView.context, item.data.size)
            else itemView.context.resources.getString(R.string.kaleyra_fileshare_na)
            kaleyraError.text = itemView.context.resources.getString(R.string.kaleyra_fileshare_download_error)
        }

        override fun unbindView(item: KaleyraFileTransferItem) = with(binding) {
            kaleyraActionClickArea.background = buttonAreaClickDefaultBackground
            kaleyraActionClickArea.isClickable = true
            kaleyraFileType.type = null
            kaleyraFileSize.text = null
            kaleyraAction.type = null
            kaleyraFileName.text = null
            kaleyraProgressBar.progress = 0
            kaleyraOperation.type = null
            kaleyraUsername.text = null
            kaleyraError.text = null
            kaleyraProgressText.text = null
        }
    }

    /**
     * Item click event
     * @suppress
     */
    internal class ItemClickEvent(private val viewModel: FileShareViewModel, private val askPermissionCallback: ((() -> Unit) -> Unit)? = null) : ClickEventHook<KaleyraFileTransferItem>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            //return the views on which you want to bind this event
            return if (viewHolder is ViewHolder) viewHolder.binding.kaleyraActionClickArea
            else null
        }

        override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<KaleyraFileTransferItem>, item: KaleyraFileTransferItem) {
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