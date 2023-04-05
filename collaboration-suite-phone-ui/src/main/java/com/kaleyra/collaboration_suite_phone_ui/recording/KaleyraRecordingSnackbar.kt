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

package com.kaleyra.collaboration_suite_phone_ui.recording

import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.snackbar.KaleyraSnackbar
import com.kaleyra.collaboration_suite_utils.ContextRetainer

/**
 * Kaleyra recording snackbar factory
 */
object KaleyraRecordingSnackbar {

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
    fun make(view: View, type: Type, @KaleyraSnackbar.Duration duration: Int): KaleyraSnackbar = when (type) {
        Type.TYPE_STARTED -> KaleyraSnackbar.make(
            view,
            R.string.kaleyra_recording_started,
            R.string.kaleyra_recording_started_message,
            duration
        )
        Type.TYPE_ENDED   -> KaleyraSnackbar.make(
            view,
            R.string.kaleyra_recording_stopped,
            R.string.kaleyra_recording_stopped_message,
            duration
        )
        else              -> KaleyraSnackbar.make(
            view,
            R.string.kaleyra_recording_failed,
            R.string.kaleyra_recording_failed_message,
            duration
        )
            .setIcon(R.drawable.ic_kaleyra_snackbar_error)
            .setIconColor(Color.WHITE)
            .setTextColor(Color.WHITE)
            .setBackgroundColor(ContextCompat.getColor(ContextRetainer.context, R.color.kaleyra_color_error_40_day))
    }
}