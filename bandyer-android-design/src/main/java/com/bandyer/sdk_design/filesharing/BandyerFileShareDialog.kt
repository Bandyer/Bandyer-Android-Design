package com.bandyer.sdk_design.filesharing

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
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
import com.bandyer.sdk_design.extensions.getCallThemeAttribute
import com.bandyer.sdk_design.extensions.getMimeType
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
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

    fun show(activity: androidx.fragment.app.FragmentActivity, viewModel: FileShareViewModel) {
        if (dialog?.isVisible == true || dialog?.isAdded == true) return
        if (dialog == null) dialog = FileShareBottomSheetDialog(viewModel)

        dialog!!.show(activity.supportFragmentManager, id)
        activity.supportFragmentManager.executePendingTransactions()
    }

    fun updateRecyclerViewItems(data: ConcurrentHashMap<String, FileShareItemData>) = dialog?.updateRecyclerViewItems(data)

    class FileShareBottomSheetDialog(private var viewModel: FileShareViewModel? = null) : BandyerBottomSheetDialog() {

        internal companion object {
            const val PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
        }

        private var binding: BandyerFileShareDialogLayoutBinding? = null

        private var dialogLayout: CoordinatorLayout? = null

        private var toolbar: MaterialToolbar? = null

        private var filesRecyclerView: RecyclerView? = null

        private var emptyListLayout: ConstraintLayout? = null

        private var uploadFileFab: LinearLayout? = null

        private var uploadFileFabText: MaterialTextView? = null

        private var itemAdapter: ItemAdapter<BandyerFileShareItem<*, *>>? = null

        private var fastAdapter: FastAdapter<IItem<*, *>>? = null

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
                viewModel?.upload(uploadId = null, context = this.requireContext(), uri = uri)
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

            itemAdapter = ItemAdapter<BandyerFileShareItem<*, *>>()
            fastAdapter = FastAdapter.with<IItem<*, *>, ItemAdapter<*>>(itemAdapter)

            uploadFileFab?.setOnClickListener { getContent?.launch("*/*") }

            initRecyclerView(filesRecyclerView!!)

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
        private fun initRecyclerView(rv: RecyclerView) {
            rv.adapter = fastAdapter
            rv.layoutManager = LinearLayoutManager(requireContext())
            rv.itemAnimator = null
            rv.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

            fastAdapter!!.withEventHook(UploadItem.UploadItemClickEvent() as EventHook<IItem<*, *>>)
            fastAdapter!!.withEventHook(DownloadItem.DownloadItemClickEvent() as EventHook<IItem<*, *>>)
            fastAdapter!!.withEventHook(DownloadAvailableItem.DownloadAvailableItemClickEvent() as EventHook<IItem<*, *>>)
            fastAdapter!!.withSelectable(true)

            fastAdapter!!.withOnClickListener(OnClickListener<BandyerFileShareItem<*,*>> { _, _, item, _ ->
                onItemClick(item)
                true
            } as OnClickListener<IItem<*, *>>)

            if (viewModel != null) updateRecyclerViewItems(viewModel!!.itemsData)
        }

        private fun onItemClick(item: BandyerFileShareItem<*,*>) {
            kotlin.runCatching {
                val uri = when (item) {
                    is UploadItem -> item.data.uri
                    is DownloadItem -> if(item.data is DownloadData.Success) item.data.uri else null
                    else -> null
                } ?: return

                val isFileInTrash = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { isFileInTrash(requireContext(), uri) } else false

                if(!doesFileExists(requireContext(), uri))
                    Snackbar.make(dialogLayout as View, R.string.bandyer_fileshare_file_cancelled, Snackbar.LENGTH_SHORT).show()
                else if (isFileInTrash)
                    Snackbar.make(dialogLayout as View, R.string.bandyer_fileshare_file_trashed, Snackbar.LENGTH_SHORT).show()
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
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                val doesExist = cursor != null && cursor.moveToFirst()
                cursor?.close()
                doesExist
            }.getOrNull() ?: false

        @RequiresApi(Build.VERSION_CODES.R)
        private fun isFileInTrash(context: Context, uri: Uri): Boolean {
            return kotlin.runCatching {
                val cursor = context.contentResolver.query(uri, null, MediaStore.MediaColumns.IS_TRASHED, null, null)
                val isInTrash = cursor != null && cursor.moveToFirst()
                cursor?.close()
                return isInTrash
            }.getOrNull() ?: false
        }

        private fun showPermissionDeniedDialog(context: Context) = AlertDialog.Builder(context, R.style.BandyerSDKDesign_AlertDialogTheme)
            .setTitle(R.string.bandyer_write_permission_dialog_title)
            .setMessage(R.string.bandyer_write_permission_dialog_descr)
            .setCancelable(true)
            .setPositiveButton(R.string.bandyer_button_ok) { di, _ ->
                di.dismiss()
            }.show()

        private fun scrollToTop() = filesRecyclerView?.layoutManager?.startSmoothScroll(smoothScroller?.apply { targetPosition = 0 })

        fun updateRecyclerViewItems(data: ConcurrentHashMap<String, FileShareItemData>) {
            uploadFileFabText?.visibility = if(data.isEmpty()) View.VISIBLE else View.GONE
            emptyListLayout?.visibility = if(data.isEmpty()) View.VISIBLE else View.GONE
            val items = arrayListOf<BandyerFileShareItem<*,*>>()
            data.values.forEach {
                if(it is UploadData.Pending || it is DownloadData.Pending)
                    scrollToTop()

                when(it) {
                    is UploadData -> UploadItem(it, viewModel!!)
                    is DownloadData -> DownloadItem(it, viewModel!!)
                    is DownloadAvailableData -> DownloadAvailableItem(it, viewModel!!) {
                        requestPermissionLauncher.launch(PERMISSION)
                    }
                    else -> null
                }?.let { item -> items.add(item) }
            }
            val sortedItems = items.toMutableList().apply { sortByDescending { item -> item.startTime } }
            val diff = FastAdapterDiffUtil.calculateDiff(itemAdapter, sortedItems, true)
            FastAdapterDiffUtil.set(itemAdapter, diff)
        }
    }
}