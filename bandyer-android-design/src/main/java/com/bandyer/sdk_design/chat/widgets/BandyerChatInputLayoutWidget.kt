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

package com.bandyer.sdk_design.chat.widgets

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerWidgetChatInputLayoutBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

/**
 * @suppress
 */
class BandyerChatInputLayoutWidget @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.bandyer_rootLayoutStyle)
    : ConstraintLayout(context, attrs, defStyleAttr) {

    var input: TextInputEditText? = null

    var send: MaterialButton? = null

    var callback: BandyerChatInputLayoutEventListener? = null

    private var textWatcher: TextWatcher? = null

    val binding: BandyerWidgetChatInputLayoutBinding by lazy { BandyerWidgetChatInputLayoutBinding.inflate(LayoutInflater.from(context), this) }

    init {
        input = binding.bandyerMessageEditText

        send = binding.bandyerSendButton
        send?.setOnClickListener {
            if (input?.text != null && input?.text!!.isNotEmpty()) {
                send?.isActivated = false
                callback?.onSendClicked(input?.text!!.toString().trim()).apply { input?.setText("") }
            }
        }

        input?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                send?.performClick()
                true
            } else false
        }

        textWatcher = object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                send?.isActivated = p0 != null && p0.isNotEmpty()

                callback?.onTextChanged(p0.toString())
            }
        }

        input?.addTextChangedListener(textWatcher)
    }

    fun dispose() {
        textWatcher?.let {
            input?.removeTextChangedListener(it)
            textWatcher = null
        }
        callback = null
    }

    override fun onDetachedFromWindow() {
        dispose()
        super.onDetachedFromWindow()
    }
}

/**
 * @suppress
 */
interface BandyerChatInputLayoutEventListener {
    fun onTextChanged(text: String)
    fun onSendClicked(text: String)
}