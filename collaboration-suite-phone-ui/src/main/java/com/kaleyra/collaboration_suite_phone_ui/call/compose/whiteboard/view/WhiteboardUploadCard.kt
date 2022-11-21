package com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model.WhiteboardUpload
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import kotlin.math.roundToInt

@Composable
internal fun WhiteboardUploadCard(upload: WhiteboardUpload) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        val error = upload is WhiteboardUpload.Error
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
                    val progress = (upload as? WhiteboardUpload.Uploading)?.progress ?: 0f
                    CircularProgressIndicator(
                        progress = progress,
                        color = MaterialTheme.colors.secondaryVariant,
                        size = 56.dp,
                        strokeWidth = ProgressIndicatorDefaults.StrokeWidth
                    )
                    Text(
                        text = stringResource(id = R.string.kaleyra_file_upload_percentage, (progress * 100).roundToInt()), fontSize = 12.sp
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
    UploadCardPreview(WhiteboardUpload.Uploading(.7f))
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun UploadCardErrorPreview() {
    UploadCardPreview(WhiteboardUpload.Error)
}

@Composable
private fun UploadCardPreview(upload: WhiteboardUpload) {
    KaleyraTheme {
        WhiteboardUploadCard(upload = upload)
    }
}