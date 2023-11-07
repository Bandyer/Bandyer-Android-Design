package com.kaleyra.video_sdk.call.whiteboard.view

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardUploadUi
import com.kaleyra.video_sdk.theme.KaleyraTheme
import kotlin.math.roundToInt

@Composable
internal fun WhiteboardUploadCard(upload: WhiteboardUploadUi) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        val error = upload is WhiteboardUploadUi.Error
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (error) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_kaleyra_close),
                        contentDescription = null,
                        tint = MaterialTheme.colors.error,
                        modifier = Modifier.size(64.dp)
                    )
                } else {
                    val progressValue = (upload as? WhiteboardUploadUi.Uploading)?.progress ?: 0f
                    val progress by animateFloatAsState(targetValue = progressValue)
                    CircularProgressIndicator(
                        progress = progress,
                        color = MaterialTheme.colors.secondaryVariant,
                        size = 56.dp,
                        strokeWidth = ProgressIndicatorDefaults.StrokeWidth
                    )
                    Text(
                        text = stringResource(id = R.string.kaleyra_file_upload_percentage, (progressValue * 100).roundToInt()), fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(id = if (error) R.string.kaleyra_whiteboard_error_title else R.string.kaleyra_whiteboard_uploading_file),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(id = if (error) R.string.kaleyra_whiteboard_error_subtitle else R.string.kaleyra_whiteboard_compressing),
                    color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun UploadCardUploadingPreview() {
    UploadCardPreview(WhiteboardUploadUi.Uploading(.7f))
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun UploadCardErrorPreview() {
    UploadCardPreview(WhiteboardUploadUi.Error)
}

@Composable
private fun UploadCardPreview(upload: WhiteboardUploadUi) {
    KaleyraTheme {
        WhiteboardUploadCard(upload = upload)
    }
}