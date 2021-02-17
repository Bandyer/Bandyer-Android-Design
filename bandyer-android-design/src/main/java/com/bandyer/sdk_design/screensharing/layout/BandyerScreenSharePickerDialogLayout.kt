package com.bandyer.sdk_design.screensharing.layout

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.bandyer.sdk_design.databinding.BandyerScreenSharePickerDialogLayoutBinding
import com.google.android.material.button.MaterialButton

/**
 * @suppress
 * @author kristiyan
 */
class BandyerScreenSharePickerDialogLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {

    var appButton: MaterialButton? = null
        private set

    var deviceButton: MaterialButton? = null
        private set

    private val binding: BandyerScreenSharePickerDialogLayoutBinding by lazy { BandyerScreenSharePickerDialogLayoutBinding.inflate(LayoutInflater.from(context), this) }

    init {
        appButton = binding.bandyerAppScreenShare
        deviceButton = binding.bandyerGlobalScreenShare
    }
}