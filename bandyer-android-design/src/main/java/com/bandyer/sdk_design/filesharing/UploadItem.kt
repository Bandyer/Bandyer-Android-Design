package com.bandyer.sdk_design.filesharing

import android.text.format.Formatter
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.extensions.getFileName
import com.bandyer.sdk_design.extensions.getMimeType
import com.bandyer.sdk_design.extensions.parseToHHmm
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textview.MaterialTextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook

class UploadItem(val data: UploadData, val viewModel: FileShareViewModel): BandyerFileShareItem<UploadItem, UploadItem.ViewHolder>(data.startTime, data.uri) {

    override fun getIdentifier(): Long = data.hashCode().toLong()

    override fun getType(): Int = R.id.bandyer_id_upload_item

    override fun getLayoutRes(): Int =  R.layout.bandyer_file_share_item

    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(view: View) : FastAdapter.ViewHolder<UploadItem>(view) {

        val fileType: BandyerFileTypeImageView = view.findViewById(R.id.bandyer_file_type)
        val fileSize: MaterialTextView = view.findViewById(R.id.bandyer_file_size)
        val action: BandyerFileShareActionButton = view.findViewById(R.id.bandyer_action)
        val fileName: MaterialTextView = view.findViewById(R.id.bandyer_file_name)
        val progressBar: LinearProgressIndicator = view.findViewById(R.id.bandyer_progress_bar)
        val operation: BandyerFileShareOpTypeImageView = view.findViewById(R.id.bandyer_operation)
        val user: MaterialTextView = view.findViewById(R.id.bandyer_username)
        val error: MaterialTextView = view.findViewById(R.id.bandyer_error)
        val progressText: MaterialTextView = view.findViewById(R.id.bandyer_progress_text)

        override fun bindView(item: UploadItem, payloads: MutableList<Any>) {
            val bytesFormatted = Formatter.formatShortFileSize(itemView.context, item.data.totalBytes)
            fileSize.text = if(bytesFormatted == "") itemView.context.resources.getString(R.string.bandyer_fileshare_na) else bytesFormatted
            user.text = itemView.context.resources.getString(R.string.bandyer_fileshare_you)
            error.text = itemView.context.resources.getString(R.string.bandyer_fileshare_error_message)
            fileName.text = item.data.uri.getFileName(itemView.context)
            operation.type = BandyerFileShareOpTypeImageView.Type.UPLOAD

            when(item.data) {
                is UploadData.Pending -> {
                    action.type = BandyerFileShareActionButton.Type.CANCEL
                    progressText.text = item.data.startTime.parseToHHmm()
                }
                is UploadData.OnProgress -> {
                    val progress = (item.data.uploadedBytes * 100f / item.data.totalBytes).toInt()
                    progressBar.progress = progress
                    progressText.text = itemView.context.resources.getString(R.string.bandyer_fileshare_progress, progress)
                    action.type = BandyerFileShareActionButton.Type.CANCEL
                }
                is UploadData.Success -> {
                    action.type = BandyerFileShareActionButton.Type.RE_DOWNLOAD
                    progressBar.progress = 100
                    progressText.text = item.data.startTime.parseToHHmm()
                }
                is UploadData.Error -> {
                    error.visibility = View.VISIBLE
                    action.type = BandyerFileShareActionButton.Type.RETRY
                    progressText.text = itemView.context.resources.getString(R.string.bandyer_fileshare_progress, 0)
                }
            }

            val mimeType = item.data.uri.getMimeType(itemView.context)
            fileType.type = when(mimeType) {
                "image/gif", "image/vnd.microsoft.icon", "image/jpeg", "image/png", "image/svg+xml", "image/tiff", "image/webp" -> BandyerFileTypeImageView.Type.IMAGE
                "application/zip", "application/x-7z-compressed", "application/x-bzip", "application/x-bzip2", "application/gzip", "application/vnd.rar"-> BandyerFileTypeImageView.Type.ARCHIVE
                else -> BandyerFileTypeImageView.Type.FILE
            }
        }

        override fun unbindView(item: UploadItem) {
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

    class UploadItemClickEvent: ClickEventHook<UploadItem>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            //return the views on which you want to bind this event
            return if (viewHolder is ViewHolder) {
                viewHolder.action
            } else {
                null
            }
        }

        override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<UploadItem>, item: UploadItem) {
            when (item.data) {
                is UploadData.Pending -> item.viewModel.cancelUpload(item.data.uploadId)
                is UploadData.OnProgress -> item.viewModel.cancelUpload(item.data.uploadId)
                is UploadData.Success -> {
                }
                is UploadData.Error -> item.viewModel.upload(item.data.uploadId, v.context, item.data.uri)
            }
        }
    }
}