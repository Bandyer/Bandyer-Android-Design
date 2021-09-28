package com.bandyer.video_android_glass_ui.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.app_design.smartglass.ChatFragmentDirections
import com.bandyer.video_android_core_ui.utils.Iso8601
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.TouchEvent
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassChatMessageLayoutBinding
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentChatBinding
import com.bandyer.video_android_glass_ui.menu.MenuItem
import com.bandyer.video_android_glass_ui.utils.TiltController
import com.bandyer.video_android_glass_ui.utils.extensions.horizontalSmoothScrollToNext
import com.bandyer.video_android_glass_ui.utils.extensions.horizontalSmoothScrollToPrevious
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * ChatFragment
 */
class ChatFragment : BaseFragment(), TiltController.TiltListener {

    //    private val activity by lazy { requireActivity() as SmartGlassActivity }

    private var _binding: BandyerGlassFragmentChatBinding? = null
    override val binding: BandyerGlassFragmentChatBinding get() = _binding!!

    private var itemAdapter: ItemAdapter<ChatMessageItem>? = null

    private var tiltController: TiltController? = null

    private var currentMsgItemIndex = 0
    private var newMessagesCounter = 0
        set(value) {
            field = value.also {
                with(binding.bandyerCounter) {
                    text = resources.getString(R.string.bandyer_glass_message_counter_pattern,it - 1)
                    visibility = if (it - 1 > 0) View.VISIBLE else View.GONE
                }
            }
        }
    private var lastMsgIndex = 0
    private var pagesIds = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tiltController = TiltController(requireContext(), this)
    }

    override fun onResume() {
        super.onResume()
        tiltController!!.requestAllSensors()
//        activity.setStatusBarColor(
//            ResourcesCompat.getColor(
//                resources,
//                R.color.bandyer_glass_background_color,
//                null
//            )
//        )
    }

    override fun onPause() {
        super.onPause()
        tiltController!!.releaseAllSensors()
//        activity.setStatusBarColor(null)
    }

    /**
     * @suppress
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        activity.showStatusBar()
//        activity.hideNotification()
//        activity.setDnd(true)

        // Apply theme wrapper and add view binding
        _binding = BandyerGlassFragmentChatBinding.inflate(
            inflater.cloneInContext(ContextThemeWrapper(requireContext(), R.style.BandyerSDKDesign_Theme_GlassChat)),
            container,
            false
        )

        // Init the RecyclerView
        binding.bandyerMessages.apply {
            itemAdapter = ItemAdapter()
            val fastAdapter = FastAdapter.with(itemAdapter!!)
            val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            val snapHelper = PagerSnapHelper().also { it.attachToRecyclerView(this) }

            this.layoutManager = layoutManager
            adapter = fastAdapter
            isFocusable = false
            setHasFixedSize(true)
            addItemDecoration(ChatReadProgressDecoration(requireContext()))

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val foundView = snapHelper.findSnapView(layoutManager) ?: return
                    val currentMsgIndex = layoutManager.getPosition(foundView)

                    if (currentMsgIndex > lastMsgIndex && pagesIds[currentMsgIndex] != pagesIds[lastMsgIndex]) {
                        newMessagesCounter--
                        lastMsgIndex = currentMsgIndex
                    }
                    currentMsgItemIndex = currentMsgIndex
                }
            })

            // pass the root view's touch event to the recycler view
            binding.root.setOnTouchListener { _, event -> onTouchEvent(event) }
        }

        // Set OnClickListeners for realwear voice commands
        with(binding.bandyerBottomNavigation) {
            setSwipeHorizontalOnClickListener { onSwipeForward(true) }
            setSwipeDownOnClickListener { onSwipeDown() }
        }

        mockMessages()

        return binding.root
    }

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        itemAdapter = null
//        activity.setDnd(false)
        pagesIds = arrayListOf()
    }

    override fun onTilt(deltaAzimuth: Float, deltaPitch: Float, deltaRoll: Float) =
        binding.bandyerMessages.scrollBy((deltaAzimuth * resources.displayMetrics.densityDpi / 5).toInt(), 0)

    override fun onTouch(event: TouchEvent): Boolean = when (event.type) {
        TouchEvent.Type.TAP -> onTap()
        TouchEvent.Type.SWIPE_DOWN -> onSwipeDown()
        TouchEvent.Type.SWIPE_FORWARD -> onSwipeForward(event.source == TouchEvent.Source.KEY)
        TouchEvent.Type.SWIPE_BACKWARD -> onSwipeBackward(event.source == TouchEvent.Source.KEY)
        else -> super.onTouch(event)
    }

    private fun onTap(): Boolean {
        val username = itemAdapter!!.adapterItems[currentMsgItemIndex].data.userAlias
        val action = ChatFragmentDirections.actionChatFragmentToChatMenuFragment(contactData.first { it.userAlias.contains(username!!) })
        findNavController().navigate(action)
        return true
    }

    private fun onSwipeDown(): Boolean {
        findNavController().popBackStack()
        return true
    }

    private fun onSwipeForward(isKeyEvent: Boolean): Boolean {
        if (isKeyEvent)
            binding.bandyerMessages.horizontalSmoothScrollToNext(currentMsgItemIndex)
        return isKeyEvent
    }

    private fun onSwipeBackward(isKeyEvent: Boolean): Boolean {
        if (isKeyEvent)
            binding.bandyerMessages.horizontalSmoothScrollToPrevious(currentMsgItemIndex)
        return isKeyEvent
    }

    /**
     * Add a chat item to the recycler view. If the text is too long to fit in one screen, more than a chat item will be added
     *
     * @param data The [ChatMessageData]
     */
    private fun addChatItem(data: ChatMessageData) {
        newMessagesCounter++
        with(binding.bandyerChatMessage) {
            post {
                val binding = BandyerGlassChatMessageLayoutBinding.bind(this)
                with(binding) {
                    bandyerName.text = data.sender
                    bandyerTime.text = Iso8601.parseTimestamp(requireContext(), data.time!!)
                    bandyerMessage.text = data.message
                    val pageList = bandyerMessage.paginate()
                    for (i in pageList.indices) {
                        itemAdapter!!.add(ChatMessageItem(ChatMessageData(data.id, data.sender, data.userAlias, pageList[i].toString(), data.time, data.userAvatarId, data.userAvatarUrl, i == 0)))
                        pagesIds.add(data.id)
                    }
                }
            }
        }
    }

    private val contactData = listOf(
        com.bandyer.video_android_glass_ui.participants.ParticipantData(
            "Mario Rossi",
            "Mario Rossi",
            com.bandyer.video_android_glass_ui.participants.ParticipantData.UserState.ONLINE,
            null,
            null,
            Instant.now().toEpochMilli()
        ),
        com.bandyer.video_android_glass_ui.participants.ParticipantData(
            "Ugo Trapasso",
            "Ugo Trapasso",
            com.bandyer.video_android_glass_ui.participants.ParticipantData.UserState.OFFLINE,
            null,
            "https://2.bp.blogspot.com/-jLEDf_NyZ1g/WmmyFZKOd-I/AAAAAAAAHd8/FZvIj2o_jqwl0S_yz4zBU16N1yGj-UCrACLcBGAs/s1600/heisenberg-breaking-bad.jpg",
            Instant.now().minus(8, ChronoUnit.DAYS).toEpochMilli()
        ),
        com.bandyer.video_android_glass_ui.participants.ParticipantData(
            "Gianfranco Sala",
            "Gianfranco Sala",
            com.bandyer.video_android_glass_ui.participants.ParticipantData.UserState.INVITED,
            null,
            null,
            Instant.now().toEpochMilli()
        )
    )

    private fun mockMessages() {
        addChatItem(
            ChatMessageData(
                UUID.randomUUID().toString(),
                "Mario",
                "Mario",
                "Tuttavia, perché voi intendiate da dove sia nato tutto questo errore, di quelli che incolpano il piacere ed esaltano il dolore, io spiegherò tutta la questione, e presenterò le idee espresse dal famoso esploratore della verità, vorrei quasi dire dal costruttore della felicità umana. Nessuno, infatti, detesta, odia, o rifugge il piacere in quanto tale, solo perché è piacere, ma perché grandi sofferenze colpiscono quelli che non sono capaci di raggiungere il piacere attraverso la ragione; e al contrario, non c'è nessuno che ami, insegua, voglia raggiungere il dolore in se stesso, soltanto perché è dolore, ma perché qualche volta accadono situazioni tali per cui attraverso la sofferenza o il dolore si cerca di raggiungere un qualche grande piacere. Concentrandoci su casi di piccola importanza: chi di noi intraprende un esercizio ginnico, se non per ottenerne un qualche vantaggio? E d'altra parte, chi avrebbe motivo di criticare colui che desidera provare un piacere cui non segua nessun fastidio, o colui che fugge un dolore che non produce nessun piacere?",
                Instant.now().toEpochMilli()
            )
        )
        addChatItem(
            ChatMessageData(
                UUID.randomUUID().toString(),
                "Ugo",
                "Ugo",
                "Come se fosse antani con lo scappellamento a sinistra",
                Instant.now().toEpochMilli(),
                userAvatarUrl = "https://2.bp.blogspot.com/-jLEDf_NyZ1g/WmmyFZKOd-I/AAAAAAAAHd8/FZvIj2o_jqwl0S_yz4zBU16N1yGj-UCrACLcBGAs/s1600/heisenberg-breaking-bad.jpg"
            )
        )
        addChatItem(
            ChatMessageData(
                UUID.randomUUID().toString(),
                "Gianfranco",
                "Gianfranco",
                "Mi piacciono i treni",
                Instant.now().toEpochMilli()
            )
        )
    }
}