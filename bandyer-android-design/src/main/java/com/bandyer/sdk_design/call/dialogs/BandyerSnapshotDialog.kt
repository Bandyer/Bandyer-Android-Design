/*
 * Copyright 2021-2022 Bandyer @ https://www.bandyer.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.bandyer.sdk_design.call.dialogs

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
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.bottom_sheet.BandyerBottomSheetDialog
import com.bandyer.sdk_design.call.layout.BandyerSnapshotDialogLayout
import com.bandyer.sdk_design.dialogs.BandyerDialog
import com.bandyer.sdk_design.extensions.*

/**
 *
 * @author kristiyan
 */
class BandyerSnapshotDialog : BandyerDialog<BandyerSnapshotDialog.SnapshotDialogFragment> {

    override val id: String = "BandyerSnapshotDialog"

    override var dialog: SnapshotDialogFragment? = null

    override fun show(activity: androidx.fragment.app.FragmentActivity) {
        if (dialog == null) dialog = SnapshotDialogFragment()
        dialog!!.show(activity.supportFragmentManager, id)
    }

    /**
     * @suppress
     */
    class SnapshotDialogFragment : BandyerBottomSheetDialog() {

        private var confirmDialog: AlertDialog? = null

        private var saved = false

        private var zoomy: Zoomy.Builder? = null

        private var rootView: BandyerSnapshotDialogLayout? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setStyle(DialogFragment.STYLE_NO_TITLE, R.style.BandyerSDKDesign_BottomSheetDialog_Snapshot)
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            rootView = BandyerSnapshotDialogLayout(ContextThemeWrapper(context, R.style.BandyerSDKDesign_BottomSheetDialog_Snapshot_Layout))
            return rootView
        }

        override fun onStart() {
            super.onStart()
            dialog ?: return
            val window = dialog!!.window
            window?.findViewById<View>(R.id.container)?.fitsSystemWindows = false
        }

        override fun onConfigurationChanged(newConfig: Configuration) {
            super.onConfigurationChanged(newConfig)
            dismiss()
            BandyerSnapshotDialog().apply {
                context?.scanForFragmentActivity()?.let {
                    this.show(it)
                }
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            // see https://github.com/imablanco/Zoomy/issues/19
            dialog?.window?.decorView?.setOnTouchListener { v, event ->
                rootView?.binding?.bandyerSnapshotPreview?.dispatchTouchEvent(event)
                v.performClick()
            }

            zoomy = Zoomy.Builder(this.dialog).target(rootView?.binding?.bandyerSnapshotPreview)
                    .enableImmersiveMode(false)
                    .interpolator(OvershootInterpolator())
                    .animateZooming(true)
                    .doubleTapListener {
                        rootView?.binding?.bandyerSnapshotShareSaveButton?.visibility = View.VISIBLE
                        rootView?.binding?.bandyerSnapshotShareCloseButton?.visibility = View.VISIBLE
                        rootView?.binding?.bandyerSnapshotShareWhiteboardButton?.visibility = View.VISIBLE
                    }
                    .tapListener {
                        rootView?.binding?.bandyerSnapshotShareSaveButton?.visibility = View.VISIBLE
                        rootView?.binding?.bandyerSnapshotShareCloseButton?.visibility = View.VISIBLE
                        rootView?.binding?.bandyerSnapshotShareWhiteboardButton?.visibility = View.VISIBLE
                    }
                    .zoomListener(object : ZoomListener {
                        override fun onViewEndedZooming(view: View?) {
                            rootView?.binding?.bandyerSnapshotShareSaveButton?.visibility = View.VISIBLE
                            rootView?.binding?.bandyerSnapshotShareCloseButton?.visibility = View.VISIBLE
                            rootView?.binding?.bandyerSnapshotShareWhiteboardButton?.visibility = View.VISIBLE
                        }

                        override fun onViewStartedZooming(view: View?) {
                            rootView?.binding?.bandyerSnapshotShareSaveButton?.visibility = View.INVISIBLE
                            rootView?.binding?.bandyerSnapshotShareCloseButton?.visibility = View.INVISIBLE
                            rootView?.binding?.bandyerSnapshotShareWhiteboardButton?.visibility = View.INVISIBLE
                        }
                    })
            zoomy!!.register()

            rootView?.binding?.bandyerSnapshotShareSaveButton?.setOnClickListener {
                saved = true
                Toast.makeText(context, context?.resources?.getString(R.string.bandyer_snapshot_saved_in_gallery), Toast.LENGTH_SHORT).show()
            }


            rootView?.binding?.bandyerSnapshotShareCloseButton?.setOnClickListener {
                if (saved) {
                    dismiss()
                    return@setOnClickListener
                }
                showDialog(context?.resources?.getString(R.string.bandyer_alert_discard_snapshot_title) ?: "",
                        context?.resources?.getString(R.string.bandyer_alert_discard_snapshot_message) ?: "", {
                    dismiss()
                }, {

                })

            }

            rootView?.binding?.bandyerSnapshotShareWhiteboardButton?.setOnClickListener {
                if (!saved)
                    showDialog(context?.resources?.getString(R.string.bandyer_alert_save_snapshot_title) ?: "",
                            context?.resources?.getString(R.string.bandyer_alert_save_snapshot_message) ?: "", {

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
            Zoomy.unregister(rootView?.binding?.bandyerSnapshotPreview)
        }

        private fun showDialog(title: String, body: String, positiveCallback: () -> Unit, negativeCallback: () -> Unit) {
            confirmDialog = AlertDialog.Builder(requireContext(), R.style.BandyerSDKDesign_AlertDialogTheme)
                    .setTitle(title)
                    .setCancelable(true)
                    .setPositiveButton(context?.resources?.getString(R.string.bandyer_confirm_message)) { dialog, which ->
                        positiveCallback.invoke()
                    }
                    .setNegativeButton(context?.resources?.getString(R.string.bandyer_cancel_message)) { dialog, which ->
                        negativeCallback.invoke()
                    }
                    .setMessage(body)
                    .show()
        }
    }
}