package com.bandyer.sdk_design.filesharing

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.bottom_sheet.BandyerBottomSheetDialog
import com.bandyer.sdk_design.databinding.BandyerFileShareDialogLayoutBinding
import com.bandyer.sdk_design.dialogs.BandyerDialog
import com.bandyer.sdk_design.extensions.*
import com.bandyer.sdk_design.filesharing.adapter_items.BandyerFileTransferItem
import com.bandyer.sdk_design.filesharing.model.TransferData
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import java.util.concurrent.ConcurrentHashMap


/**
 * The file share dialog
 */
class BandyerFileShareDialog: BandyerDialog<BandyerFileShareDialog.FileShareBottomSheetDialog> {

    override var dialog: FileShareBottomSheetDialog? = null

    override val id: String = "bandyerFileSharingDialog"

    override fun show(activity: androidx.fragment.app.FragmentActivity) {
        if (dialog?.isVisible == true || dialog?.isAdded == true) return
        if (dialog == null) dialog = FileShareBottomSheetDialog()

        dialog!!.show(activity.supportFragmentManager, id)
        activity.supportFragmentManager.executePendingTransactions()
    }

    /**
     * It shows the dialog
     *
     * @param activity The activity
     * @param viewModel The activity view model
     * @param itemsData The transfers' data
     */
    fun show(activity: androidx.fragment.app.FragmentActivity, viewModel: FileShareViewModel, itemsData: ConcurrentHashMap<String, TransferData>) {
        if (dialog?.isVisible == true || dialog?.isAdded == true) return
        if (dialog == null) dialog = FileShareBottomSheetDialog(viewModel, itemsData)

        dialog!!.show(activity.supportFragmentManager, id)
        activity.supportFragmentManager.executePendingTransactions()
    }

    /**
     * It update the recycler view items' data
     *
     * @param data The transfers' data
     */
    fun updateItemsData(data: ConcurrentHashMap<String, TransferData>) = dialog?.updateRecyclerViewItems(data)

    /**
     * @suppress
     */
    class FileShareBottomSheetDialog(private var viewModel: FileShareViewModel? = null, private val itemsData: ConcurrentHashMap<String, TransferData>? = null) : BandyerBottomSheetDialog() {

        internal companion object {
            const val PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
            const val MAX_FILE_BYTES = 150 * 1000 * 1000
        }

        private var binding: BandyerFileShareDialogLayoutBinding? = null

        private var dialogLayout: CoordinatorLayout? = null

        private var toolbar: MaterialToolbar? = null

        private var filesRecyclerView: RecyclerView? = null

        private var emptyListLayout: ConstraintLayout? = null

        private var uploadFileFab: LinearLayout? = null

        private var uploadFileFabText: MaterialTextView? = null

        private var itemAdapter: ItemAdapter<BandyerFileTransferItem>? = null

        private var fastAdapter: FastAdapter<BandyerFileTransferItem>? = null

        private var smoothScroller: SmoothScroller? = null

        private val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (!isGranted) showPermissionDeniedDialog(requireContext())
            }

        private var getContent: ActivityResultLauncher<String>? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setStyle(DialogFragment.STYLE_NO_TITLE, requireContext().getCallThemeAttribute(R.styleable.BandyerSDKDesign_Theme_Call_bandyer_fileShareDialogStyle))
            smoothScroller = LinearSmoothScroller(requireContext())
            getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri ?: return@registerForActivityResult
                if(uri.getFileSize(requireContext()) > MAX_FILE_BYTES) {
                    showMaxBytesDialog(requireContext())
                    return@registerForActivityResult
                }

                viewModel?.uploadFile(this.requireContext(), uri = uri,  sender = "")
            }
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            binding = BandyerFileShareDialogLayoutBinding.inflate(inflater, container, false)
            dialogLayout = binding!!.root
            toolbar = binding!!.bandyerToolbar
            filesRecyclerView = binding!!.bandyerRecyclerView
            uploadFileFab = binding!!.bandyerUploadFileFab
            uploadFileFabText = binding!!.bandyerFileShareFabText
            emptyListLayout = binding!!.bandyerEmptyListLayout.root

            itemAdapter = ItemAdapter()
            fastAdapter = FastAdapter.with(itemAdapter!!)

            uploadFileFab?.setOnClickListener { getContent?.launch("*/*") }

            if(itemsData != null) initRecyclerView(filesRecyclerView!!, itemsData)

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

        override fun onDismiss(dialog: DialogInterface) {
            super.onDismiss(dialog)
            binding = null
            dialogLayout = null
            toolbar = null
            filesRecyclerView = null
            uploadFileFab = null
            uploadFileFabText = null
            emptyListLayout = null
            itemAdapter = null
            fastAdapter = null
        }

        override fun onCollapsed() = Unit

        override fun onDialogWillShow() = Unit

        override fun onSlide(offset: Float) = Unit

        override fun onStateChanged(newState: Int) = Unit

        override fun onExpanded() = Unit

        @Suppress("UNCHECKED_CAST")
        private fun initRecyclerView(rv: RecyclerView, itemsData: ConcurrentHashMap<String, TransferData>) {
            rv.adapter = fastAdapter
            rv.layoutManager = LinearLayoutManager(requireContext())
            rv.itemAnimator = null
            rv.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

            fastAdapter!!.addEventHook(BandyerFileTransferItem.ItemClickEvent())

            fastAdapter!!.onClickListener = { _, _, item, _ ->
                onItemClick(item)
                true
            }

            updateRecyclerViewItems(itemsData)
        }

        private fun showPermissionDeniedDialog(context: Context) = AlertDialog.Builder(context, R.style.BandyerSDKDesign_AlertDialogTheme)
            .setTitle(R.string.bandyer_write_permission_dialog_title)
            .setMessage(R.string.bandyer_write_permission_dialog_descr)
            .setCancelable(true)
            .setPositiveButton(R.string.bandyer_button_ok) { di, _ ->
                di.dismiss()
            }.show()

        private fun showMaxBytesDialog(context: Context) = AlertDialog.Builder(context, R.style.BandyerSDKDesign_AlertDialogTheme)
            .setTitle(R.string.bandyer_max_bytes_dialog_title)
            .setMessage(R.string.bandyer_max_bytes_dialog_descr)
            .setCancelable(true)
            .setPositiveButton(R.string.bandyer_button_ok) { di, _ ->
                di.dismiss()
            }.show()

        private fun scrollToTop() = filesRecyclerView?.layoutManager?.startSmoothScroll(smoothScroller?.apply { targetPosition = 0 })

        private fun onItemClick(item: BandyerFileTransferItem) {
            kotlin.runCatching {
                val type = item.data.type
                val state = item.data.state
                if(type is TransferData.Type.DownloadAvailable || state !is TransferData.State.Success) return

                val uri = if(type is TransferData.Type.Upload) item.data.data.uri else state.uri

                if(!doesFileExists(requireContext(), uri))
                    Snackbar.make(dialogLayout as View, R.string.bandyer_fileshare_file_cancelled, Snackbar.LENGTH_SHORT).show()
                else
                    openFileOrShowMessage(requireContext(), uri)
            }
        }

        private fun openFileOrShowMessage(context: Context, uri: Uri) {
            val mimeType = uri.getMimeType(context)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, mimeType)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                context.startActivity(intent)
            } catch (ex: ActivityNotFoundException) {
                Snackbar.make(dialogLayout as View, R.string.bandyer_fileshare_impossible_open_file, Snackbar.LENGTH_SHORT).show()
            }
        }

        private fun doesFileExists(context: Context, uri: Uri): Boolean =
            kotlin.runCatching {
                context.contentResolver.query(uri, null, null, null, null)?.use {
                    it.moveToFirst()
                }
            }.getOrNull() ?: false

        fun updateRecyclerViewItems(data: ConcurrentHashMap<String, TransferData>) {
            uploadFileFabText?.visibility = if(data.isEmpty()) View.VISIBLE else View.GONE
            emptyListLayout?.visibility = if(data.isEmpty()) View.VISIBLE else View.GONE
            val items = arrayListOf<BandyerFileTransferItem>()
            data.values.forEach {
                if(it.type is TransferData.Type.DownloadAvailable) {
                    items.add(BandyerFileTransferItem(it, viewModel!!) { requestPermissionLauncher.launch(PERMISSION) })
                    return@forEach
                }

                if(it.state is TransferData.State.Pending)
                    scrollToTop()

                items.add(BandyerFileTransferItem(it, viewModel!!))
            }

            val sortedItems = items.toMutableList().apply { sortByDescending { item -> item.data.data.creationTime } }
            val diff = FastAdapterDiffUtil.calculateDiff(itemAdapter!!, sortedItems, true)
            FastAdapterDiffUtil[itemAdapter!!] = diff
        }
    }
}