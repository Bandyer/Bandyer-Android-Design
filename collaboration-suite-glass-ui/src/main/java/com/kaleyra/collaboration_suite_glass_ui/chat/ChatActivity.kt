package com.kaleyra.collaboration_suite_glass_ui.chat

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.kaleyra.collaboration_suite_core_ui.call.CallUIController
import com.kaleyra.collaboration_suite_core_ui.call.CallUIDelegate
import com.kaleyra.collaboration_suite_core_ui.chat.ChatActivity
import com.kaleyra.collaboration_suite_core_ui.chat.ChatService
import com.kaleyra.collaboration_suite_core_ui.common.DeviceStatusDelegate
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.call.CallViewModel
import com.kaleyra.collaboration_suite_glass_ui.call.CallViewModelFactory
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraChatActivityGlassBinding

class ChatActivity : ChatActivity() {

    private lateinit var binding: KaleyraChatActivityGlassBinding

    private val viewModel: ChatViewModel by viewModels { ChatViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.kaleyra_chat_activity_glass)

//        enableImmersiveMode()
    }

    override fun onServiceBound(service: ChatService) = Unit
}