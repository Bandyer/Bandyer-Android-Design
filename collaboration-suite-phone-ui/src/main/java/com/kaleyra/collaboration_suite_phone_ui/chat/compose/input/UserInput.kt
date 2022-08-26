package com.kaleyra.collaboration_suite_phone_ui.chat.compose.input

import android.view.ContextThemeWrapper
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatInputLayoutEventListener
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatInputLayoutWidget
import com.kaleyra.collaboration_suite_phone_ui.extensions.getAttributeResourceId

@Composable
internal fun UserInput(onSendMessage: (String) -> Unit, onTyping: () -> Unit) {
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = {
            val themeResId = it.theme.getAttributeResourceId(R.attr.kaleyra_chatInputWidgetStyle)
            KaleyraChatInputLayoutWidget(ContextThemeWrapper(it, themeResId))
        },
        update = {
            it.callback = object : KaleyraChatInputLayoutEventListener {
                override fun onTextChanged(text: String) = onTyping()
                override fun onSendClicked(text: String) = onSendMessage(text)
            }
        }
    )
}