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
import com.bandyer.sdk_design.filesharing.model.FileShareItemData
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class FileShareActivity : AppCompatActivity() {

    private val viewModel: LocalFileShareViewModel by viewModels()

    private lateinit var binding: ActivityFileShareBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileShareBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.itemsData["id_1"] = FileShareItemData(FileInfo(id = "1", uri = "".toUri(), name = "razer.jpg", mimeType = "image/jpeg", sender = "Gianluigi", size = 100L), FileShareItemData.State.Pending, FileShareItemData.Type.DownloadAvailable)
        viewModel.itemsData["id_2"] = FileShareItemData(FileInfo(id = "2", uri = "".toUri(), name = "identity_card.pdf", mimeType = "", sender = "Mario", size = 100L), FileShareItemData.State.Success("".toUri()), FileShareItemData.Type.Download)
        viewModel.itemsData["id_3"] = FileShareItemData(FileInfo(id = "3", uri = "".toUri(), name = "car.zip", mimeType = "application/zip", sender = "Luigi", size = 1000L), FileShareItemData.State.OnProgress(600L), FileShareItemData.Type.Download)
        viewModel.itemsData["id_4"] = FileShareItemData(FileInfo(id = "4", uri = "".toUri(), name = "phone.doc", mimeType = "", sender = "Gianni", size = 23000000L), FileShareItemData.State.Error(Throwable()), FileShareItemData.Type.Upload)
        viewModel.itemsData["id_5"] = FileShareItemData(FileInfo(id = "5", uri = "".toUri(), name = "address.jpg", mimeType = "image/jpeg", sender = "Marco", size = 1000L), FileShareItemData.State.Pending, FileShareItemData.Type.Upload)

        binding.btnFileShare.setOnClickListener { BandyerFileShareDialog().show(this@FileShareActivity, viewModel) }
    }
}

class LocalFileShareViewModel: FileShareViewModel() {
    override val itemsData: ConcurrentHashMap<String, FileShareItemData> = ConcurrentHashMap()

    override fun upload(context: Context, shareItemData: FileShareItemData) = FileShareItemData(FileInfo(id = "6", uri = "".toUri(), name = "", mimeType = "", sender = ""), FileShareItemData.State.Pending, FileShareItemData.Type.Upload)

    override fun download(context: Context, shareItemData: FileShareItemData) = FileShareItemData(FileInfo(id = "7", uri = "".toUri(), name = "", mimeType = "", sender = ""), FileShareItemData.State.Pending, FileShareItemData.Type.Download)

    override fun cancel(shareItemData: FileShareItemData) = Unit
}

