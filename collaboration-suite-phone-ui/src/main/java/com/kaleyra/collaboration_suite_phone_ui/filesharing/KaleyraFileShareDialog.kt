/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_phone_ui.filesharing

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.KaleyraBottomSheetDialog
import com.kaleyra.collaboration_suite_phone_ui.databinding.KaleyraFileShareDialogLayoutBinding
import com.kaleyra.collaboration_suite_phone_ui.dialogs.KaleyraDialog
import com.kaleyra.collaboration_suite_phone_ui.extensions.*
import com.kaleyra.collaboration_suite_phone_ui.filesharing.adapter_items.KaleyraFileTransferItem
import com.kaleyra.collaboration_suite_phone_ui.filesharing.model.TransferData
import com.kaleyra.collaboration_suite_phone_ui.utils.LastDividerItemDecoration
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil

/**
 * The file share dialog
 */
class KaleyraFileShareDialog : KaleyraDialog<KaleyraFileShareDialog.FileShareBottomSheetDialog> {

    override var dialog: FileShareBottomSheetDialog? = null

    override val id: String = "KaleyraFileSharingDialog"

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
     * @param pickFileCallback The callback to be executed on fab click
     */
    fun show(activity: androidx.fragment.app.FragmentActivity, viewModel: FileShareViewModel, pickFileCallback: () -> Unit) {
        if (dialog?.isVisible == true || dialog?.isAdded == true) return
        if (dialog == null) dialog = FileShareBottomSheetDialog(viewModel, pickFileCallback)

        dialog!!.show(activity.supportFragmentManager, id)
        activity.supportFragmentManager.executePendingTransactions()
    }

    /**
     * Update the recycler view items' data
     */
    fun notifyDataSetChanged() = dialog?.notifyDataSetChanged()

    /**
     * @suppress
     */
    class FileShareBottomSheetDialog(private val viewModel: FileShareViewModel? = null, private val pickFileCallback: (() -> Unit)? = null) : KaleyraBottomSheetDialog() {

        internal companion object {
            const val PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
        }

        private var binding: KaleyraFileShareDialogLayoutBinding? = null

        private var dialogLayout: CoordinatorLayout? = null

        private var toolbar: MaterialToolbar? = null

        private var filesRecyclerView: RecyclerView? = null

        private var emptyListLayout: ConstraintLayout? = null

        private var uploadFileFab: LinearLayout? = null

        private var uploadFileFabText: MaterialTextView? = null

        private var itemAdapter: ItemAdapter<KaleyraFileTransferItem>? = null

        private var fastAdapter: FastAdapter<KaleyraFileTransferItem>? = null

        private var smoothScroller: SmoothScroller? = null

        private var permissionGrantedCallback: (() -> Unit)? = null

        private var requestPermissionLauncher: ActivityResultLauncher<String>? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setStyle(DialogFragment.STYLE_NO_TITLE, requireContext().getCallThemeAttribute(R.styleable.KaleyraCollaborationSuiteUI_Theme_Call_kaleyra_fileShareDialogStyle))
            smoothScroller = LinearSmoothScroller(requireContext())
            requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) permissionGrantedCallback?.invoke()
            }
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            binding = KaleyraFileShareDialogLayoutBinding.inflate(inflater, container, false)
            dialogLayout = binding!!.root
            toolbar = binding!!.kaleyraToolbar
            filesRecyclerView = binding!!.kaleyraRecyclerView
            uploadFileFab = binding!!.kaleyraUploadFileFab
            uploadFileFabText = binding!!.kaleyraFileShareFabText
            emptyListLayout = binding!!.kaleyraEmptyListLayout.root

            itemAdapter = ItemAdapter()
            fastAdapter = FastAdapter.with(itemAdapter!!)

            uploadFileFab?.setOnClickListener { pickFileCallback?.invoke() }

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
            rv.isFocusable = false
            rv.isFocusableInTouchMode = false
            rv.addItemDecoration(LastDividerItemDecoration(requireContext()))

            fastAdapter!!.addEventHook(KaleyraFileTransferItem.ItemClickEvent(viewModel!!) { callback ->
                permissionGrantedCallback = callback
                requestPermissionLauncher?.launch(PERMISSION)
            })

            fastAdapter!!.onClickListener = { _, _, item, _ ->
                onItemClick(item)
                true
            }

            notifyDataSetChanged()
        }

        private fun scrollToTop() = filesRecyclerView?.layoutManager?.startSmoothScroll(smoothScroller?.apply { targetPosition = 0 })

        private fun onItemClick(item: KaleyraFileTransferItem) {
            kotlin.runCatching {
                val type = item.data.type
                val state = item.data.state
                if (state !is TransferData.State.Success) return

                val uri = if (type is TransferData.Type.Upload) item.data.uri else item.data.successUri!!

                if (!doesFileExists(requireContext(), uri))
                    Snackbar.make(dialogLayout as View, R.string.kaleyra_fileshare_file_cancelled, Snackbar.LENGTH_SHORT).show()
                else
                    openFileOrShowMessage(requireContext(), uri)
            }
        }

        private fun openFileOrShowMessage(context: Context, uri: Uri) {
            val mimeType = uri.getMimeType(context)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, mimeType)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            try {
                context.startActivity(intent)
            } catch (ex: ActivityNotFoundException) {
                Snackbar.make(dialogLayout as View, R.string.kaleyra_fileshare_impossible_open_file, Snackbar.LENGTH_SHORT).show()
            }
        }

        private fun doesFileExists(context: Context, uri: Uri): Boolean =
            kotlin.runCatching {
                context.contentResolver.query(uri, null, null, null, null)?.use {
                    it.moveToFirst()
                }
            }.getOrNull() ?: false

        fun notifyDataSetChanged() {
            val data = viewModel?.itemsData ?: return
            uploadFileFabText?.visibility = if (data.isEmpty()) View.VISIBLE else View.GONE
            emptyListLayout?.visibility = if (data.isEmpty()) View.VISIBLE else View.GONE
            val items = arrayListOf<KaleyraFileTransferItem>()
            data.values.forEach {
                if (it.state is TransferData.State.Available) {
                    items.add(KaleyraFileTransferItem(it))
                    return@forEach
                }

                if (it.state is TransferData.State.Pending)
                    scrollToTop()

                items.add(KaleyraFileTransferItem(it))
            }

            val sortedItems = items.toMutableList().apply { sortByDescending { item -> item.data.creationTime } }
            val diff = FastAdapterDiffUtil.calculateDiff(itemAdapter!!, sortedItems, true)
            FastAdapterDiffUtil[itemAdapter!!] = diff
        }
    }
}