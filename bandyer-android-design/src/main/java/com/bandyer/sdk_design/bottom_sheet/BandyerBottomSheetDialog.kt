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

package com.bandyer.sdk_design.bottom_sheet

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.bottom_sheet.behaviours.BandyerBottomSheetBehaviour
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


/**
 * @suppress
 */
abstract class BandyerBottomSheetDialog : BottomSheetDialogFragment() {

    abstract fun onExpanded()

    abstract fun onCollapsed()

    abstract fun onDialogWillShow()

    abstract fun onSlide(offset: Float)

    abstract fun onStateChanged(@BandyerBottomSheetBehaviour.State newState: Int)

    protected var behavior: BottomSheetBehavior<View>? = null

    protected var isUserAction = false

    private var mDismissListener: DialogInterface.OnDismissListener? = null

    open val isDraggable = false

    open val isDismissable = false

    private val bottomSheetBehavior = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(p0: View, slideOffset: Float) {
            val offset = if (slideOffset < 0) 0f else slideOffset
            onSlide(offset)
        }

        override fun onStateChanged(p0: View, newState: Int) {
            onStateChanged(newState)
            when (newState) {
                BottomSheetBehavior.STATE_EXPANDED -> {
                    isUserAction = false
                    onExpanded()
                }
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    isUserAction = false
                    onCollapsed()
                }
                else -> {
                }
            }
        }
    }

    fun setDismissListener(listener: DialogInterface.OnDismissListener) {
        mDismissListener = listener
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setOnShowListener { dialog ->
                setupBottomSheetBehaviour()
                onDialogWillShow()
                isCancelable = isDismissable
            }
        }
    }

    private fun setupBottomSheetBehaviour() {
        val bottomSheet = (dialog as BottomSheetDialog).findViewById(R.id.design_bottom_sheet) as FrameLayout?
        val params = bottomSheet!!.layoutParams as CoordinatorLayout.LayoutParams
        params.behavior = BottomSheetBehavior<View>(bottomSheet.context, null)
        bottomSheet.layoutParams = params
        behavior = BottomSheetBehavior.from(bottomSheet) as BottomSheetBehavior<View>
        behavior!!.state = BottomSheetBehavior.STATE_EXPANDED
        behavior!!.isDraggable = isDraggable
        behavior!!.peekHeight = 0
        behavior!!.addBottomSheetCallback(bottomSheetBehavior)
    }

    /**
     * @suppress
     * @see [here](https://medium.com/square-corner-blog/a-small-leak-will-sink-a-great-ship-efbae00f9a0f)
     */
    private fun flushStackLocalLeaks(looper: Looper) {
        val handler = Handler(looper)
        handler.post {
            Looper.myQueue().addIdleHandler {
                handler.sendMessageDelayed(handler.obtainMessage(), 1000)
                true
            }
        }
    }

    override fun dismiss() {
        clean()
        if (isVisible && !isStateSaved) super.dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        clean()
    }

    private fun clean() {
        Looper.myLooper()?.let {
            flushStackLocalLeaks(it)
        }
        mDismissListener?.onDismiss(dialog)
        behavior?.removeBottomSheetCallback(bottomSheetBehavior)
        behavior = null
        mDismissListener = null
    }

    override fun setCancelable(cancelable: Boolean) {
        dialog ?: return
        val touchOutsideView: View = dialog!!.window!!.decorView.findViewById(R.id.touch_outside)
        behavior?.isHideable = cancelable
        if (cancelable) {
            touchOutsideView.setOnClickListener {
                if (dialog!!.isShowing) dialog!!.cancel()
            }
        } else touchOutsideView.setOnClickListener(null)
    }

    protected fun collapse() {
        isUserAction = true
        behavior?.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    protected fun expand() {
        isUserAction = true
        behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

}