package com.bandyer.sdk_design.filesharing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.bottom_sheet.BandyerBottomSheetDialog
import com.bandyer.sdk_design.chat.adapter_items.message.text.BandyerChatTextMessageItem
import com.bandyer.sdk_design.databinding.BandyerFileShareDialogLayoutBinding
import com.bandyer.sdk_design.dialogs.BandyerDialog
import com.bandyer.sdk_design.extensions.getCallThemeAttribute
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.adapters.ItemAdapter

class BandyerFileShareDialog: BandyerDialog<BandyerFileShareDialog.FileShareBottomSheetDialog> {


    override var dialog: FileShareBottomSheetDialog? = null

    override val id: String = "bandyerFileSharingDialog"

    override fun show(activity: androidx.fragment.app.FragmentActivity) {
        if (dialog?.isVisible == true || dialog?.isAdded == true) return
        if (dialog == null) dialog = FileShareBottomSheetDialog()
        dialog!!.show(activity.supportFragmentManager, id)
        activity.supportFragmentManager.executePendingTransactions()
    }

    class FileShareBottomSheetDialog : BandyerBottomSheetDialog() {

        private var binding: BandyerFileShareDialogLayoutBinding? = null

        private var dialogLayout: CoordinatorLayout? = null

        private var toolbar: MaterialToolbar? = null

        private var filesRecyclerView: RecyclerView? = null

        private var uploadFileFab: ExtendedFloatingActionButton? = null

        private val itemAdapter = ItemAdapter<BandyerFileShareItem>()

        private val fastAdapter = FastAdapter.with<IItem<*, *>, ItemAdapter<*>>(itemAdapter)

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setStyle(DialogFragment.STYLE_NO_TITLE, requireContext().getCallThemeAttribute(R.styleable.BandyerSDKDesign_Theme_Call_bandyer_fileShareDialogStyle))
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            binding = BandyerFileShareDialogLayoutBinding.inflate(inflater, container, false)
            dialogLayout = binding!!.root
            toolbar = binding!!.bandyerToolbar
            filesRecyclerView = binding!!.bandyerRecyclerView
            uploadFileFab = binding!!.bandyerUploadFileFab

            filesRecyclerView?.init()

            return dialogLayout
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            toolbar!!.setNavigationOnClickListener { dismiss() }
        }

        override fun onStart() {
            // Set the dialog full screen
            val parentLayout = dialog?.findViewById<ViewGroup>(R.id.design_bottom_sheet)
            val layoutParams = parentLayout?.layoutParams
            layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
            parentLayout?.layoutParams = layoutParams
            super.onStart()
        }

        override fun onCollapsed() = Unit

        override fun onDialogWillShow() = Unit

        override fun onSlide(offset: Float) = Unit

        override fun onStateChanged(newState: Int) = Unit

        override fun onExpanded() = Unit

        private fun RecyclerView.init() {
            adapter = fastAdapter
            itemAdapter.add(BandyerFileShareItem(FileShareData(FILE_SHARE_OP_TYPE.UPLOAD, "name", "pdf", 34L, "asdasd", "Giulio")))
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.HORIZONTAL))
        }
    }
}