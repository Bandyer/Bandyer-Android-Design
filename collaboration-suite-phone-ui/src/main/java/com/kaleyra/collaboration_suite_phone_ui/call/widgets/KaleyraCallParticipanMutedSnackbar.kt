package com.kaleyra.collaboration_suite_phone_ui.call.widgets

import android.view.View
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.snackbar.KaleyraSnackbar

/**
 * Kaleyra call participant muted snackbar
 */
object KaleyraCallParticipantMutedSnackbar {

    /**
     * Make a KaleyraCallParticipanMutedSnackbar
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
     * @param adminDisplayName The display name of the admin that has muted the call participant
     * @param duration How long to display the message. Can be {@link #LENGTH_SHORT}, {@link
     *     #LENGTH_LONG}, {@link #LENGTH_INDEFINITE}, or a custom duration in milliseconds.
     * @return KaleyraSnackbar
     */
    @JvmStatic
    fun make(view: View, adminDisplayName: String? = null, @KaleyraSnackbar.Duration duration: Int): KaleyraSnackbar {
        return KaleyraSnackbar.make(
            view,
            duration).apply {
            val title = view.context.resources.getQuantityString(
                R.plurals.kaleyra_call_participant_muted_by_admin,
                if (adminDisplayName.isNullOrBlank()) 0 else 1,
                adminDisplayName)
            setTitle(title)
        }
    }
}