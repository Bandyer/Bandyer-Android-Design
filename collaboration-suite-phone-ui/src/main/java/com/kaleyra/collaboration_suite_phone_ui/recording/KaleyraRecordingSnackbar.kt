package com.kaleyra.collaboration_suite_phone_ui.recording

import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.snackbar.KaleyraSnackbar
import com.kaleyra.collaboration_suite_utils.ContextRetainer

/**
 * KaleyraRecordingSnackbar
 *
 * @constructor
 */
class KaleyraRecordingSnackbar private constructor(private val kaleyraSnackbar: KaleyraSnackbar) {

    /**
     * The snackbar view
     */
    val view = kaleyraSnackbar.view

    /**
     * KaleyraRecordingSnackbar's types
     */
    enum class Type {
        /**
         * t y p e_s t a r t e d
         */
        TYPE_STARTED,

        /**
         * t y p e_e n d e d
         */
        TYPE_ENDED,

        /**
         * t y p e_e r r o r
         */
        TYPE_ERROR
    }

    /**
     * @suppress
     */
    companion object {
        /**
         * Make a KaleyraRecordingSnackbar
         *
         * <p>Snackbar will try and find a parent view to hold Snackbar's view from the value given to
         * {@code view}. Snackbar will walk up the view tree trying to find a suitable parent, which is
         * defined as a {@link CoordinatorLayout} or the window decor's content view, whichever comes
         * first.
         *
         * <p>Having a {@link CoordinatorLayout} in your view hierarchy allows Snackbar to enable certain
         * features, such as swipe-to-dismiss and automatically moving of widgets.
         *
         * @param view The view to find a parent from
         * @param type The type of the snackbar
         * @param duration How long to display the message. Can be {@link #LENGTH_SHORT}, {@link
         *     #LENGTH_LONG}, {@link #LENGTH_INDEFINITE}, or a custom duration in milliseconds.
         * @return KaleyraRecordingSnackbar
         */
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
                    R.string.kaleyra_recording_stopped,
                    R.string.kaleyra_recording_stopped_message,
                    duration
                )
                else -> KaleyraSnackbar
                    .make(
                        view,
                        R.string.kaleyra_recording_failed,
                        R.string.kaleyra_recording_failed_message,
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

    /**
     * Show the snackbar
     */
    fun show() = kaleyraSnackbar.show()
}