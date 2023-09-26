package com.kaleyra.collaboration_suite_phone_ui.chat.appbar.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.view.Avatar
import com.kaleyra.collaboration_suite_phone_ui.common.text.Ellipsize
import com.kaleyra.collaboration_suite_phone_ui.common.text.EllipsizeText

@Composable
internal fun ChatAppBarContent(
    image: ImmutableUri,
    title: String,
    subtitle: String,
    typingDots: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Avatar(
            uri = image,
            contentDescription = stringResource(id = R.string.kaleyra_chat_avatar_desc),
            placeholder = R.drawable.ic_kaleyra_avatar,
            error = R.drawable.ic_kaleyra_avatar,
            contentColor = MaterialTheme.colors.onPrimary,
            backgroundColor = colorResource(R.color.kaleyra_color_grey_light),
            size = 40.dp
        )
        Column(Modifier.padding(start = 12.dp)) {
            EllipsizeText(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                ellipsize = Ellipsize.Marquee
            )
            Row {
                EllipsizeText(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = LocalContentColor.current.copy(alpha = 0.5f),
                    modifier = Modifier.testTag(SubtitleTag),
                    ellipsize = Ellipsize.Marquee
                )
                if (typingDots) {
                    TypingDots(
                        color = LocalContentColor.current.copy(alpha = 0.5f),
                        modifier = Modifier
                            .align(Alignment.Bottom)
                            .padding(start = 4.dp, bottom = 4.dp)
                            .testTag(BouncingDotsTag)
                    )
                }
            }
        }
    }
}