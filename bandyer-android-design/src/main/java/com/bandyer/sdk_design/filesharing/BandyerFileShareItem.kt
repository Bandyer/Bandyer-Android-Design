package com.bandyer.sdk_design.filesharing

import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.IClickable
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.items.AbstractItem

abstract class BandyerFileShareItem<I, VH>(var startTime: Long): AbstractItem<I, VH>() where I: IItem<*, *>, I: IClickable<*>, VH: RecyclerView.ViewHolder
