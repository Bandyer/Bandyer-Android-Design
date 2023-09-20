package com.kaleyra.collaboration_suite_phone_ui.chat.conversation.view.item

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
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.formatter.SymbolAnnotationType
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.formatter.messageFormatter
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.ConversationElement
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.view.MessageStateTag
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ParticipantDetails
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.view.Avatar
import com.kaleyra.collaboration_suite_phone_ui.extensions.ModifierExtensions.highlightOnFocus

private val OtherBubbleShape = RoundedCornerShape(0.dp, 24.dp, 24.dp, 12.dp)
private val MyBubbleShape = RoundedCornerShape(24.dp, 12.dp, 0.dp, 24.dp)

@Composable
internal fun OtherMessageItem(
    message: ConversationElement.Message,
    participantDetails: ParticipantDetails?,
    modifier: Modifier = Modifier
) {
    MessageItem(horizontalArrangement = Arrangement.Start, modifier = modifier) {
        Row {
            val username = participantDetails?.first ?: ""
            val uri = participantDetails?.second
            if (uri != null) {
                Avatar(
                    uri = uri,
                    contentDescription = stringResource(id = R.string.kaleyra_avatar),
                    placeholder = R.drawable.ic_kaleyra_avatar,
                    error = R.drawable.ic_kaleyra_avatar,
                    contentColor = MaterialTheme.colors.onPrimary,
                    backgroundColor = colorResource(R.color.kaleyra_color_grey_light),
                    size = 32.dp
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Bubble(
                messageText = message.data.text,
                messageTime = message.data.time,
                username = username,
                messageState = null,
                shape = OtherBubbleShape,
                backgroundColor = MaterialTheme.colors.primaryVariant
            )
        }
    }
}

@Composable
internal fun MyMessageItem(
    message: ConversationElement.Message,
    messageState: Message.State,
    modifier: Modifier = Modifier
) {
    MessageItem(horizontalArrangement = Arrangement.End, modifier = modifier) {
        Bubble(
            messageText = message.data.text,
            messageTime = message.data.time,
            username = null,
            messageState = messageState,
            shape = MyBubbleShape,
            backgroundColor = MaterialTheme.colors.secondary
        )
    }
}

@Composable
internal fun MessageItem(
    horizontalArrangement: Arrangement.Horizontal,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .focusable(true, interactionSource)
            .highlightOnFocus(interactionSource)
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
        modifier = Modifier.widthIn(min = 0.dp, max = configuration.screenWidthDp.div(2).dp)
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

            ClickableMessageText(messageText = messageText, textColor = contentColorFor(backgroundColor))

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