package com.kaleyra.collaboration_suite_phone_ui.call.stream.model

import android.net.Uri
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri

val streamUiMock = StreamUi("streamId", "username", VideoUi("1",null, false, false, ImmutableList(emptyList())), ImmutableUri(Uri.EMPTY))