package com.bandyer.video_android_glass_ui.chat.menu

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.video_android_glass_ui.*
import com.bandyer.video_android_glass_ui.GlassViewModel
import com.bandyer.video_android_glass_ui.GlassViewModelFactory
import com.bandyer.video_android_glass_ui.chat.ChatFragmentArgs
import com.bandyer.video_android_glass_ui.common.item_decoration.HorizontalCenterItemDecoration
import com.bandyer.video_android_glass_ui.common.item_decoration.MenuProgressIndicator
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentChatMenuBinding
import com.bandyer.video_android_glass_ui.utils.GlassDeviceUtils
import com.bandyer.video_android_glass_ui.utils.TiltListener
import com.bandyer.video_android_glass_ui.utils.extensions.ContextExtensions.getChatThemeAttribute
import com.bandyer.video_android_glass_ui.utils.extensions.horizontalSmoothScrollToNext
import com.bandyer.video_android_glass_ui.utils.extensions.horizontalSmoothScrollToPrevious
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

/**
 * ChatMenuFragment
 */
internal class ChatMenuFragment : BaseFragment(), TiltListener {

    private var _binding: BandyerGlassFragmentChatMenuBinding? = null
    override val binding: BandyerGlassFragmentChatMenuBinding get() = _binding!!

    private var itemAdapter: ItemAdapter<ChatMenuItem>? = null

    private val args: ChatMenuFragmentArgs by navArgs()

    private var actionIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(args.enableTilt) tiltListener = this
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

        // Args
//        val data = args.participantData!!

        // Apply theme wrapper and add view binding
        val themeResId = requireContext().getChatThemeAttribute(R.styleable.BandyerSDKDesign_Theme_Glass_Chat_bandyer_chatMenuStyle)
        _binding = BandyerGlassFragmentChatMenuBinding.inflate(
            inflater.cloneInContext(ContextThemeWrapper(requireActivity(), themeResId)),
            container,
            false
        ).apply {
            if(GlassDeviceUtils.isRealWear)
                bandyerBottomNavigation.setListenersForRealwear()

            // Init the RecyclerView
            with(bandyerActions) {
                itemAdapter = ItemAdapter()
                val fastAdapter = FastAdapter.with(itemAdapter!!)
                val layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                val snapHelper = LinearSnapHelper().also { it.attachToRecyclerView(this) }

                this.layoutManager = layoutManager
                adapter = fastAdapter
                isFocusable = false
                setHasFixedSize(true)

                addItemDecoration(HorizontalCenterItemDecoration())
                addItemDecoration(MenuProgressIndicator(requireContext(), snapHelper))

                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        val foundView = snapHelper.findSnapView(layoutManager) ?: return
                        actionIndex = layoutManager.getPosition(foundView)
                    }
                })

                // Forward the root view's touch event to the recycler view
                root.setOnTouchListener { _, event -> onTouchEvent(event) }
            }

            // TODO ricordarsi di settare nel data binding i dati degli utenti
            // TODO Mettere modello User con name, avatar, state
        }

        with(itemAdapter!!) {
            add(ChatMenuItem(resources.getString(R.string.bandyer_glass_videocall)))
            add(ChatMenuItem(resources.getString(R.string.bandyer_glass_call)))
        }

        return binding.root
    }

    override fun onServiceBound() = Unit

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        itemAdapter = null
    }

    override fun onTilt(deltaAzimuth: Float, deltaPitch: Float, deltaRoll: Float) =
        binding.bandyerActions.scrollBy((deltaAzimuth * resources.displayMetrics.densityDpi / 5).toInt(), 0)

    override fun onTap() = false

    override fun onSwipeDown() = true.also { findNavController().popBackStack() }

    override fun onSwipeForward(isKeyEvent: Boolean) = isKeyEvent.also { if(it) binding.bandyerActions.horizontalSmoothScrollToNext(actionIndex) }

    override fun onSwipeBackward(isKeyEvent: Boolean) = isKeyEvent.also { if(it) binding.bandyerActions.horizontalSmoothScrollToPrevious(actionIndex) }
}