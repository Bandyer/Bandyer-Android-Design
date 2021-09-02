package com.bandyer.sdk_design.new_smartglass.menu

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.*
import com.bandyer.sdk_design.databinding.BandyerFragmentMenuBinding
import com.bandyer.sdk_design.new_smartglass.SmartGlassBaseFragment
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent
import com.bandyer.sdk_design.new_smartglass.bottom_action_bar.BottomActionBarView
import com.bandyer.sdk_design.new_smartglass.smoothScrollToNext
import com.bandyer.sdk_design.new_smartglass.smoothScrollToPrevious
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

abstract class SmartGlassMenuFragment : SmartGlassBaseFragment() {

    private var binding: BandyerFragmentMenuBinding? = null

    protected var itemAdapter: ItemAdapter<MenuItem>? = null
    protected var fastAdapter: FastAdapter<MenuItem>? = null

    protected var root: ViewGroup? = null
    protected var rvMenu: RecyclerView? = null
    protected var bottomActionBar: BottomActionBarView? = null

    protected var currentMenuItemIndex = 0
    private var lastMotionEventAction: Int? = null
    private var snapHelper: LinearSnapHelper? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BandyerFragmentMenuBinding.inflate(inflater, container, false)

        // set the views
        root = binding!!.root
        rvMenu = binding!!.menu
        bottomActionBar = binding!!.bottomActionBar

        // init the recycler view
        itemAdapter = ItemAdapter()
        fastAdapter = FastAdapter.with(itemAdapter!!)
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvMenu!!.layoutManager = layoutManager
        rvMenu!!.adapter = fastAdapter
        rvMenu!!.isFocusable = false
        rvMenu!!.setHasFixedSize(true)

        snapHelper = LinearSnapHelper()
        snapHelper!!.attachToRecyclerView(rvMenu)

        rvMenu!!.addItemDecoration(
            LineItemIndicatorDecoration(
                requireContext(),
                snapHelper!!
            )
        )
        rvMenu!!.addItemDecoration(OffsetItemDecoration())

        // add scroll listener
        rvMenu!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val foundView = snapHelper!!.findSnapView(layoutManager) ?: return
                currentMenuItemIndex = layoutManager.getPosition(foundView)
            }
        })

        // pass the root view's touch event to the recycler view
        root!!.setOnTouchListener { _, event ->
            if (lastMotionEventAction == MotionEvent.ACTION_DOWN && event.action == MotionEvent.ACTION_UP)
                rvMenu!!.getChildAt(currentMenuItemIndex).performClick()
            lastMotionEventAction = event.action
            rvMenu!!.onTouchEvent(event)
        }

        return root!!
    }

    override fun onDestroyView() {
        super.onDestroyView()
        itemAdapter = null
        fastAdapter = null
        binding = null
        root = null
        rvMenu = null
        bottomActionBar = null
    }

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent): Boolean = when (event.type) {
        SmartGlassTouchEvent.Type.SWIPE_FORWARD -> {
            if (event.source == SmartGlassTouchEvent.Source.KEY) {
                rvMenu!!.smoothScrollToNext(currentMenuItemIndex)
                true
            } else false
        }
        SmartGlassTouchEvent.Type.SWIPE_BACKWARD -> {
            if (event.source == SmartGlassTouchEvent.Source.KEY) {
                rvMenu!!.smoothScrollToPrevious(currentMenuItemIndex)
                true
            } else false
        }
        else -> super.onSmartGlassTouchEvent(event)
    }
}