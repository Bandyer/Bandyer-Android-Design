package com.bandyer.sdk_design.filesharing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.solver.state.State
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.bottom_sheet.BandyerBottomSheetDialog
import com.bandyer.sdk_design.databinding.BandyerFileShareDialogLayoutBinding
import com.bandyer.sdk_design.dialogs.BandyerDialog
import com.bandyer.sdk_design.extensions.getCallThemeAttribute
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textview.MaterialTextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.commons.utils.FastAdapterDiffUtil
import com.mikepenz.fastadapter.listeners.EventHook
import com.mikepenz.fastadapter.listeners.OnClickListener
import java.util.concurrent.ConcurrentHashMap


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

        private val viewModel: FileShareViewModel by activityViewModels()

        private var binding: BandyerFileShareDialogLayoutBinding? = null

        private var dialogLayout: CoordinatorLayout? = null

        private var toolbar: MaterialToolbar? = null

        private var filesRecyclerView: RecyclerView? = null

        private var emptyListLayout: ConstraintLayout? = null

        private var uploadFileFab: LinearLayout? = null

        private var uploadFileFabText: MaterialTextView? = null

        private val itemAdapter = ItemAdapter<BandyerFileShareItem<*, *>>()

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
            uploadFileFabText = binding!!.bandyerFileShareFabText
            emptyListLayout = binding!!.bandyerEmptyListLayout.root

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
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.HORIZONTAL))

            fastAdapter.withEventHook(UploadItem.UploadItemClickEvent() as EventHook<IItem<*, *>>)
            fastAdapter.withEventHook(DownloadItem.DownloadItemClickEvent() as EventHook<IItem<*, *>>)
            fastAdapter.withEventHook(DownloadAvailableItem.DownloadAvailableItemClickEvent() as EventHook<IItem<*, *>>)
            fastAdapter.withSelectable(true)
            // TODO add listener behaviour
            fastAdapter.withOnClickListener(OnClickListener<BandyerFileShareItem<*,*>> { v, adapter, item, position -> true } as OnClickListener<IItem<*, *>>)
        }

        fun setFabOnClickCallback(callback: () -> Unit) = uploadFileFab?.setOnClickListener { callback.invoke() }

//        fun addDownloadAvailable(data: DownloadAvailableData) = itemAdapter.add(0, DownloadAvailableItem(data, viewModel))

        fun updateRecyclerViewItems(data: ConcurrentHashMap<String, FileShareItemData>) {
            uploadFileFabText?.visibility = if(data.isEmpty()) View.VISIBLE else View.GONE
            emptyListLayout?.visibility = if(data.isEmpty()) View.VISIBLE else View.GONE
            val items: List<BandyerFileShareItem<*,*>> = data.values.map {
                when(it) {
                    is UploadData -> UploadItem(it, viewModel)
                    is DownloadData -> DownloadItem(it, viewModel)
                    else -> DownloadAvailableItem(it as DownloadAvailableData, viewModel)
                }
            }
            val sortedItems = items.toMutableList().apply { sortByDescending { item -> item.startTime } }
            val diff = FastAdapterDiffUtil.calculateDiff(itemAdapter, sortedItems, true)
            FastAdapterDiffUtil.set(itemAdapter, diff)
        }
    }
}