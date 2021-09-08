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
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.demo_sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerChatMessageLayoutBinding
import com.bandyer.sdk_design.new_smartglass.BandyerSmartGlassTouchEvent
import com.bandyer.sdk_design.new_smartglass.chat.BandyerChatItem
import com.bandyer.sdk_design.new_smartglass.chat.SmartGlassChatFragment
import com.bandyer.sdk_design.new_smartglass.chat.SmartGlassMessageData
import com.bandyer.sdk_design.new_smartglass.contact.BandyerContactData
import com.bandyer.sdk_design.new_smartglass.smoothScrollToNext
import com.bandyer.sdk_design.new_smartglass.smoothScrollToPrevious
import com.bandyer.sdk_design.new_smartglass.utils.Iso8601
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class ChatFragment : SmartGlassChatFragment(), TiltController.TiltListener {

    private val activity by lazy { requireActivity() as SmartGlassActivity }

    private var tiltController: TiltController? = null

    private var currentMsgItemIndex = 0
    private var newMessagesCounter = 0
        set(value) {
            field = value
            val counterValue = value - 1
            counter?.text = resources.getString(
                com.bandyer.sdk_design.R.string.bandyer_smartglass_message_counter_pattern,
                counterValue
            )
            counter?.visibility = if (counterValue > 0) View.VISIBLE else View.GONE
        }
    private var lastMsgIndex = 0
    private var pagesIds = arrayListOf<String>()

    private val contactData = listOf(
        BandyerContactData(
            "Mario Rossi",
            "Mario Rossi",
            BandyerContactData.UserState.ONLINE,
            R.drawable.sample_image,
            null,
            Instant.now().toEpochMilli()
        ),
        BandyerContactData(
            "Ugo Trapasso",
            "Ugo Trapasso",
            BandyerContactData.UserState.OFFLINE,
            null,
            "https://i.imgur.com/9I2qAlW.jpeg",
            Instant.now().minus(8, ChronoUnit.DAYS).toEpochMilli()
        ),
        BandyerContactData(
            "Gianfranco Sala",
            "Gianfranco Sala",
            BandyerContactData.UserState.INVITED,
            null,
            null,
            Instant.now().toEpochMilli()
        )
    )

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

        rvMessages!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager
                val foundView = snapHelper!!.findSnapView(layoutManager) ?: return
                val currentMsgIndex = layoutManager!!.getPosition(foundView)

                if (currentMsgIndex > lastMsgIndex && pagesIds[currentMsgIndex] != pagesIds[lastMsgIndex]) {
                    newMessagesCounter--
                    lastMsgIndex = currentMsgIndex
                }
                currentMsgItemIndex = currentMsgIndex
            }
        })

        addChatItem(
            SmartGlassMessageData(
                UUID.randomUUID().toString(),
                "Mario",
                "Mario",
                "Tuttavia, perché voi intendiate da dove sia nato tutto questo errore, di quelli che incolpano il piacere ed esaltano il dolore, io spiegherò tutta la questione, e presenterò le idee espresse dal famoso esploratore della verità, vorrei quasi dire dal costruttore della felicità umana. Nessuno, infatti, detesta, odia, o rifugge il piacere in quanto tale, solo perché è piacere, ma perché grandi sofferenze colpiscono quelli che non sono capaci di raggiungere il piacere attraverso la ragione; e al contrario, non c'è nessuno che ami, insegua, voglia raggiungere il dolore in se stesso, soltanto perché è dolore, ma perché qualche volta accadono situazioni tali per cui attraverso la sofferenza o il dolore si cerca di raggiungere un qualche grande piacere. Concentrandoci su casi di piccola importanza: chi di noi intraprende un esercizio ginnico, se non per ottenerne un qualche vantaggio? E d'altra parte, chi avrebbe motivo di criticare colui che desidera provare un piacere cui non segua nessun fastidio, o colui che fugge un dolore che non produce nessun piacere?",
                Instant.now().toEpochMilli(),
                R.drawable.sample_image
            )
        )
        addChatItem(
            SmartGlassMessageData(
                UUID.randomUUID().toString(),
                "Ugo",
                "Ugo",
                "Come se fosse antani con lo scappellamento a sinistra",
                Instant.now().toEpochMilli(),
                userAvatarUrl = "https://i.imgur.com/9I2qAlW.jpeg"
            )
        )
        addChatItem(
            SmartGlassMessageData(
                UUID.randomUUID().toString(),
                "Gianfranco",
                "Gianfranco",
                "Mi piacciono i treni",
                Instant.now().toEpochMilli()
            )
        )
//
//        Handler(Looper.getMainLooper()).postDelayed({
//            addChatItem(
//                SmartGlassMessageData(
//                    UUID.randomUUID().toString(),
//                    "Gianfranco",
//                    "Gianfranco",
//                    "La scatola è sulla sinistra",
//                    Instant.now().toEpochMilli()
//                )
//            )
//
//        }, 1000)

        bottomActionBar!!.setSwipeOnClickListener {
            rvMessages!!.smoothScrollToNext(currentMsgItemIndex)
        }

        bottomActionBar!!.setSwipeDownOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentMsgItemIndex = 0
        newMessagesCounter = 0
        lastMsgIndex = 0
        pagesIds = arrayListOf()
    }

    override fun onTilt(x: Float, y: Float) = rvMessages!!.scrollBy((x * 40).toInt(), 0)

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            tiltController!!.requestAllSensors()
        activity.setStatusBarColor(
            ResourcesCompat.getColor(
                resources,
                R.color.bandyer_smartglass_background_color,
                null
            )
        )
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            tiltController!!.releaseAllSensors()
        activity.setStatusBarColor(null)
    }

    override fun onSmartGlassTouchEvent(event: BandyerSmartGlassTouchEvent): Boolean =
        when (event.type) {
            BandyerSmartGlassTouchEvent.Type.SWIPE_FORWARD -> {
                if (event.source == BandyerSmartGlassTouchEvent.Source.KEY) {
                    rvMessages!!.smoothScrollToNext(currentMsgItemIndex)
                    true
                } else false
            }
            BandyerSmartGlassTouchEvent.Type.SWIPE_BACKWARD -> {
                if (event.source == BandyerSmartGlassTouchEvent.Source.KEY) {
                    rvMessages!!.smoothScrollToPrevious(currentMsgItemIndex)
                    true
                } else false
            }
            BandyerSmartGlassTouchEvent.Type.TAP -> {
                val username = itemAdapter!!.adapterItems[currentMsgItemIndex].data.userAlias
                val action =
                    ChatFragmentDirections.actionChatFragmentToContactDetailsFragment(
                        contactData.first { it.userAlias.contains(username!!) }
                    )
                findNavController().navigate(action)
                true
            }
            BandyerSmartGlassTouchEvent.Type.SWIPE_DOWN -> {
                findNavController().popBackStack()
                true
            }
            else -> super.onSmartGlassTouchEvent(event)
        }

    /**
     * Add a chat item to the recycler view. If the text is too long to fit in one screen, more than a chat item will be added
     *
     * @param data The [SmartGlassMessageData]
     */
    private fun addChatItem(data: SmartGlassMessageData) {
        newMessagesCounter++
        chatMessageView?.post {
            val binding = BandyerChatMessageLayoutBinding.bind(chatMessageView!!)
            with(binding) {
                bandyerName.text = data.sender
                bandyerTime.text = Iso8601.parseTimestamp(requireContext(), data.time!!)
                bandyerMessage.text = data.message
                val pageList = bandyerMessage.paginate()
                for (i in pageList.indices) {
                    val pageData = SmartGlassMessageData(
                        data.id,
                        data.sender,
                        data.userAlias,
                        pageList[i].toString(),
                        data.time,
                        data.userAvatarId,
                        data.userAvatarUrl,
                        i == 0
                    )
                    itemAdapter!!.add(BandyerChatItem(pageData))
                    pagesIds.add(data.id)
                }
            }
        }
    }
}