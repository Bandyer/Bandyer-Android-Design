package com.bandyer.sdk_design.new_smartglass

import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.sdk_design.databinding.BandyerFragmentMenuBinding
import com.bandyer.sdk_design.extensions.setPaddingEnd
import com.bandyer.sdk_design.extensions.setPaddingStart
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import kotlin.math.roundToInt

class MenuFragment : Fragment(), SmartGlassTouchEventListener, BottomBarHolder {

    private lateinit var binding: BandyerFragmentMenuBinding

    private val itemAdapter = ItemAdapter<MenuItem>()
    private val fastAdapter = FastAdapter.with(itemAdapter)

    private var currentMenuItemIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BandyerFragmentMenuBinding.inflate(inflater, container, false)

        val menu = binding.menu
        val layoutManager = CenterLinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        menu.layoutManager = layoutManager
        menu.adapter = fastAdapter
        menu.clipToPadding = false
        menu.isFocusable = true

        // TODO add decoration height through style
//        menu.addItemDecoration(
//            MenuItemIndicatorDecoration(
//                requireContext(),
//                Resources.getSystem().displayMetrics.density * 4
//            )
//        )

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(menu)

        menu.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val foundView = snapHelper.findSnapView(layoutManager) ?: return
                currentMenuItemIndex = layoutManager.getPosition(foundView)
            }
        })

        itemAdapter.add(MenuItem("Muta"))
        itemAdapter.add(MenuItem("Attiva microfono"))
        itemAdapter.add(MenuItem("Volume"))

        return binding.root
    }

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean = false

    override fun showBottomBar() {
        binding.bottomActionBar.visibility = View.VISIBLE
    }

    override fun hideBottomBar() {
        binding.bottomActionBar.visibility = View.GONE
    }
}