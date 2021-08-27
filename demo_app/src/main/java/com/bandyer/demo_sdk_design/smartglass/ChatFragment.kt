package com.bandyer.demo_sdk_design.smartglass

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bandyer.demo_sdk_design.R
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent
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

        addChatItem(
            SmartGlassChatData(
                UUID.randomUUID().toString(),
                "Mario",
                "Mario",
                "Sed euismod odio vitae lectus facilisis ornare. Suspendisse sodales dolor sapien, at vestibulum justo egestas ut. Curabitur dapibus, mi vel placerat iaculis, elit nisi lacinia magna, et cursus nulla mi ut metus. Mauris lobortis ullamcorper venenatis. Fusce auctor accumsan ipsum, eu tincidunt tortor ultrices ultrices. Cras mauris massa, eleifend sed elit a, viverra mattis urna. Sed finibus nunc in consectetur varius. Aenean vestibulum id nulla non pharetra. Proin consequat elit id neque tincidunt posuere. Donec commodo, augue nec consectetur scelerisque, elit ligula vehicula eros, a iaculis felis arcu eget dui. Sed euismod odio vitae lectus facilisis ornare. Suspendisse sodales dolor sapien, at vestibulum justo egestas ut",
                Date().time,
                R.drawable.sample_image
            )
        )
        addChatItem(
            SmartGlassChatData(
                UUID.randomUUID().toString(),
                "Ugo",
                "Ugo",
                "Come se fosse antani con lo scappellamento a sinistra",
                Date().time
            )
        )
        addChatItem(
            SmartGlassChatData(
                UUID.randomUUID().toString(),
                "Gianfranco",
                "Gianfranco",
                "Mi piacciono i treni",
                Date().time
            )
        )

        Handler(Looper.getMainLooper()).postDelayed({
            addChatItem(
                SmartGlassChatData(
                    UUID.randomUUID().toString(),
                    "Gianfranco",
                    "Gianfranco",
                    "La scatola è sulla sinistra",
                    Date().time
                )
            )

        }, 1000)
        return view
    }

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent): Boolean =
        when (event.type) {
            SmartGlassTouchEvent.Type.SWIPE_DOWN -> {
                findNavController().popBackStack()
                true
            }
            else -> super.onSmartGlassTouchEvent(event)
        }
}