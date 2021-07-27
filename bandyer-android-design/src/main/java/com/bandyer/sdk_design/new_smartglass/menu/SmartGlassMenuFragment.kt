package com.bandyer.sdk_design.new_smartglass.menu

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.sdk_design.databinding.BandyerFragmentMenuBinding
import com.bandyer.sdk_design.new_smartglass.SmartGlassBaseFragment
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent
import com.bandyer.sdk_design.new_smartglass.bottom_action_bar.BottomActionBarView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

abstract class SmartGlassMenuFragment : SmartGlassBaseFragment() {

    private lateinit var binding: BandyerFragmentMenuBinding

    protected val itemAdapter = ItemAdapter<MenuItem>()
    protected val fastAdapter = FastAdapter.with(itemAdapter)

    protected var root: View? = null
    protected var rvMenu: RecyclerView? = null
    protected var bottomActionBar: BottomActionBarView? = null

    protected var currentMenuItemIndex = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BandyerFragmentMenuBinding.inflate(inflater, container, false)

        // set the views
        root = binding.root
        rvMenu = binding.menu
        bottomActionBar = binding.bottomActionBar

        // init the recycler view
        val layoutManager =
            CenterLinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvMenu!!.layoutManager = layoutManager
        rvMenu!!.adapter = fastAdapter
        rvMenu!!.clipToPadding = false
        rvMenu!!.isFocusable = true

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(rvMenu)

        rvMenu!!.addItemDecoration(
            MenuItemIndicatorDecoration(
                requireContext(),
                snapHelper
            )
        )

        // add scroll listener
        rvMenu!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val foundView = snapHelper.findSnapView(layoutManager) ?: return
                currentMenuItemIndex = layoutManager.getPosition(foundView)
            }
        })

        // pass the root view's touch event to the recycler view
        root!!.setOnTouchListener { _, event -> rvMenu!!.onTouchEvent(event) }
        return root!!
    }

    override fun onDestroyView() {
        super.onDestroyView()
        root = null
        rvMenu = null
        bottomActionBar = null
    }

    abstract override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean
}