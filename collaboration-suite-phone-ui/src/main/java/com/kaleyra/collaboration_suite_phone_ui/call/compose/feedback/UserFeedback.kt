package com.kaleyra.collaboration_suite_phone_ui.call.compose.feedback

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.fragment.app.FragmentActivity
import com.kaleyra.collaboration_suite_phone_ui.call.compose.permission.findActivity
import com.kaleyra.collaboration_suite_phone_ui.feedback.FeedbackDialog

@Composable
fun UserFeedback(onUserFeedback: (Float, String) -> Unit, onDismiss: () -> Unit) {
    val activity = LocalContext.current.findActivity() as? FragmentActivity
    val fragmentManager = activity?.supportFragmentManager
    val themeColors = MaterialTheme.colors
    val fontFamily = MaterialTheme.typography.body1.fontFamily ?: FontFamily.Default

    DisposableEffect(fragmentManager) {
        var dialog: FeedbackDialog? = null

        if (fragmentManager != null) {
            dialog = FeedbackDialog(themeColors, fontFamily).apply {
                onFeedback(onUserFeedback)
                onDismiss(onDismiss)
                show(fragmentManager, javaClass.name)
            }
        }

        onDispose {
            dialog?.dismiss()
            dialog = null
        }
    }
}