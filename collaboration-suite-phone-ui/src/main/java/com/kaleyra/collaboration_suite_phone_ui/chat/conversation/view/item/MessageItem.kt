package com.kaleyra.collaboration_suite_phone_ui.chat.conversation.view.item

import android.net.Uri
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.collaboration_suite_core_ui.model.UserDetails
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.formatter.SymbolAnnotationType
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.formatter.messageFormatter
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.ConversationElement
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.view.MessageStateTag
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.view.Avatar
import com.kaleyra.collaboration_suite_phone_ui.extensions.ModifierExtensions.highlightOnFocus

private val OtherBubbleShape = RoundedCornerShape(0.dp, 24.dp, 24.dp, 12.dp)
private val MyBubbleShape = RoundedCornerShape(24.dp, 12.dp, 0.dp, 24.dp)

@Composable
internal fun MessageItem(
    showUserDetails: Boolean,
    message: ConversationElement.Message,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val horizontalArrangement =
        if (message.data is Message.MyMessage) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier
            .focusable(true, interactionSource)
            .highlightOnFocus(interactionSource)
            .then(modifier),
        horizontalArrangement = horizontalArrangement,
        content = {
            Row {
                if (showUserDetails && message.data is Message.OtherMessage) {
                    Avatar(
                        uri = message.data.avatar,
                        contentDescription = stringResource(id = R.string.kaleyra_avatar),
                        placeholder = R.drawable.ic_kaleyra_avatar,
                        error = R.drawable.ic_kaleyra_avatar,
                        contentColor = MaterialTheme.colors.onPrimary,
                        backgroundColor = colorResource(R.color.kaleyra_color_grey_light),
                        size = 32.dp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Bubble(showUserDetails, message)
            }
        }
    )
}

@Composable
internal fun Bubble(showUsername: Boolean, message: ConversationElement.Message) {
    val configuration = LocalConfiguration.current

    Card(
        shape = if (message.data is Message.MyMessage) MyBubbleShape else OtherBubbleShape,
        backgroundColor = if (message.data is Message.MyMessage) MaterialTheme.colors.secondary else MaterialTheme.colors.primaryVariant,
        elevation = 0.dp,
        modifier = Modifier.widthIn(min = 0.dp, max = configuration.screenWidthDp.div(2).dp)
    ) {
        Column(modifier = Modifier.padding(16.dp, 8.dp)) {
            if (showUsername && message.data is Message.OtherMessage) {
                Text(
                    text = message.data.username,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.body2
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            ClickableMessageText(item = message)

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = message.data.time,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.body2
                )

                if (message.data is Message.MyMessage) {
                    val messageState by message.data.state.collectAsStateWithLifecycle(
                        Message.State.Sending)

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
internal fun ClickableMessageText(item: ConversationElement.Message) {
    val uriHandler = LocalUriHandler.current

    val styledMessage = messageFormatter(
        text = item.data.text,
        primary = item.data !is Message.MyMessage
    )

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