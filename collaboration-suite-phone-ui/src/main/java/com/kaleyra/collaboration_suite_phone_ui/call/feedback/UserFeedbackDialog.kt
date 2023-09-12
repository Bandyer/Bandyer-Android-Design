package com.kaleyra.collaboration_suite_phone_ui.call.feedback

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kaleyra.collaboration_suite_phone_ui.call.feedback.view.FeedbackForm
import com.kaleyra.collaboration_suite_phone_ui.call.feedback.view.FeedbackSent
import com.kaleyra.collaboration_suite_phone_ui.theme.KaleyraTheme
import kotlinx.coroutines.delay

private const val AutoDismissMs = 3000L

@Composable
internal fun UserFeedbackDialog(onUserFeedback: (Float, String) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        var isFeedbackSent by remember { mutableStateOf(false) }

        if (isFeedbackSent) {
            LaunchedEffect(Unit) {
                delay(AutoDismissMs)
                onDismiss()
            }
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .wrapContentSize()
                .animateContentSize()
        ) {
            if (!isFeedbackSent) {
                FeedbackForm(
                    onUserFeedback = { value: Float, text: String ->
                        onUserFeedback(value, text)
                        isFeedbackSent = true
                    },
                    onDismiss = onDismiss
                )
            } else FeedbackSent(onDismiss)
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun UserFeedbackDialogPreview() = KaleyraTheme {
    UserFeedbackDialog(onUserFeedback = { _, _ -> }, onDismiss = {})
}