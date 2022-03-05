/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_core_ui.layout

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.kaleyra.collaboration_suite_core_ui.databinding.KaleyraCallWatermarkBinding
import com.kaleyra.collaboration_suite_core_ui.utils.kaleyraCollaborationSuiteUIPrefs
import com.squareup.picasso.Picasso
import java.io.File

/**
 * Kaleyra watermark layout
 * @author kristiyan
 * @constructor
 */
class KaleyraCallWatermarkLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: KaleyraCallWatermarkBinding = KaleyraCallWatermarkBinding.inflate(LayoutInflater.from(context), this, true)

    private val watermarkUri = kaleyraCollaborationSuiteUIPrefs().getString("call_watermark_image_uri", null)
    private val watermarkText = kaleyraCollaborationSuiteUIPrefs().getString("call_watermark_text", binding.kaleyraLabelView.text.toString())

    init {
        setUp()
        updateVisibility()
    }

    private fun updateVisibility() = with(binding) {
        val displayImage = watermarkUri?.isBlank() == false || (kaleyraLogoView.drawable != null && watermarkUri == null)
        val displayText = !watermarkText.isNullOrBlank()
        kaleyraLogoView.visibility = if (displayImage) View.VISIBLE else View.GONE
        kaleyraLabelView.visibility = if (displayText) View.VISIBLE else View.GONE
        super.setVisibility(if (!displayImage && !displayText) View.GONE else View.VISIBLE)
    }

    private fun setUp() = with(binding) {
        kaleyraLabelView.text = watermarkText
        watermarkUri ?: return
        val uri = if (watermarkUri.startsWith("content") || watermarkUri.startsWith("android.resource") || watermarkUri.startsWith("file") || watermarkUri.startsWith("http")) Uri.parse(watermarkUri) else Uri.fromFile(File(watermarkUri))
        Picasso.get().load(uri).into(kaleyraLogoView)
    }

    /**
     * @suppress
     */
    override fun setVisibility(visibility: Int) = when {
        this.visibility == visibility -> Unit
        visibility == View.VISIBLE -> updateVisibility()
        else -> super.setVisibility(visibility)
    }
}
