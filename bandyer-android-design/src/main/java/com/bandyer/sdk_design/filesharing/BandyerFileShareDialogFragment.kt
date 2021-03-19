package com.bandyer.sdk_design.filesharing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import com.bandyer.sdk_design.R
import com.google.android.material.composethemeadapter.MdcTheme

abstract class BandyerFileShareDialogFragment: DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // In order for savedState to work, the same ID needs to be used for all instances.
            id = R.id.bandyer_id_file_share_fragment

            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            setContent {
                MdcTheme {
                    FileShare(sharedFiles = (::getSharedFiles)(),
                        onNavIconPressed = { dismiss() },
                        onAddButtonPressed = { (::onAddButtonPressed)() },
                        onItemEvent = { event -> (::onItemEvent)(event) },
                        onItemButtonEvent = { event -> (::onItemButtonEvent)(event) }
                    )
                }
            }
        }
    }

    abstract fun getSharedFiles(): List<FileShareItemData>

    abstract fun onAddButtonPressed()

    abstract fun onItemEvent(event: FileShareItemEvent)

    abstract fun onItemButtonEvent(event: FileShareItemButtonEvent)
}