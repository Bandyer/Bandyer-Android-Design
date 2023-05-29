package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.net.Uri
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

val streamUiMock = StreamUi("streamId", "username", VideoUi("1",null, false, false, ImmutableList(emptyList())), ImmutableUri(Uri.EMPTY))