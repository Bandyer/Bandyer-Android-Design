package com.kaleyra.collaboration_suite_phone_ui.call.compose.feedback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.kaleyra.collaboration_suite_phone_ui.call.compose.permission.findActivity
import com.kaleyra.collaboration_suite_phone_ui.call.dialogs.KaleyraParicipantRemovedDialog

@Composable
fun KickedMessage(adminName: String?= null, onDismiss: () -> Unit) {
    val activity = LocalContext.current.findActivity() as? FragmentActivity
    val fragmentManager = activity?.supportFragmentManager

    DisposableEffect(fragmentManager) {
        var dialog: KaleyraParicipantRemovedDialog? = null

        if (fragmentManager != null) {
            dialog = KaleyraParicipantRemovedDialog(adminName).apply {
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