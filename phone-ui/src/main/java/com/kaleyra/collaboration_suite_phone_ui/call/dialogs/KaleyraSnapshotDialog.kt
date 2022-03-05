/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_phone_ui.call.dialogs

import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import com.ablanco.zoomy.ZoomListener
import com.ablanco.zoomy.Zoomy
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.KaleyraBottomSheetDialog
import com.kaleyra.collaboration_suite_phone_ui.call.layout.KaleyraSnapshotDialogLayout
import com.kaleyra.collaboration_suite_phone_ui.dialogs.KaleyraDialog
import com.kaleyra.collaboration_suite_phone_ui.extensions.*

/**
 *
 * @author kristiyan
 */
class KaleyraSnapshotDialog : KaleyraDialog<KaleyraSnapshotDialog.SnapshotDialogFragment> {

    override val id: String = "KaleyraSnapshotDialog"

    override var dialog: SnapshotDialogFragment? = null

    override fun show(activity: androidx.fragment.app.FragmentActivity) {
        if (dialog == null) dialog = SnapshotDialogFragment()
        dialog!!.show(activity.supportFragmentManager, id)
    }

    /**
     * @suppress
     */
    class SnapshotDialogFragment : KaleyraBottomSheetDialog() {

        private var confirmDialog: AlertDialog? = null

        private var saved = false

        private var zoomy: Zoomy.Builder? = null

        private var rootView: KaleyraSnapshotDialogLayout? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setStyle(DialogFragment.STYLE_NO_TITLE, R.style.KaleyraCollaborationSuiteUI_BottomSheetDialog_Snapshot)
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            rootView = KaleyraSnapshotDialogLayout(ContextThemeWrapper(context, R.style.KaleyraCollaborationSuiteUI_BottomSheetDialog_Snapshot_Layout))
            return rootView
        }

        override fun onStart() {
            super.onStart()
            dialog ?: return
            dialog!!.window!!.decorView.fitsSystemWindows = false
        }

        override fun onConfigurationChanged(newConfig: Configuration) {
            super.onConfigurationChanged(newConfig)
            dismiss()
            KaleyraSnapshotDialog().apply {
                context?.scanForFragmentActivity()?.let {
                    this.show(it)
                }
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            // see https://github.com/imablanco/Zoomy/issues/19
            dialog?.window?.decorView?.setOnTouchListener { v, event ->
                rootView?.binding?.kaleyraSnapshotPreview?.dispatchTouchEvent(event)
                v.performClick()
            }

            zoomy = Zoomy.Builder(this.dialog).target(rootView?.binding?.kaleyraSnapshotPreview)
                    .enableImmersiveMode(false)
                    .interpolator(OvershootInterpolator())
                    .animateZooming(true)
                    .doubleTapListener {
                        rootView?.binding?.kaleyraSnapshotShareSaveButton?.visibility = View.VISIBLE
                        rootView?.binding?.kaleyraSnapshotShareCloseButton?.visibility = View.VISIBLE
                        rootView?.binding?.kaleyraSnapshotShareWhiteboardButton?.visibility = View.VISIBLE
                    }
                    .tapListener {
                        rootView?.binding?.kaleyraSnapshotShareSaveButton?.visibility = View.VISIBLE
                        rootView?.binding?.kaleyraSnapshotShareCloseButton?.visibility = View.VISIBLE
                        rootView?.binding?.kaleyraSnapshotShareWhiteboardButton?.visibility = View.VISIBLE
                    }
                    .zoomListener(object : ZoomListener {
                        override fun onViewEndedZooming(view: View?) {
                            rootView?.binding?.kaleyraSnapshotShareSaveButton?.visibility = View.VISIBLE
                            rootView?.binding?.kaleyraSnapshotShareCloseButton?.visibility = View.VISIBLE
                            rootView?.binding?.kaleyraSnapshotShareWhiteboardButton?.visibility = View.VISIBLE
                        }

                        override fun onViewStartedZooming(view: View?) {
                            rootView?.binding?.kaleyraSnapshotShareSaveButton?.visibility = View.INVISIBLE
                            rootView?.binding?.kaleyraSnapshotShareCloseButton?.visibility = View.INVISIBLE
                            rootView?.binding?.kaleyraSnapshotShareWhiteboardButton?.visibility = View.INVISIBLE
                        }
                    })
            zoomy!!.register()

            rootView?.binding?.kaleyraSnapshotShareSaveButton?.setOnClickListener {
                saved = true
                Toast.makeText(context, context?.resources?.getString(R.string.kaleyra_snapshot_saved_in_gallery), Toast.LENGTH_SHORT).show()
            }


            rootView?.binding?.kaleyraSnapshotShareCloseButton?.setOnClickListener {
                if (saved) {
                    dismiss()
                    return@setOnClickListener
                }
                showDialog(context?.resources?.getString(R.string.kaleyra_alert_discard_snapshot_title) ?: "",
                        context?.resources?.getString(R.string.kaleyra_alert_discard_snapshot_message) ?: "", {
                    dismiss()
                }, {

                })

            }

            rootView?.binding?.kaleyraSnapshotShareWhiteboardButton?.setOnClickListener {
                if (!saved)
                    showDialog(context?.resources?.getString(R.string.kaleyra_alert_save_snapshot_title) ?: "",
                            context?.resources?.getString(R.string.kaleyra_alert_save_snapshot_message) ?: "", {

                    }, {

                    })
            }
        }

        override fun onExpanded() {}

        override fun onCollapsed() {}

        override fun onDialogWillShow() {
        }

        override fun onSlide(offset: Float) {}

        override fun onStateChanged(newState: Int) {}

        override fun onDismiss(dialog: DialogInterface) {
            super.onDismiss(dialog)
            confirmDialog?.dismiss()
            Zoomy.unregister(rootView?.binding?.kaleyraSnapshotPreview)
        }

        private fun showDialog(title: String, body: String, positiveCallback: () -> Unit, negativeCallback: () -> Unit) {
            confirmDialog = AlertDialog.Builder(requireContext(), R.style.KaleyraCollaborationSuiteUI_AlertDialogTheme)
                    .setTitle(title)
                    .setCancelable(true)
                    .setPositiveButton(context?.resources?.getString(R.string.kaleyra_confirm_message)) { dialog, which ->
                        positiveCallback.invoke()
                    }
                    .setNegativeButton(context?.resources?.getString(R.string.kaleyra_cancel_message)) { dialog, which ->
                        negativeCallback.invoke()
                    }
                    .setMessage(body)
                    .show()
        }
    }
}