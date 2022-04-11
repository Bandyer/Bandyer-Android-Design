package com.kaleyra.collaboration_suite_core_ui.chat

import com.kaleyra.collaboration_suite_core_ui.common.BoundServiceActivity

abstract class ChatActivity: BoundServiceActivity<ChatService>(ChatService::class.java)