package com.bandyer.demo_sdk_design

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.bandyer.demo_sdk_design.databinding.ActivityFileShareBinding
import com.bandyer.sdk_design.filesharing.BandyerFileShareDialog
import com.bandyer.sdk_design.filesharing.FileShareViewModel
import com.bandyer.sdk_design.filesharing.model.FileInfo
import com.bandyer.sdk_design.filesharing.model.FileTransfer
import java.util.*

class FileShareActivity : AppCompatActivity() {

    private val viewModel: LocalFileShareViewModel by viewModels()

    private lateinit var binding: ActivityFileShareBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileShareBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.itemsData["id_1"] = FileTransfer(FileInfo(uri = "".toUri(), name = "razer.jpg", mimeType = "image/jpg", sender = "Gianluigi", size = 100L), FileTransfer.State.Pending, FileTransfer.Type.DownloadAvailable)
        viewModel.itemsData["id_2"] = FileTransfer(FileInfo(uri = "".toUri(), name = "identity_card.pdf", mimeType = "", sender = "Mario", size = 100L), FileTransfer.State.Success("".toUri()), FileTransfer.Type.Download)
        viewModel.itemsData["id_3"] = FileTransfer(FileInfo(uri = "".toUri(), name = "car.zip", mimeType = "application/zip", sender = "Luigi", size = 1000L), FileTransfer.State.OnProgress(600L), FileTransfer.Type.Download)
        viewModel.itemsData["id_4"] = FileTransfer(FileInfo(uri = "".toUri(), name = "phone.doc", mimeType = "", sender = "Gianni", size = 23000000L), FileTransfer.State.Error(Throwable()), FileTransfer.Type.Upload)
        viewModel.itemsData["id_5"] = FileTransfer(FileInfo(uri = "".toUri(), name = "address.jpg", mimeType = "image/jpg", sender = "Marco", size = 1000L), FileTransfer.State.Pending, FileTransfer.Type.Upload)

        binding.btnFileShare.setOnClickListener { BandyerFileShareDialog().show(this@FileShareActivity, viewModel) }
    }
}

class LocalFileShareViewModel: FileShareViewModel() {

    override fun upload(context: Context, transfer: FileTransfer) = FileTransfer(FileInfo(uri = "".toUri(), name = "", mimeType = "", sender = ""), FileTransfer.State.Pending, FileTransfer.Type.Upload)

    override fun download(context: Context, transfer: FileTransfer) = FileTransfer(FileInfo(uri = "".toUri(), name = "", mimeType = "", sender = ""), FileTransfer.State.Pending, FileTransfer.Type.Download)

    override fun cancel(transfer: FileTransfer) = Unit
}

