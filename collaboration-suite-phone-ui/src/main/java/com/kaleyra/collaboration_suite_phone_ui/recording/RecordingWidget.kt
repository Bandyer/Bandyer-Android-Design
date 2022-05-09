package com.kaleyra.collaboration_suite_phone_ui.recording

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ViewExtensions.blink
import com.kaleyra.collaboration_suite_phone_ui.databinding.KaleyraRecordingWidgetBinding

class RecordingWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding: KaleyraRecordingWidgetBinding =
        KaleyraRecordingWidgetBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.kaleyraIcon.blink(BLINK_DURATION, -1)
    }

    private companion object {
        const val BLINK_DURATION = 1000L // millis
    }
}