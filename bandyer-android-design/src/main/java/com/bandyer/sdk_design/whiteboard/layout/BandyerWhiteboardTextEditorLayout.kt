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

package com.bandyer.sdk_design.whiteboard.layout

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.*
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.bandyer.sdk_design.buttons.BandyerActionButton
import com.bandyer.sdk_design.databinding.BandyerWhiteboardTextEditorBinding
import com.bandyer.sdk_design.extensions.hideKeyboard
import com.bandyer.sdk_design.extensions.showKeyboard
import com.bandyer.sdk_design.whiteboard.BandyerCancelActionButton
import com.bandyer.sdk_design.whiteboard.BandyerCancelActionButtonState
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView


/**
 * @suppress
 * @author kristiyan
 */
class BandyerWhiteboardTextEditorLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RelativeLayout(context, attrs, defStyleAttr) {

    var cancelInputButton: BandyerCancelActionButton? = null
        private set

    var acceptInputButton: BandyerActionButton? = null
        private set

    var inputTextField: TextInputEditText? = null
        private set

    var dataLossMessage: MaterialTextView? = null
        private set

    var actionMenu: LinearLayout? = null
        private set

    private var tapTextAreaGestureDetector: GestureDetector? = null

    val text: String
        get() = inputTextField!!.text!!.toString()

    val binding: BandyerWhiteboardTextEditorBinding by lazy { BandyerWhiteboardTextEditorBinding.inflate(LayoutInflater.from(context)) }

    init {
        binding.root.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        addView(binding.root)

        acceptInputButton = binding.bandyerAcceptButton
        cancelInputButton = binding.bandyerCancelButton
        inputTextField = binding.bandyerWhiteboardEditText
        dataLossMessage = binding.bandyerWhiteboardDataLossTextView
        actionMenu = binding.bandyerWhiteboardActions

        tapTextAreaGestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                inputTextField?.performClick()
                inputTextField?.requestFocus()
                inputTextField?.requestFocusFromTouch()
                binding.bandyerWhiteboardEditText.showKeyboard()
                return true
            }
        })

        binding.bandyerTextArea.setOnTouchListener { v, event ->
            v.performClick()
            tapTextAreaGestureDetector?.onTouchEvent(event) == true
        }

        cancelInputButton?.state = BandyerCancelActionButtonState.DISMISS
    }

    fun showDiscardChangesMode() {
        (binding.bandyerTextArea.parent as View).animate().alpha(1.0f).duration = 300
        binding.bandyerTextArea.visibility = View.GONE
        binding.bandyerWhiteboardDataLossTextView.visibility = View.VISIBLE
        binding.bandyerWhiteboardDataLossTextView.animate().alpha(1.0f).duration = 300
        cancelInputButton?.state = BandyerCancelActionButtonState.CANCEL
        binding.bandyerWhiteboardEditText.hideKeyboard()
    }

    fun getDiscardChangesModeHeight(): Int {
        binding.bandyerWhiteboardDataLossTextView
        binding.bandyerWhiteboardActions
        val dataLossHeight = getTextViewHeight(binding.bandyerWhiteboardDataLossTextView)
        val lp = binding.bandyerWhiteboardActions.layoutParams as MarginLayoutParams
        return dataLossHeight + binding.bandyerWhiteboardActions.height + lp.bottomMargin
    }

    fun setTextAsChanged(changed: Boolean) {
        cancelInputButton?.state = if (changed) BandyerCancelActionButtonState.DISCARD_CHANGES
                              else BandyerCancelActionButtonState.DISMISS
    }

    /**
     * Get the TextView height before the TextView will render
     * @param textView the TextView to measure
     * @return the height of the textView
     */
    fun getTextViewHeight(textView: TextView): Int {
        val deviceWidth = Resources.getSystem().displayMetrics.widthPixels

        val widthMeasureSpec = MeasureSpec.makeMeasureSpec(deviceWidth, MeasureSpec.AT_MOST)
        val heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        textView.measure(widthMeasureSpec, heightMeasureSpec)
        return textView.measuredHeight
    }


    fun showEditMode(withChanges: Boolean = false) {
        (binding.bandyerTextArea.parent as View).animate().alpha(1.0f).duration = 300
        binding.bandyerTextArea.visibility = View.VISIBLE
        binding.bandyerWhiteboardDataLossTextView.visibility = View.GONE
        binding.bandyerTextArea.animate().alpha(1.0f).duration = 300
        setTextAsChanged(withChanges)
    }

    fun alphaAnimateTextArea(alpha: Float) {
        (binding.bandyerTextArea.parent as View).alpha = alpha
        (binding.bandyerTextArea.parent as View).requestLayout()
    }

}