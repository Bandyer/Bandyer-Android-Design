package com.kaleyra.video_sdk.call.stream.model

import android.net.Uri
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri

val streamUiMock = StreamUi("streamId", "username", VideoUi("1",null, false, false, ImmutableList(emptyList())), ImmutableUri(Uri.EMPTY))