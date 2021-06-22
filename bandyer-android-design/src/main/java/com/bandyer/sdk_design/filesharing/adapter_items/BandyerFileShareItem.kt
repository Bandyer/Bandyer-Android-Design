package com.bandyer.sdk_design.filesharing.adapter_items

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.extensions.doesFileExists
import com.bandyer.sdk_design.extensions.getMimeType
import com.bandyer.sdk_design.extensions.isFileInTrash
import com.bandyer.sdk_design.filesharing.FileShareViewModel
import com.bandyer.sdk_design.filesharing.model.FileTransfer
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.fastadapter.items.AbstractItem

abstract class BandyerFileShareItem<VH>(val data: FileTransfer, val viewModel: FileShareViewModel): AbstractItem<VH>() where VH: RecyclerView.ViewHolder {
    protected fun openFile(uri: Uri, view: View) {
        kotlin.runCatching {
            if (!view.context.doesFileExists(uri))
                Snackbar.make(view, R.string.bandyer_fileshare_file_cancelled, Snackbar.LENGTH_SHORT).show()
            else {
                val isFileInTrash = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) view.context.isFileInTrash(uri) else false
                if(isFileInTrash) Snackbar.make(view, R.string.bandyer_fileshare_file_trashed, Snackbar.LENGTH_SHORT).show()
                else sendIntent(uri, view)
            }
        }
    }

    private fun sendIntent(uri: Uri, view: View) {
        val mimeType = uri.getMimeType(view.context)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, mimeType)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            view.context.startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            Snackbar.make(view, R.string.bandyer_fileshare_impossible_open_file, Snackbar.LENGTH_SHORT).show()
        }
    }
}
