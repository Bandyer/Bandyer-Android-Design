package com.bandyer.sdk_design.filesharing

import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.IClickable
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.items.AbstractItem
import java.io.File

abstract class BandyerFileShareItem<I, VH>(var startTime: Long, val file: File): AbstractItem<I, VH>() where I: IItem<*, *>, I: IClickable<*>, VH: RecyclerView.ViewHolder
