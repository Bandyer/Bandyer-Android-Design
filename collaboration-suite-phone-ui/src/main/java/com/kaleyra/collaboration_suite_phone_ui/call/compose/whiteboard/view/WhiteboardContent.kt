package com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.view

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kaleyra.collaboration_suite.phonebox.WhiteboardView
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model.WhiteboardUploadUi
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme

const val LinearProgressIndicatorTag = "LinearProgressIndicatorTag"
const val WhiteboardViewTag = "WhiteboardViewTag"

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun WhiteboardContent(
    loading: Boolean,
    upload: WhiteboardUploadUi?,
    onWhiteboardViewCreated: (WhiteboardView) -> Unit,
    onWhiteboardViewDispose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        var whiteboardView by remember { mutableStateOf<WhiteboardView?>(null) }

        val runningInPreview = LocalInspectionMode.current
        if (!runningInPreview) {
            AndroidView(
                factory = { context ->
                    WhiteboardView(context).apply {
                        onWhiteboardViewCreated(this)

                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }.also { whiteboardView = it }
                },
                modifier = Modifier.testTag(WhiteboardViewTag)
            )
        }

        val currentOnDispose by rememberUpdatedState(onWhiteboardViewDispose)

        whiteboardView?.let { it ->
            DisposableEffect(it) {
                onDispose { currentOnDispose() }
            }
        }

        if (loading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .testTag(LinearProgressIndicatorTag),
                color = MaterialTheme.colors.secondary
            )
        }
        if (upload != null) {
            WhiteboardUploadCard(upload = upload)
        }
    }
}


@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun WhiteboardContentUploadingPreview() {
    WhiteboardContentPreview(upload = WhiteboardUploadUi.Uploading(.7f))
}

@Preview
@Composable
internal fun WhiteboardContentLoadingPreview() {
    WhiteboardContentPreview(loading = true)
}

@Composable
private fun WhiteboardContentPreview(
    loading: Boolean = false,
    upload: WhiteboardUploadUi? = null
) {
    KaleyraTheme {
        Surface {
            WhiteboardContent(
                loading = loading,
                upload = upload,
                onWhiteboardViewCreated = {},
                onWhiteboardViewDispose = {}
            )
        }
    }
}