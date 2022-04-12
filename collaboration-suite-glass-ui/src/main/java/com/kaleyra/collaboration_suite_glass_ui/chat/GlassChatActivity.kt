package com.kaleyra.collaboration_suite_glass_ui.chat

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.kaleyra.collaboration_suite_core_ui.chat.ChatActivity
import com.kaleyra.collaboration_suite_core_ui.chat.ChatService
import com.kaleyra.collaboration_suite_core_ui.chat.ChatUIDelegate
import com.kaleyra.collaboration_suite_core_ui.common.DeviceStatusDelegate
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraChatActivityGlassBinding

class GlassChatActivity : ChatActivity() {

    private lateinit var binding: KaleyraChatActivityGlassBinding

    private var service: ChatService? = null

    private val viewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(
            service as ChatUIDelegate,
            service as DeviceStatusDelegate
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.kaleyra_chat_activity_glass)

//        enableImmersiveMode()
    }

    override fun onServiceBound(service: ChatService) {
        this.service = service
    }
}