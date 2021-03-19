package com.bandyer.sdk_design.filesharing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FileShareViewModel : ViewModel() {

    var fileShareItems by mutableStateOf(listOf<FileShareItemData>())
        private set

    init {
        fileShareItems = listOf(
            FileShareItemData(0,"documento_ident.jpg", 0.6f, false, "Gianfranco", FileType.MEDIA, 500),
            FileShareItemData(1,"moto.pdf", 0.8f, false, "Mario", FileType.MISC, 433),
            FileShareItemData(2,"pasqua.zip", 0f, true, "Luigi", FileType.ARCHIVE, 346)
        )
    }

    fun addItem() {
        fileShareItems = fileShareItems + listOf(FileShareItemData(fileShareItems.size, "${fileShareItems.size}_cuffie.jpg", 0f, false, "Francesca", FileType.MEDIA, 785))
    }

    fun updateItem(item: FileShareItemData) {
        val index = fileShareItems.indexOf(item)
        val item1 = fileShareItems[index]
        item1.fileName = "Modified"
        item1.progress = 1.0f
        fileShareItems = fileShareItems.toMutableList().also {
            it[index] = item1
        }
    }

    fun removeItem(item: FileShareItemData) {
        fileShareItems = fileShareItems.toMutableList().also { it.remove(item) }
    }
}