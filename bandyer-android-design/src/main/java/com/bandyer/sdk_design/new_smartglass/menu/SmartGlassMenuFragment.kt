package com.bandyer.sdk_design.new_smartglass.menu

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.*
import androidx.recyclerview.widget.*
import com.bandyer.sdk_design.databinding.BandyerFragmentMenuBinding
import com.bandyer.sdk_design.new_smartglass.SmartGlassBaseFragment
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent
import com.bandyer.sdk_design.new_smartglass.bottom_action_bar.BottomActionBarView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import kotlin.math.abs

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
        rvMenu!!.clipToPadding = false
        rvMenu!!.isFocusable = true

        snapHelper = LinearSnapHelper()
        snapHelper!!.attachToRecyclerView(rvMenu)

        rvMenu!!.addItemDecoration(
            ItemIndicatorDecoration(
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
            Log.e("touchEvent", event.toString())
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

    fun RecyclerView.swipeToNext() {
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        val startX = screenWidth / 2f
        val startY = screenHeight / 2f

        val layoutManager = (layoutManager!! as LinearLayoutManager)
        val currentPos = layoutManager.findFirstCompletelyVisibleItemPosition()
        if (currentPos == adapter!!.itemCount - 1) return
        val nextView = findViewHolderForAdapterPosition(currentPos + 1)!!.itemView
        val targetX = nextView.left + (nextView.right - nextView.left) / 2f

        val downTime = SystemClock.uptimeMillis()
        var eventTime = SystemClock.uptimeMillis()

        root!!.dispatchTouchEvent(
            MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_DOWN,
                startX,
                startY,
                0
            )
        )

        val steps = 20
        val stepX = abs(targetX - startX) / steps
        var deltaX = 0f
        for (i in 0..steps) {
            deltaX = startX - i * stepX
            eventTime += 35
            root!!.dispatchTouchEvent(
                MotionEvent.obtain(
                    downTime,
                    eventTime,
                    MotionEvent.ACTION_MOVE,
                    deltaX,
                    startY,
                    0
                )
            )
        }

        root!!.dispatchTouchEvent(
            MotionEvent.obtain(
                downTime,
                ++eventTime,
                MotionEvent.ACTION_UP,
                deltaX,
                startY,
                0
            )
        )
    }

    fun RecyclerView.swipeToPrevious() {
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        val startX = screenWidth / 2f
        val startY = screenHeight / 2f

        val layoutManager = (layoutManager!! as LinearLayoutManager)
        val currentPos = layoutManager.findFirstCompletelyVisibleItemPosition()
        if (currentPos == 0) return
        val nextView = findViewHolderForAdapterPosition(currentPos - 1)!!.itemView
        val targetX = nextView.left + (nextView.right - nextView.left) / 2f

        val downTime = SystemClock.uptimeMillis()
        var eventTime = SystemClock.uptimeMillis()

        root!!.dispatchTouchEvent(
            MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_DOWN,
                startX,
                startY,
                0
            )
        )

        val steps = 20
        val stepX = abs(targetX - startX) / steps
        var deltaX = 0f
        for (i in 0..steps) {
            deltaX = startX + i * stepX
            eventTime += 35
            root!!.dispatchTouchEvent(
                MotionEvent.obtain(
                    downTime,
                    eventTime,
                    MotionEvent.ACTION_MOVE,
                    deltaX,
                    startY,
                    0
                )
            )
        }

        root!!.dispatchTouchEvent(
            MotionEvent.obtain(
                downTime,
                ++eventTime,
                MotionEvent.ACTION_UP,
                deltaX,
                startY,
                0
            )
        )
    }

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean = when (event) {
        SmartGlassTouchEvent.Event.SWIPE_FORWARD -> {
            rvMenu!!.swipeToNext()
            true
        }
        SmartGlassTouchEvent.Event.SWIPE_BACKWARD -> {
            rvMenu!!.swipeToPrevious()
            true
        }
        else -> false
    }
}