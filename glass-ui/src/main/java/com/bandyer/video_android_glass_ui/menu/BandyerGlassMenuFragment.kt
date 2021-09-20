package com.bandyer.video_android_glass_ui.menu

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.video_android_glass_ui.databinding.BandyerFragmentMenuBinding
import com.bandyer.video_android_glass_ui.BandyerGlassBaseFragment
import com.bandyer.video_android_glass_ui.bottom_action_bar.BandyerBottomActionBarView
import com.bandyer.video_android_glass_ui.common.item_decoration.BandyerCenterItemDecoration
import com.bandyer.video_android_glass_ui.common.item_decoration.LineItemIndicatorDecoration
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

/**
 * BandyerGlassMenuFragment. A base class for the menu fragment.
 */
abstract class BandyerGlassMenuFragment : BandyerGlassBaseFragment() {

    private var binding: BandyerFragmentMenuBinding? = null

    protected var itemAdapter: ItemAdapter<BandyerMenuItem>? = null
    protected var fastAdapter: FastAdapter<BandyerMenuItem>? = null

    protected var root: ViewGroup? = null
    protected var rvMenu: RecyclerView? = null
    protected var bottomActionBar: BandyerBottomActionBarView? = null

    protected var snapHelper: LinearSnapHelper? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BandyerFragmentMenuBinding.inflate(inflater, container, false)

        // set the views
        root = binding!!.root
        rvMenu = binding!!.bandyerMenu
        bottomActionBar = binding!!.bandyerBottomActionBar

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
        rvMenu!!.addItemDecoration(BandyerCenterItemDecoration())

        // pass the root view's touch event to the recycler view
        root!!.setOnTouchListener { _, event -> rvMenu!!.onTouchEvent(event) }

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
        snapHelper = null
    }
}