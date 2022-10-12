package com.kaleyra.collaboration_suite_phone_ui.call.widgets.recording

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ViewExtensions.blink
import com.kaleyra.collaboration_suite_phone_ui.databinding.KaleyraRecordingWidgetBinding

/**
 * KaleyraRecordingWidget. It has a blinking red dot and a REC text.
 *
 * @constructor
 */
class KaleyraRecordingWidget @JvmOverloads constructor(
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

    /**
     * @suppress
     */
    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        binding.kaleyraIcon.also {
            if (visibility == VISIBLE) it.blink(BLINK_DURATION, -1) else it.clearAnimation()
        }
    }

    private companion object {
        const val BLINK_DURATION = 1000L // millis
    }
}