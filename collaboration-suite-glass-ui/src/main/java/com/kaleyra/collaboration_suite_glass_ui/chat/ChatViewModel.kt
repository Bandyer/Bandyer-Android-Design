package com.kaleyra.collaboration_suite_glass_ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kaleyra.collaboration_suite_core_ui.call.CallUIDelegate
import com.kaleyra.collaboration_suite_glass_ui.call.CallViewModel

@Suppress("UNCHECKED_CAST")
internal class ChatViewModelFactory(

) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ChatViewModel() as T
}

class ChatViewModel(): ViewModel() {
}