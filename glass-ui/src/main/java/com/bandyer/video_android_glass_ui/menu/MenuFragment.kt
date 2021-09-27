package com.bandyer.video_android_glass_ui.menu

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.bottom_navigation.BottomNavigationView
import com.bandyer.video_android_glass_ui.common.item_decoration.HorizontalCenterItemDecoration
import com.bandyer.video_android_glass_ui.common.item_decoration.MenuProgressIndicator
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentMenuBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

/**
 * BandyerGlassMenuFragment. A base class for the menu fragment.
 */
abstract class MenuFragment : BaseFragment() {

    private var binding: BandyerGlassFragmentMenuBinding? = null

    protected var itemAdapter: ItemAdapter<BandyerMenuItem>? = null
    protected var fastAdapter: FastAdapter<BandyerMenuItem>? = null

    protected var root: ViewGroup? = null
    protected var rvMenu: RecyclerView? = null
    protected var bottomNavigation: BottomNavigationView? = null

    protected var snapHelper: LinearSnapHelper? = null

    /**
     * @suppress
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BandyerGlassFragmentMenuBinding.inflate(inflater, container, false)

        // set the views
        root = binding!!.root
        rvMenu = binding!!.bandyerMenu
        bottomNavigation = binding!!.bandyerBottomActionBar

        // init the recycler view
        itemAdapter = ItemAdapter()
        fastAdapter = FastAdapter.with(itemAdapter!!)
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvMenu!!.apply {
            this.layoutManager = layoutManager
            this.adapter = fastAdapter
            this.isFocusable = false
            this.setHasFixedSize(true)
            this.addItemDecoration(HorizontalCenterItemDecoration())
        }

        snapHelper = LinearSnapHelper().apply {
            this.attachToRecyclerView(rvMenu)
        }

        rvMenu!!.addItemDecoration(
            MenuProgressIndicator(
                requireContext(),
                snapHelper!!
            )
        )

        // pass the root view's touch event to the recycler view
        root!!.setOnTouchListener { _, event -> rvMenu!!.onTouchEvent(event) }

        return root!!
    }

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        itemAdapter = null
        fastAdapter = null
        binding = null
        root = null
        rvMenu = null
        bottomNavigation = null
        snapHelper = null
    }
}