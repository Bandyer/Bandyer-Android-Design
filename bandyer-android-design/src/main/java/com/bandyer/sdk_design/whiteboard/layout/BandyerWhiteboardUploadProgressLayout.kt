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
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerProgressViewLayoutBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textview.MaterialTextView

/**
 *
 * @property style BandyerWhiteboardUploadProgressLayoutState
 * @property progressTitle BandyerTextView?
 * @property progressSubtitle BandyerTextView?
 * @property progressText BandyerTextView?
 * @property progressBar ProgressBar?
 * @property errorImage ImageView?
 * @constructor
 */
class BandyerWhiteboardUploadProgressLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.bandyer_rootLayoutStyle) : MaterialCardView(context, attrs, defStyleAttr) {

    /**
     * Its value change the visibility of the progress bar, the progress percentage and the error image
     */
    var errorOccurred: Boolean = false
        set(value) {
            field = value
            if(value) {
                errorImage?.visibility = View.VISIBLE
                progressText?.visibility = View.INVISIBLE
                progressBar?.visibility = View.INVISIBLE
            } else {
                errorImage?.visibility = View.INVISIBLE
                progressText?.visibility = View.VISIBLE
                progressBar?.visibility = View.VISIBLE
            }
        }

    var progressTitle: MaterialTextView? = null
        private set

    var progressSubtitle: MaterialTextView? = null
        private set

    var progressText: MaterialTextView? = null
        private set

    var progressBar: CircularProgressIndicator? = null
        private set

    var errorImage: MaterialButton? = null
        private set

    private val binding: BandyerProgressViewLayoutBinding by lazy { BandyerProgressViewLayoutBinding.inflate(LayoutInflater.from(context), this) }

    init {
        progressTitle = binding.bandyerWhiteboardProgressTitle
        progressSubtitle = binding.bandyerWhiteboardProgressSubtitle
        progressText = binding.bandyerWhiteboardProgressProgressText
        progressBar = binding.bandyerWhiteboardProgressProgressBar
        errorImage = binding.bandyerWhiteboardProgressErrorImage
    }

}
