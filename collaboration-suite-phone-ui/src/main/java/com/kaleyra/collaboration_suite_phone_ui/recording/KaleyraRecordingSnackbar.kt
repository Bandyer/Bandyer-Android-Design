package com.kaleyra.collaboration_suite_phone_ui.recording

import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.snackbar.KaleyraSnackbar
import com.kaleyra.collaboration_suite_utils.ContextRetainer

class KaleyraRecordingSnackbar private constructor(private val kaleyraSnackbar: KaleyraSnackbar) {

    enum class Type {
        TYPE_STARTED,
        TYPE_ENDED,
        TYPE_ERROR
    }

    companion object {
        @JvmStatic
        fun make(view: View, type: Type, @KaleyraSnackbar.Duration duration: Int): KaleyraRecordingSnackbar {
            val snackbar = when (type) {
                Type.TYPE_STARTED -> KaleyraSnackbar.make(
                    view,
                    R.string.kaleyra_recording_started,
                    R.string.kaleyra_recording_started_message,
                    duration
                )
                Type.TYPE_ENDED -> KaleyraSnackbar.make(
                    view,
                    R.string.kaleyra_recording_ended,
                    R.string.kaleyra_recording_ended_message,
                    duration
                )
                else -> KaleyraSnackbar
                    .make(
                        view,
                        R.string.kaleyra_recording_ended,
                        R.string.kaleyra_recording_ended_message,
                        duration
                    )
                    .setIcon(R.drawable.ic_kaleyra_snackbar_error)
                    .setIconColor(Color.WHITE)
                    .setTextColor(Color.WHITE)
                    .setBackgroundColor(
                        ContextCompat.getColor(
                            ContextRetainer.context,
                            R.color.kaleyra_color_error_40_day
                        )
                    )
            }
            return KaleyraRecordingSnackbar(snackbar)
        }
    }

    fun show() = kaleyraSnackbar.show()
}