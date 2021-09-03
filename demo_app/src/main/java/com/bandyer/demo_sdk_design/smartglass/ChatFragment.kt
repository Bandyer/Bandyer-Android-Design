package com.bandyer.demo_sdk_design.smartglass

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import com.bandyer.demo_sdk_design.R
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent
import com.bandyer.sdk_design.new_smartglass.chat.SmartGlassMessageData
import com.bandyer.sdk_design.new_smartglass.chat.SmartGlassChatFragment
import com.bandyer.sdk_design.new_smartglass.smoothScrollToNext
import java.util.*

class ChatFragment : SmartGlassChatFragment(), TiltController.TiltListener {

    private val activity by lazy { requireActivity() as SmartGlassActivity }

    private var tiltController: TiltController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tiltController =
                TiltController(
                    requireContext(),
                    this
                )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity() as SmartGlassActivity).showStatusBar()
        activity.hideNotification()

        val view = super.onCreateView(inflater, container, savedInstanceState)

        if(Build.MODEL == resources.getString(R.string.bandyer_smartglass_realwear_model_name))
            bottomActionBar!!.setSwipeText(resources.getString(R.string.bandyer_smartglass_right_left))

        addChatItem(
            SmartGlassMessageData(
                UUID.randomUUID().toString(),
                "Mario",
                "Mario",
                "Tuttavia, perché voi intendiate da dove sia nato tutto questo errore, di quelli che incolpano il piacere ed esaltano il dolore, io spiegherò tutta la questione, e presenterò le idee espresse dal famoso esploratore della verità, vorrei quasi dire dal costruttore della felicità umana. Nessuno, infatti, detesta, odia, o rifugge il piacere in quanto tale, solo perché è piacere, ma perché grandi sofferenze colpiscono quelli che non sono capaci di raggiungere il piacere attraverso la ragione; e al contrario, non c'è nessuno che ami, insegua, voglia raggiungere il dolore in se stesso, soltanto perché è dolore, ma perché qualche volta accadono situazioni tali per cui attraverso la sofferenza o il dolore si cerca di raggiungere un qualche grande piacere. Concentrandoci su casi di piccola importanza: chi di noi intraprende un esercizio ginnico, se non per ottenerne un qualche vantaggio? E d'altra parte, chi avrebbe motivo di criticare colui che desidera provare un piacere cui non segua nessun fastidio, o colui che fugge un dolore che non produce nessun piacere?",
                Date().time,
                R.drawable.sample_image
            )
        )
        addChatItem(
            SmartGlassMessageData(
                UUID.randomUUID().toString(),
                "Ugo",
                "Ugo",
                "Come se fosse antani con lo scappellamento a sinistra",
                Date().time
            )
        )
        addChatItem(
            SmartGlassMessageData(
                UUID.randomUUID().toString(),
                "Gianfranco",
                "Gianfranco",
                "Mi piacciono i treni",
                Date().time
            )
        )

        Handler(Looper.getMainLooper()).postDelayed({
            addChatItem(
                SmartGlassMessageData(
                    UUID.randomUUID().toString(),
                    "Gianfranco",
                    "Gianfranco",
                    "La scatola è sulla sinistra",
                    Date().time
                )
            )

        }, 1000)

        bottomActionBar!!.setTapText(null)

        bottomActionBar!!.setSwipeOnClickListener {
            rvMessages!!.smoothScrollToNext(currentMsgItemIndex)
        }

        bottomActionBar!!.setSwipeDownOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }

    override fun onTilt(x: Float, y: Float) = rvMessages!!.scrollBy((x * 40).toInt(), 0)

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            tiltController!!.requestAllSensors()
        activity.setStatusBarColor(ResourcesCompat.getColor(resources, R.color.bandyer_smartglass_background_color, null))
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            tiltController!!.releaseAllSensors()
        activity.setStatusBarColor(null)
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