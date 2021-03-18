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

    override fun onStart() {
        val parentLayout = dialog?.findViewById<ViewGroup>(R.id.content)
        val layoutParams = parentLayout?.layoutParams
        layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams?.width = ViewGroup.LayoutParams.MATCH_PARENT
        parentLayout?.layoutParams = layoutParams
        super.onStart()
    }

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
                    FileShare(sharedFiles = listOf(
                        FileShareData(0,"documento_ident.jpg", 0.6f, false, "Gianfranco", FileType.MEDIA, 500),
                        FileShareData(1,"moto.pdf", 0.8f, false, "Mario", FileType.MISC, 433),
                        FileShareData(2,"pasqua.zip", 0f, true, "Luigi", FileType.ARCHIVE, 346)
                    ),
                        onNavIconPressed = { dismiss() },
                        onAddButtonPressed = { (::onAddButtonPressed)() },
                        onItemEvent = { event -> (::onItemEvent)(event) },
                        onItemButtonEvent = { event -> (::onItemButtonEvent)(event) }
                    )
                }
            }
        }
    }

    abstract fun onAddButtonPressed()

    abstract fun onItemEvent(event: FileShareItemEvent)

    abstract fun onItemButtonEvent(event: FileShareItemButtonEvent)
}