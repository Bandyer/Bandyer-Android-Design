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

package com.bandyer.sdk_design.call.layout

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerCallWatermarkBinding
import com.bandyer.sdk_design.utils.bandyerSDKDesignPrefs
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.squareup.picasso.Picasso
import java.io.File


/**
 * Bandyer watermark layout
 * @author kristiyan
 * @constructor
 */
class BandyerCallWatermarkLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.bandyer_rootLayoutStyle) : LinearLayout(context, attrs, defStyleAttr) {

    /**
     * Image of the watermark
     */
    var watermarkImage: ShapeableImageView? = null
        private set

    /**
     * Title of the watermark
     */
    var watermarkText: MaterialTextView? = null
        private set

    private val binding: BandyerCallWatermarkBinding by lazy { BandyerCallWatermarkBinding.inflate(LayoutInflater.from(context), this) }

    init {
        watermarkImage = binding.bandyerLogoView
        watermarkText = binding.bandyerLabelView

        val watermarkLogo = bandyerSDKDesignPrefs().getString("call_watermark_image_uri", null)
        val watermarkTitle = bandyerSDKDesignPrefs().getString("call_watermark_text", null)

        post { setUp(watermarkLogo, watermarkTitle) }
    }


    private fun setUp(watermarkLogo: String?, watermarkTitle: String?) {
        watermarkTitle?.let {
            binding.bandyerLogoView.visibility = if (it.isBlank()) View.GONE else View.VISIBLE
            binding.bandyerLabelView.text = it
            binding.bandyerLabelView.requestLayout()
        }

        watermarkLogo?.let {
            val uri = if (watermarkLogo.startsWith("content") || watermarkLogo.startsWith("android.resource") || watermarkLogo.startsWith("file") || watermarkLogo.startsWith("http")) Uri.parse(watermarkLogo) else Uri.fromFile(File(watermarkLogo))
            Picasso.get().load(uri).into(binding.bandyerLogoView)
            binding.bandyerLogoView.visibility = if (it.isBlank()) View.GONE else View.VISIBLE
            binding.bandyerLogoView.requestLayout()

        }
    }
}
