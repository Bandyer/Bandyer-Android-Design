package com.bandyer.demo_sdk_design.smartglass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent
import com.bandyer.sdk_design.new_smartglass.chat.ChatItem
import com.bandyer.sdk_design.new_smartglass.chat.SmartGlassChatFragment

class ChatFragment : SmartGlassChatFragment() {

    private val activity by lazy { requireActivity() as SmartGlassActivity }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity.hideNotification()

        val view = super.onCreateView(inflater, container, savedInstanceState)

        itemAdapter.add(ChatItem("Mario: Il numero seriale del macchinario dovrebbe essere AR56000TY7-1824\\nConfermi?"))
        itemAdapter.add(ChatItem("Francesco: La scatola è sulla sinistra"))
        itemAdapter.add(ChatItem("Gianfranco: Mi piacciono i treni"))

        return view
    }

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean =
        when (event) {
            SmartGlassTouchEvent.Event.SWIPE_DOWN -> {
                findNavController().popBackStack()
                true
            }
            else -> false
        }
}