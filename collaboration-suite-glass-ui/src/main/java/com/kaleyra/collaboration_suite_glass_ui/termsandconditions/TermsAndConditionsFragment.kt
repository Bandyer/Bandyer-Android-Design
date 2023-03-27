/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_glass_ui.termsandconditions

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.kaleyra.collaboration_suite.chatbox.ChatBox
import com.kaleyra.collaboration_suite.phonebox.PhoneBox
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.extensions.TermsAndConditionsExt.accept
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.extensions.TermsAndConditionsExt.decline
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.model.TermsAndConditions
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.getThemeAttribute
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.bottom_navigation.BottomNavigationView
import com.kaleyra.collaboration_suite_glass_ui.common.BaseFragment
import com.kaleyra.collaboration_suite_glass_ui.common.ReadProgressDecoration
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraFragmentTermsAndConditionsBinding
import com.kaleyra.collaboration_suite_glass_ui.utils.TiltListener
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.ContextExtensions.tiltScrollFactor
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.horizontalSmoothScrollToNext
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.horizontalSmoothScrollToPrevious
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import kotlinx.coroutines.flow.*

internal class TermsAndConditionsFragment : BaseFragment(), TiltListener {

    private var _binding: KaleyraFragmentTermsAndConditionsBinding? = null
    override val binding: KaleyraFragmentTermsAndConditionsBinding get() = _binding!!

    private var itemAdapter: ItemAdapter<TermsAndConditionsItem>? = null

    private var currentMsgItemIndex = -1

    private val args: TermsAndConditionsFragmentArgs by navArgs()

    private var termsAndConditions: TermsAndConditions? = null

    private val viewModel: TermsAndConditionsViewModel by activityViewModels()

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
            R.style.KaleyraCollaborationSuiteUI_TermsAndConditionsTheme_Glass,
            R.styleable.KaleyraCollaborationSuiteUI_TermsAndConditionsTheme_Glass,
            R.styleable.KaleyraCollaborationSuiteUI_TermsAndConditionsTheme_Glass_kaleyra_termsAndConditionsStyle
        )
        _binding = KaleyraFragmentTermsAndConditionsBinding.inflate(
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

            termsAndConditions = args.termsAndConditions
            termsAndConditions?.apply {
                kaleyraTitle.text = title
                with(kaleyraMessage) {
                    post {
                        text = message
                        val items = paginate().map { TermsAndConditionsItem(it.toString()) }
                        itemAdapter!!.set(items)
                    }
                }

                with(kaleyraBottomNavigation) {
                    setSecondItemActionText(acceptText)
                    setThirdItemActionText(declineText)
                    setSecondItemContentDescription(acceptText)
                    setThirdItemContentDescription(declineText)
                }
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


    override fun onTap(): Boolean {
        val activity = requireActivity() as? GlassTermsAndConditionsActivity ?: return false
        termsAndConditions?.accept()
        combine(viewModel.phoneBox
            .flatMapLatest { it.state }, viewModel.chatBox
            .flatMapLatest { it.state }) { pbState, cbState ->
            pbState == PhoneBox.State.Connected && cbState == ChatBox.State.Connected
        }
            .takeWhile { !it }
            .onCompletion { activity.finishAndRemoveTask() }
            .launchIn(lifecycleScope)
        return true
    }

    override fun onSwipeDown(): Boolean {
        val activity = requireActivity() as? GlassTermsAndConditionsActivity ?: return false
        termsAndConditions?.decline()
        activity.finishAndRemoveTask()
        return true
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