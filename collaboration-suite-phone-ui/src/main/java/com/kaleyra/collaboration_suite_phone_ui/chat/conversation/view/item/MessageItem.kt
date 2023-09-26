package com.kaleyra.collaboration_suite_phone_ui.chat.conversation.view.item

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatParticipantDetails
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.formatter.SymbolAnnotationType
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.formatter.messageFormatter
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.view.MessageStateTag
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.view.Avatar
import com.kaleyra.collaboration_suite_phone_ui.extensions.ModifierExtensions.highlightOnFocus
import com.kaleyra.collaboration_suite_phone_ui.theme.KaleyraTheme
import kotlinx.coroutines.flow.MutableStateFlow

val MessageItemAvatarSize = 28.dp
val OtherBubbleAvatarSpacing = 8.dp
val OtherBubbleLeftSpacing = 36.dp
const val BubbleTestTag = "BubbleTestTag"

@Composable
internal fun OtherMessageItem(
    message: Message.OtherMessage,
    isFirstChainMessage: Boolean = true,
    isLastChainMessage: Boolean = true,
    participantDetails: ChatParticipantDetails? = null,
    modifier: Modifier = Modifier
) {
    MessageRow(
        isLastChainMessage = isLastChainMessage,
        horizontalArrangement = Arrangement.Start,
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            val uri = participantDetails?.image
            when {
                isLastChainMessage && uri != null -> {
                    Avatar(
                        uri = uri,
                        contentDescription = stringResource(id = R.string.kaleyra_avatar),
                        placeholder = R.drawable.ic_kaleyra_avatar,
                        error = R.drawable.ic_kaleyra_avatar,
                        contentColor = MaterialTheme.colors.onPrimary,
                        backgroundColor = colorResource(R.color.kaleyra_color_grey_light),
                        size = MessageItemAvatarSize
                    )
                    Spacer(modifier = Modifier.width(OtherBubbleAvatarSpacing))
                }

                uri != null -> Spacer(modifier = Modifier.width(OtherBubbleLeftSpacing))
            }
            Bubble(
                messageText = message.content,
                messageTime = message.time,
                username = if (isFirstChainMessage) participantDetails?.username else null,
                messageState = null,
                shape = RoundedCornerShape(
                    topStart = if (isFirstChainMessage) 12.dp else 6.dp,
                    topEnd = 24.dp,
                    bottomStart = if (isLastChainMessage) 0.dp else 6.dp,
                    bottomEnd = 24.dp
                ),
                backgroundColor = MaterialTheme.colors.primaryVariant
            )
        }
    }
}

@Composable
internal fun MyMessageItem(
    message: Message.MyMessage,
    isFirstChainMessage: Boolean = true,
    isLastChainMessage: Boolean = true,
    modifier: Modifier = Modifier
) {
    val messageState by message.state.collectAsStateWithLifecycle(initialValue = Message.State.Read)
    MessageRow(
        isLastChainMessage = isLastChainMessage,
        horizontalArrangement = Arrangement.End,
        modifier = modifier
    ) {
        Bubble(
            messageText = message.content,
            messageTime = message.time,
            username = null,
            messageState = messageState,
            shape = RoundedCornerShape(
                topStart = 24.dp,
                topEnd = if (isFirstChainMessage) 12.dp else 6.dp,
                bottomStart = 24.dp,
                bottomEnd = if (isLastChainMessage) 0.dp else 6.dp
            ),
            backgroundColor = MaterialTheme.colors.secondary
        )
    }
}

@Composable
internal fun MessageRow(
    isLastChainMessage: Boolean,
    horizontalArrangement: Arrangement.Horizontal,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .focusable(true, interactionSource)
            .highlightOnFocus(interactionSource)
            .padding(bottom = if (isLastChainMessage) 6.dp else 0.dp)
            .then(modifier),
        horizontalArrangement = horizontalArrangement,
        content = content
    )
}

@Composable
internal fun Bubble(
    messageText: String,
    messageTime: String,
    username: String?,
    messageState: Message.State?,
    shape: Shape,
    backgroundColor: Color
) {
    val configuration = LocalConfiguration.current

    Card(
        shape = shape,
        backgroundColor = backgroundColor,
        elevation = 0.dp,
        modifier = Modifier
            .widthIn(min = 0.dp, max = configuration.screenWidthDp.div(2).dp)
            .testTag(BubbleTestTag)
    ) {
        Column(modifier = Modifier.padding(16.dp, 8.dp)) {
            if (username != null) {
                Text(
                    text = username,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.body2
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            ClickableMessageText(
                messageText = messageText,
                textColor = contentColorFor(backgroundColor)
            )

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = messageTime,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.body2
                )

                if (messageState != null) {
                    Icon(
                        painter = painterFor(messageState),
                        contentDescription = contentDescriptionFor(messageState),
                        modifier = Modifier
                            .padding(2.dp)
                            .size(16.dp)
                            .testTag(MessageStateTag)
                    )
                }
            }
        }
    }
}

@Composable
internal fun ClickableMessageText(messageText: String, textColor: Color) {
    val uriHandler = LocalUriHandler.current

    val styledMessage = messageFormatter(text = messageText, textColor = textColor)

    ClickableText(
        text = styledMessage,
        style = MaterialTheme.typography.body2.copy(color = LocalContentColor.current),
        onClick = {
            styledMessage
                .getStringAnnotations(start = it, end = it)
                .firstOrNull()
                ?.let { annotation ->
                    when (annotation.tag) {
                        SymbolAnnotationType.LINK.name -> uriHandler.openUri(annotation.item)
                        else -> Unit
                    }
                }
        }
    )
}

@Composable
private fun painterFor(state: Message.State): Painter =
    painterResource(
        id = when (state) {
            is Message.State.Sending -> R.drawable.ic_kaleyra_clock
            is Message.State.Sent -> R.drawable.ic_kaleyra_single_tick
            else -> R.drawable.ic_kaleyra_double_tick
        }
    )

@Composable
private fun contentDescriptionFor(state: Message.State): String =
    stringResource(
        id = when (state) {
            is Message.State.Sending -> R.string.kaleyra_chat_msg_status_pending
            is Message.State.Sent -> R.string.kaleyra_chat_msg_status_sent
            else -> R.string.kaleyra_chat_msg_status_seen
        }
    )

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun OtherMessageItemPreview() = KaleyraTheme {
    Surface {
        OtherMessageItem(
            message = Message.OtherMessage(
                "userId8",
                "id8",
                "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                "15:01"
            )
        )
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun MyMessageItemPreview() = KaleyraTheme {
    Surface {
        MyMessageItem(
            message = Message.MyMessage(
                "id8",
                "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                "15:01",
                MutableStateFlow(Message.State.Read)
            )
        )
    }
}