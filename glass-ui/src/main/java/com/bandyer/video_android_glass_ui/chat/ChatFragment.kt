package com.bandyer.video_android_glass_ui.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ObservableInt
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.video_android_core_ui.utils.Iso8601
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.GlassViewModel
import com.bandyer.video_android_glass_ui.GlassViewModelFactory
import com.bandyer.video_android_glass_ui.ProvidersHolder
import com.bandyer.video_android_glass_ui.common.ReadProgressDecoration
import com.bandyer.video_android_glass_ui.common.UserState
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentChatBinding
import com.bandyer.video_android_glass_ui.participants.ParticipantData
import com.bandyer.video_android_glass_ui.utils.GlassDeviceUtils
import com.bandyer.video_android_glass_ui.utils.TiltListener
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
class ChatFragment : BaseFragment(), TiltListener {

    private var _binding: BandyerGlassFragmentChatBinding? = null
    override val binding: BandyerGlassFragmentChatBinding get() = _binding!!

    private var itemAdapter: ItemAdapter<ChatMessageItem>? = null

    private var currentMsgItemIndex = 0
    private var newMessagesCounter = ObservableInt(-1)

    private var lastMsgIndex = 0
    private var pagesIds = arrayListOf<String>()

    private val activityViewModel: GlassViewModel by activityViewModels { GlassViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(activityViewModel.tiltEnabled) tiltListener = this
    }

    /**
     * @suppress
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        // Add view binding
        _binding = BandyerGlassFragmentChatBinding
            .inflate(inflater, container, false)
            .apply {
                if(GlassDeviceUtils.isRealWear)
                    bandyerBottomNavigation.setListenersForRealwear()

                // Init the RecyclerView
                bandyerMessages.apply {
                    itemAdapter = ItemAdapter()
                    val fastAdapter = FastAdapter.with(itemAdapter!!)
                    val layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    val snapHelper = PagerSnapHelper().also { it.attachToRecyclerView(this) }

                    this.layoutManager = layoutManager
                    adapter = fastAdapter
                    isFocusable = false
                    setHasFixedSize(true)
                    addItemDecoration(ReadProgressDecoration(requireContext()))

                    addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            val foundView = snapHelper.findSnapView(layoutManager) ?: return
                            val currentMsgIndex = layoutManager.getPosition(foundView)

                            if (currentMsgIndex > lastMsgIndex && pagesIds[currentMsgIndex] != pagesIds[lastMsgIndex]) {
                                newMessagesCounter?.apply { set(get() - 1) }
                                lastMsgIndex = currentMsgIndex
                            }
                            currentMsgItemIndex = currentMsgIndex
                        }
                    })

                    // Forward the root view's touch event to the recycler view
                    root.setOnTouchListener { _, event -> onTouchEvent(event) }
                }
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
//        newMessagesCounter = 0
        pagesIds = arrayListOf()
    }

    override fun onShow() = Unit

    override fun onTilt(deltaAzimuth: Float, deltaPitch: Float, deltaRoll: Float) =
        binding.bandyerMessages.scrollBy((deltaAzimuth * resources.displayMetrics.densityDpi / 5).toInt(), 0)

    override fun onTap() = true.also {
        val username = itemAdapter!!.adapterItems[currentMsgItemIndex].data.userAlias
        val action = ChatFragmentDirections.actionChatFragmentToChatMenuFragment(contactData.first { it.userAlias.contains(username!!) })
        findNavController().navigate(action)
    }

    override fun onSwipeDown() = true.also { findNavController().popBackStack() }

    override fun onSwipeForward(isKeyEvent: Boolean) = isKeyEvent.also {
        if (it) binding.bandyerMessages.horizontalSmoothScrollToNext(currentMsgItemIndex)
    }

    override fun onSwipeBackward(isKeyEvent: Boolean) = isKeyEvent.also {
        if (it) binding.bandyerMessages.horizontalSmoothScrollToPrevious(currentMsgItemIndex)
    }

    /**
     * Add a chat item to the recycler view. If the text is too long to fit in one screen, more than a chat item will be added
     *
     * @param data The [ChatMessageData]
     */
    private fun addChatItem(data: ChatMessageData) {
        newMessagesCounter.apply { set(get() + 1) }
        with(binding.bandyerChatMessage) {
            post {
                with(binding) {
                    bandyerName.text = data.sender
                    bandyerTime.text = Iso8601.parseTimestamp(requireContext(), data.time!!)
                    bandyerMessage.text = data.message
                    val pageList = bandyerMessage.paginate()
                    for (i in pageList.indices) {
                        itemAdapter!!.add(
                            ChatMessageItem(
                                ChatMessageData(
                                    data.id,
                                    data.sender,
                                    data.userAlias,
                                    pageList[i].toString(),
                                    data.time,
                                    data.userAvatarId,
                                    data.userAvatarUrl,
                                    i == 0
                                )
                            )
                        )
                        pagesIds.add(data.id)
                    }
                }
            }
        }
    }

    private val contactData = listOf(
        ParticipantData(
            "Mario Rossi",
            "Mario Rossi",
            UserState.ONLINE,
            null,
            null,
            Instant.now().toEpochMilli()
        ),
        ParticipantData(
            "Ugo Trapasso",
            "Ugo Trapasso",
             UserState.OFFLINE,
            null,
            "https://2.bp.blogspot.com/-jLEDf_NyZ1g/WmmyFZKOd-I/AAAAAAAAHd8/FZvIj2o_jqwl0S_yz4zBU16N1yGj-UCrACLcBGAs/s1600/heisenberg-breaking-bad.jpg",
            Instant.now().minus(8, ChronoUnit.DAYS).toEpochMilli()
        ),
        ParticipantData(
            "Gianfranco Sala",
            "Gianfranco Sala",
             UserState.INVITED,
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