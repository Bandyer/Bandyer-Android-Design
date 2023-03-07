package com.kaleyra.collaboration_suite_glass_ui.userdataconsentagreement

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.getThemeAttribute
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.bottom_navigation.BottomNavigationView
import com.kaleyra.collaboration_suite_glass_ui.common.BaseFragment
import com.kaleyra.collaboration_suite_glass_ui.common.ReadProgressDecoration
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraFragmentUserDataConsentAgreementBinding
import com.kaleyra.collaboration_suite_glass_ui.utils.TiltListener
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.ContextExtensions.tiltScrollFactor
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.horizontalSmoothScrollToNext
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.horizontalSmoothScrollToPrevious
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

internal class UserDataConsentAgreementFragment : BaseFragment(), TiltListener {

    private var _binding: KaleyraFragmentUserDataConsentAgreementBinding? = null
    override val binding: KaleyraFragmentUserDataConsentAgreementBinding get() = _binding!!

    private var itemAdapter: ItemAdapter<UserDataConsentAgreementItem>? = null

    private var currentMsgItemIndex = -1

    private val args: UserDataConsentAgreementFragmentArgs by lazy { UserDataConsentAgreementFragmentArgs.fromBundle(requireActivity().intent?.extras ?: Bundle()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (args.enableTilt) tiltListener = this
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        val themeResId = requireContext().getThemeAttribute(
            R.style.KaleyraCollaborationSuiteUI_UserDataConsentAgreementTheme_Glass,
            R.styleable.KaleyraCollaborationSuiteUI_UserDataConsentAgreementTheme_Glass,
            R.styleable.KaleyraCollaborationSuiteUI_UserDataConsentAgreementTheme_Glass_kaleyra_userDataConsentAgreementStyle
        )
        _binding = KaleyraFragmentUserDataConsentAgreementBinding.inflate(
            inflater.cloneInContext(ContextThemeWrapper(requireActivity(), themeResId)),
            container,
            false
        ).apply {
            if (DeviceUtils.isRealWear)
                setListenersForRealWear(kaleyraBottomNavigation)

            with(kaleyraMessageRecyclerView) {
                val snapHelper = PagerSnapHelper()
                itemAdapter = ItemAdapter()
                val fastAdapter = FastAdapter.with(itemAdapter!!)
                val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

                this.layoutManager = layoutManager
                this.adapter = fastAdapter
                this.isFocusable = false
                this.setHasFixedSize(true)
                this.addItemDecoration(ReadProgressDecoration(requireContext()))
                snapHelper.attachToRecyclerView(this)

                this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    private var lastView: View? = null

                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        val foundView = snapHelper.findSnapView(layoutManager) ?: return
                        val position = layoutManager.getPosition(foundView)
                        if (currentMsgItemIndex == position && lastView == foundView) return
                        currentMsgItemIndex = position
                        lastView = foundView
                    }
                })

                // Forward the root view's touch event to the recycler view
                root.setOnTouchListener { _, event -> onTouchEvent(event) }
            }

            kaleyraTitle.text = args.title
            with(kaleyraMessage) {
                post {
                    text = args.message
                    val items = paginate().map { UserDataConsentAgreementItem(it.toString()) }
                    itemAdapter!!.set(items)
                }
            }

            with(kaleyraBottomNavigation) {
                setSecondItemActionText(args.acceptText)
                setThirdItemActionText(args.declineText)
                setSecondItemContentDescription(args.acceptText)
                setThirdItemContentDescription(args.declineText)
            }
        }

        return binding.root
    }

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        itemAdapter = null
    }

    override fun onTap(): Boolean = onCallback(args.acceptCallback?.block)

    override fun onSwipeDown(): Boolean = onCallback(args.declineCallback?.block)

    private fun onCallback(callback: (() -> Unit)?): Boolean {
        return if (callback != null) {
            callback.invoke()
            requireActivity().finishAndRemoveTask()
            true
        } else false
    }

    override fun onSwipeForward(isKeyEvent: Boolean) = isKeyEvent.also {
        if (it) binding.kaleyraMessageRecyclerView.horizontalSmoothScrollToNext(currentMsgItemIndex)
    }

    override fun onSwipeBackward(isKeyEvent: Boolean) = isKeyEvent.also {
        if (it) binding.kaleyraMessageRecyclerView.horizontalSmoothScrollToPrevious(currentMsgItemIndex)
    }

    override fun onTilt(deltaAzimuth: Float, deltaPitch: Float, deltaRoll: Float) {
        binding.kaleyraMessageRecyclerView.scrollBy((deltaAzimuth * requireContext().tiltScrollFactor()).toInt(), 0)
    }

    override fun setListenersForRealWear(bottomNavView: BottomNavigationView) {
        super.setListenersForRealWear(bottomNavView)
        bottomNavView.setFirstItemListeners({ onSwipeForward(true) }, { onSwipeBackward(true) })
    }

}