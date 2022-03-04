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

package com.bandyer.video_android_phone_ui.whiteboard.layout

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.bandyer.video_android_phone_ui.R
import com.bandyer.video_android_phone_ui.databinding.BandyerProgressViewLayoutBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textview.MaterialTextView

/**
 *
 * @property state BandyerWhiteboardUploadProgressLayout.State?
 * @property progressTitle MaterialTextView?
 * @property progressSubtitle MaterialTextView?
 * @property progressText MaterialTextView?
 * @property progressBar CircularProgressIndicator?
 * @property errorImage MaterialButton?
 * @constructor
 */
class BandyerWhiteboardUploadProgressLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.bandyer_rootLayoutStyle) : MaterialCardView(context, attrs, defStyleAttr) {

    /**
     * The state of the upload progress card view
     */
    var state: State? = null
        set(value) {
            field = value
            updateView(field)
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
        state = State.COMPLETED
    }

    /**
     * Update the UI based on the state value
     * @param state the state value
     */
    private fun updateView(state: State?) {
        if(state == null) return
        when (state) {
            State.UPLOADING -> {
                visibility = View.VISIBLE
                errorImage?.visibility = View.INVISIBLE
                progressText?.visibility = View.VISIBLE
                progressBar?.visibility = View.VISIBLE
            }
            State.ERROR -> {
                visibility = View.VISIBLE
                errorImage?.visibility = View.VISIBLE
                progressText?.visibility = View.INVISIBLE
                progressBar?.visibility = View.INVISIBLE
            }
            State.COMPLETED -> visibility = View.GONE
        }
    }

    /**
     * Update the progress bar and the percentage text
     * @param percentage the new percentage value
     */
    fun updateUploadingProgress(percentage: Int) {
        progressBar?.setProgressCompat(percentage, true)
        progressText?.text = resources.getString(R.string.bandyer_file_upload_percentage, percentage)
    }

    /**
     * Show the card in the uploading state
     * @param title the title to be set
     * @param subtitle the subtitle to be set
     * @param percentage the percentage to be set
     */
    fun showUploading(title: String, subtitle: String, percentage: Float) {
        state = State.UPLOADING
        updateTitleText(title, subtitle)
        updateUploadingProgress(percentage.toInt())
    }

    /**
     * Show the card in the error state and automatically hide it after 3 seconds
     * @param title the title to be set
     * @param subtitle the subtitle to be set
     */
    fun showError(title: String, subtitle: String) {
        state = State.ERROR
        updateTitleText(title, subtitle)
        postDelayed({
            state = State.COMPLETED
        }, 3000)
    }

    /**
     * Hide the view
     */
    fun hide() {
        state = State.COMPLETED
    }

    /**
     * Update the title and subtitle text
     * @param title the updated title text
     * @param subtitle the updated subtitle text
     */
    private fun updateTitleText(title: String, subtitle: String) {
        progressTitle?.text = title
        progressSubtitle?.text = subtitle
    }

    /**
     * States of a BandyerWhiteboardUploadProgressLayout
     * @constructor
     */
    enum class State {
        /**
         * U p l o a d i n g
         *
         * @constructor Create empty U p l o a d i n g
         */
        UPLOADING,

        /**
         * E r r o r
         *
         * @constructor Create empty E r r o r
         */
        ERROR,

        /**
         * C o m p l e t e d
         *
         * @constructor Create empty C o m p l e t e d
         */
        COMPLETED,
    }

}
