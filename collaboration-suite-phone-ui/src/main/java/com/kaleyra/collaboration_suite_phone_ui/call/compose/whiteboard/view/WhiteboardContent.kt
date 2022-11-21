package com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.view

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model.WhiteboardUpload
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme

const val LinearProgressIndicatorTag = "LinearProgressIndicatorTag"

@Composable
internal fun WhiteboardContent(
    loading: Boolean,
    upload: WhiteboardUpload?
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.kaleyra_color_loading_whiteboard_background))
    ) {
        // TODO place web view
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
    WhiteboardContentPreview(upload = WhiteboardUpload.Uploading(.7f))
}

@Preview
@Composable
internal fun WhiteboardContentLoadingPreview() {
    WhiteboardContentPreview(loading = true)
}

@Composable
private fun WhiteboardContentPreview(
    loading: Boolean = false,
    upload: WhiteboardUpload? = null
) {
    KaleyraTheme {
        Surface {
            WhiteboardContent(
                loading = loading,
                upload = upload
            )
        }
    }
}