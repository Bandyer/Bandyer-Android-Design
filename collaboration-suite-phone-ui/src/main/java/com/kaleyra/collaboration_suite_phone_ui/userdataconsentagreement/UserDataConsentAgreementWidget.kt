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

package com.kaleyra.collaboration_suite_phone_ui.userdataconsentagreement

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.kaleyra.collaboration_suite_core_ui.R
import com.kaleyra.collaboration_suite_phone_ui.databinding.KaleyraUserDataConsentAgreementWidgetLayoutBinding

class UserDataConsentAgreementWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.kaleyra_rootLayoutStyle
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var acceptButtonText: String = ""

    private val binding: KaleyraUserDataConsentAgreementWidgetLayoutBinding by lazy { KaleyraUserDataConsentAgreementWidgetLayoutBinding.inflate(LayoutInflater.from(context), this, true) }

    fun setTitle(text: String) {
        binding.kaleyraTitle.text = text
    }

    fun setMessage(text: String) {
        binding.kaleyraMessage.text = text
    }

    fun showProgress() {
        binding.kaleyraDeclineButton.isActivated = false
        binding.kaleyraDeclineButton.isClickable = false
        binding.kaleyraDeclineButton.visibility = View.GONE

        binding.kaleyraAcceptButton.isActivated = false
        binding.kaleyraAcceptButton.isClickable = false
        binding.kaleyraAcceptButton.text = " ".repeat(acceptButtonText.length)

        binding.kaleyraProgressBar.visibility = View.VISIBLE
    }

    fun hideProgress() {
        binding.kaleyraDeclineButton.isActivated = true
        binding.kaleyraDeclineButton.isClickable = true
        binding.kaleyraDeclineButton.visibility = View.VISIBLE

        binding.kaleyraAcceptButton.isActivated = true
        binding.kaleyraAcceptButton.isClickable = true
        binding.kaleyraAcceptButton.text = acceptButtonText

        binding.kaleyraProgressBar.visibility = View.GONE
    }

    fun setAcceptButtonText(text: String) {
        acceptButtonText = text
        binding.kaleyraAcceptButton.text = text
    }

    fun setDeclineButtonText(text: String) {
        binding.kaleyraDeclineButton.text = text
    }

    fun setAcceptButtonListener(block: () -> Unit) {
        binding.kaleyraAcceptButton.setOnClickListener {
            block.invoke()
        }
    }

    fun setDeclineButtonListener(block: () -> Unit) {
        binding.kaleyraDeclineButton.setOnClickListener {
            block.invoke()
        }
    }
}