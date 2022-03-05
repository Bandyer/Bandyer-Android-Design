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

package com.kaleyra.collaboration_suite_phone_ui.whiteboard.layout

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.*
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.kaleyra.collaboration_suite_phone_ui.buttons.KaleyraActionButton
import com.kaleyra.collaboration_suite_phone_ui.databinding.KaleyraWhiteboardTextEditorBinding
import com.kaleyra.collaboration_suite_phone_ui.whiteboard.KaleyraCancelActionButton
import com.kaleyra.collaboration_suite_phone_ui.whiteboard.KaleyraCancelActionButtonState
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.kaleyra.collaboration_suite_phone_ui.extensions.hideKeyboard
import com.kaleyra.collaboration_suite_phone_ui.extensions.showKeyboard


/**
 * @suppress
 * @author kristiyan
 */
class KaleyraWhiteboardTextEditorLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RelativeLayout(context, attrs, defStyleAttr) {

    var cancelInputButton: KaleyraCancelActionButton? = null
        private set

    var acceptInputButton: KaleyraActionButton? = null
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

    val binding: KaleyraWhiteboardTextEditorBinding by lazy { KaleyraWhiteboardTextEditorBinding.inflate(LayoutInflater.from(context)) }

    init {
        binding.root.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        addView(binding.root)

        acceptInputButton = binding.kaleyraAcceptButton
        cancelInputButton = binding.kaleyraCancelButton
        inputTextField = binding.kaleyraWhiteboardEditText
        dataLossMessage = binding.kaleyraWhiteboardDataLossTextView
        actionMenu = binding.kaleyraWhiteboardActions

        tapTextAreaGestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                inputTextField?.performClick()
                inputTextField?.requestFocus()
                inputTextField?.requestFocusFromTouch()
                binding.kaleyraWhiteboardEditText.showKeyboard()
                return true
            }
        })

        binding.kaleyraTextArea.setOnTouchListener { v, event ->
            v.performClick()
            tapTextAreaGestureDetector?.onTouchEvent(event) == true
        }

        cancelInputButton?.state = KaleyraCancelActionButtonState.DISMISS
    }

    fun showDiscardChangesMode() {
        (binding.kaleyraTextArea.parent as View).animate().alpha(1.0f).duration = 300
        binding.kaleyraTextArea.visibility = View.GONE
        binding.kaleyraWhiteboardDataLossTextView.visibility = View.VISIBLE
        binding.kaleyraWhiteboardDataLossTextView.animate().alpha(1.0f).duration = 300
        cancelInputButton?.state = KaleyraCancelActionButtonState.CANCEL
        binding.kaleyraWhiteboardEditText.hideKeyboard()
    }

    fun getDiscardChangesModeHeight(): Int {
        binding.kaleyraWhiteboardDataLossTextView
        binding.kaleyraWhiteboardActions
        val dataLossHeight = getTextViewHeight(binding.kaleyraWhiteboardDataLossTextView)
        val lp = binding.kaleyraWhiteboardActions.layoutParams as MarginLayoutParams
        return dataLossHeight + binding.kaleyraWhiteboardActions.height + lp.bottomMargin
    }

    fun setTextAsChanged(changed: Boolean) {
        cancelInputButton?.state = if (changed) KaleyraCancelActionButtonState.DISCARD_CHANGES
                              else KaleyraCancelActionButtonState.DISMISS
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
        (binding.kaleyraTextArea.parent as View).animate().alpha(1.0f).duration = 300
        binding.kaleyraTextArea.visibility = View.VISIBLE
        binding.kaleyraWhiteboardDataLossTextView.visibility = View.GONE
        binding.kaleyraTextArea.animate().alpha(1.0f).duration = 300
        setTextAsChanged(withChanges)
    }

    fun alphaAnimateTextArea(alpha: Float) {
        (binding.kaleyraTextArea.parent as View).alpha = alpha
        (binding.kaleyraTextArea.parent as View).requestLayout()
    }

}