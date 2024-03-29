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

package com.kaleyra.collaboration_suite_phone_ui.call.layout

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout

import com.kaleyra.collaboration_suite_phone_ui.databinding.KaleyraSnapshotDialogLayoutBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

/**
 * @suppress
 */
class KaleyraSnapshotDialogLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {

    var closeButton: MaterialButton? = null
        private set

    var saveButton: MaterialButton? = null
        private set

    var collaborateButton: ExtendedFloatingActionButton? = null
        private set

    val binding: KaleyraSnapshotDialogLayoutBinding by lazy { KaleyraSnapshotDialogLayoutBinding.inflate(LayoutInflater.from(context), this) }

    init {
        closeButton = binding.kaleyraSnapshotShareCloseButton
        saveButton = binding.kaleyraSnapshotShareSaveButton
        collaborateButton = binding.kaleyraSnapshotShareWhiteboardButton
    }
}