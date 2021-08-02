package com.bandyer.demo_sdk_design.smartglass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bandyer.demo_sdk_design.R
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent
import com.bandyer.sdk_design.new_smartglass.chat.ChatItem
import com.bandyer.sdk_design.new_smartglass.chat.SmartGlassChatData
import com.bandyer.sdk_design.new_smartglass.chat.SmartGlassChatFragment
import java.util.*

class ChatFragment : SmartGlassChatFragment() {

    private val activity by lazy { requireActivity() as SmartGlassActivity }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity() as SmartGlassActivity).showStatusBar()
        activity.hideNotification()

        val view = super.onCreateView(inflater, container, savedInstanceState)

        itemAdapter!!.add(
            ChatItem(
                SmartGlassChatData(
                    "Mario",
                    "Mario",
                    "Il numero seriale del macchinario dovrebbe essere AR56000TY7-1824. Confermi?",
                    Date().time,
                    R.drawable.sample_image
                )
            )
        )
        itemAdapter!!.add(
            ChatItem(
                SmartGlassChatData(
                    "Ugo",
                    "Ugo",
                    "Come se fosse antani con lo scappellamento a sinistra",
                    Date().time
                )
            )
        )
        itemAdapter!!.add(
            ChatItem(
                SmartGlassChatData(
                    "Gianfranco",
                    "Gianfranco",
                    "Mi piacciono i treni",
                    Date().time
                )
            )
        )

        counter!!.text = "+3"
//        Handler(Looper.getMainLooper()).postDelayed({
//            itemAdapter!!.add(ChatItem("Francesco: La scatola è sulla sinistra"))
//
//        }, 200)
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