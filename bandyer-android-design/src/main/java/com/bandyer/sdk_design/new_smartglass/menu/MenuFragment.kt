package com.bandyer.sdk_design.new_smartglass

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.sdk_design.databinding.BandyerFragmentMenuBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

class MenuFragment : Fragment(), SmartGlassTouchEventListener, BottomBarHolder {

    private lateinit var binding: BandyerFragmentMenuBinding

    private val itemAdapter = ItemAdapter<MenuItem>()
    private val fastAdapter = FastAdapter.with(itemAdapter)

    private var currentMenuItemIndex = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BandyerFragmentMenuBinding.inflate(inflater, container, false)

        val root = binding.root
        val menu = binding.menu
        val layoutManager =
            CenterLinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        menu.layoutManager = layoutManager
        menu.adapter = fastAdapter
        menu.clipToPadding = false
        menu.isFocusable = true

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(menu)

        // TODO add decoration height through style
        menu.addItemDecoration(
            MenuItemIndicatorDecoration(
                requireContext(),
                snapHelper,
                Resources.getSystem().displayMetrics.density * 3
            )
        )

        menu.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val foundView = snapHelper.findSnapView(layoutManager) ?: return
                currentMenuItemIndex = layoutManager.getPosition(foundView)
            }
        })

        itemAdapter.add(MenuItem("Attiva microfono"))
        itemAdapter.add(MenuItem("Muta camera"))
        itemAdapter.add(MenuItem("Volume"))
        itemAdapter.add(MenuItem("Zoom"))

        root.setOnTouchListener { _, event -> menu.onTouchEvent(event) }
        return root
    }

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean = false

    override fun showBottomBar() {
        binding.bottomActionBar.visibility = View.VISIBLE
    }

    override fun hideBottomBar() {
        binding.bottomActionBar.visibility = View.GONE
    }
}