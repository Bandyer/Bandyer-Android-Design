package com.kaleyra.collaboration_suite_glass_ui.common

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils
import com.kaleyra.collaboration_suite_glass_ui.bottom_navigation.BottomNavigationView
import com.kaleyra.collaboration_suite_glass_ui.call.adapter_items.ParticipantItem
import com.kaleyra.collaboration_suite_glass_ui.common.item_decoration.HorizontalCenterItemDecoration
import com.kaleyra.collaboration_suite_glass_ui.common.item_decoration.MenuProgressIndicator
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassFragmentParticipantsBinding
import com.kaleyra.collaboration_suite_glass_ui.utils.TiltListener
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.ContextExtensions.tiltScrollFactor
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.horizontalSmoothScrollToNext
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.horizontalSmoothScrollToPrevious
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import kotlinx.coroutines.launch

/**
 * ParticipantsFragment
 */
internal abstract class ParticipantsFragment : BaseFragment(), TiltListener {

    private var _binding: KaleyraGlassFragmentParticipantsBinding? = null
    override val binding: KaleyraGlassFragmentParticipantsBinding get() = _binding!!

    protected var itemAdapter: ItemAdapter<ParticipantItem>? = null
    protected var snapHelper: LinearSnapHelper? = null

    protected var currentParticipantIndex = -1

    protected abstract val usersDescription: UsersDescription

    /**
     * @suppress
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        // Apply theme wrapper and add view binding
        _binding = KaleyraGlassFragmentParticipantsBinding
            .inflate(inflater, container, false)
            .apply {
                if (DeviceUtils.isRealWear)
                    setListenersForRealWear(kaleyraBottomNavigation)

                // Init the RecyclerView
                with(kaleyraParticipants) {
                    itemAdapter = ItemAdapter()
                    val fastAdapter = FastAdapter.with(itemAdapter!!)
                    val layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    snapHelper = LinearSnapHelper().also { it.attachToRecyclerView(this) }

                    this.layoutManager = layoutManager
                    adapter = fastAdapter
                    isFocusable = false

                    addItemDecoration(HorizontalCenterItemDecoration())
                    addItemDecoration(MenuProgressIndicator(requireContext(), snapHelper!!))

                    // Forward the root view's touch event to the recycler view
                    root.setOnTouchListener { _, event -> onTouchEvent(event) }
                }
            }

        bindUI()

        return binding.root
    }

    protected open fun bindUI() {
        with(binding.kaleyraParticipants) {
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val foundView = snapHelper!!.findSnapView(layoutManager) ?: return
                    currentParticipantIndex = layoutManager!!.getPosition(foundView)

                    val userId =
                        itemAdapter!!.getAdapterItem(currentParticipantIndex).data.userId
                    with(binding.kaleyraUserInfo) {
                        lifecycleScope.launch {
                            val name = usersDescription.name(listOf(userId))
                            setName(name)

                            val image = usersDescription.image(listOf(userId))
                            if (image != Uri.EMPTY) setAvatar(image)
                            else {
                                setAvatar(null)
                                setAvatarBackgroundAndLetter(name)
                            }
                        }
                    }
                    onParticipantScrolled(userId)
                }
            })
        }
    }

    protected open fun onParticipantScrolled(userId: String) = Unit

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        itemAdapter = null
    }

    override fun onTilt(deltaAzimuth: Float, deltaPitch: Float, deltaRoll: Float) =
        binding.kaleyraParticipants.scrollBy(
            (deltaAzimuth * requireContext().tiltScrollFactor()).toInt(),
            0
        )

    override fun onTap() = false

    override fun onSwipeDown() = true.also { findNavController().popBackStack() }

    override fun onSwipeForward(isKeyEvent: Boolean) =
        (isKeyEvent && currentParticipantIndex != -1).also {
            if (it) binding.kaleyraParticipants.horizontalSmoothScrollToNext(
                currentParticipantIndex
            )
        }

    override fun onSwipeBackward(isKeyEvent: Boolean) =
        (isKeyEvent && currentParticipantIndex != -1).also {
            if (it) binding.kaleyraParticipants.horizontalSmoothScrollToPrevious(
                currentParticipantIndex
            )
        }

    override fun setListenersForRealWear(bottomNavView: BottomNavigationView) {
        super.setListenersForRealWear(bottomNavView)
        bottomNavView.setFirstItemListeners({ onSwipeForward(true) }, { onSwipeBackward(true) })
    }
}