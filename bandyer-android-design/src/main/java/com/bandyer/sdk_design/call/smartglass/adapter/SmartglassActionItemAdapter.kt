package com.bandyer.sdk_design.call.smartglass.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.sdk_design.bottom_sheet.items.ActionItem

/**
 * Adapter class for the action items swipeable smartglass menu
 * @suppress
 */
class SmartglassActionItemAdapter(private val onActionItemClickedListener: OnActionItemClickedListener? = null): RecyclerView.Adapter<SmartglassActionItemAdapter.ActionItemViewHolder>() {

    private var actionItems: List<ActionItem>? = null

    fun setItems(actionItems: List<ActionItem>) {
        this.actionItems = actionItems
        notifyDataSetChanged()
    }

    /**
     * The smartglass action item view holder
     * @suppress
     * @constructor
     */
    class ActionItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionItemViewHolder {
        return ActionItemViewHolder(FrameLayout(parent.context).apply {
            this.layoutParams = ViewGroup.LayoutParams(parent.width, parent.height)
        })
    }

    override fun onBindViewHolder(holder: ActionItemViewHolder, position: Int) {
        val actionItem = actionItems?.get(position) ?: return
        if (holder.itemView !is FrameLayout) return
        holder.itemView.removeAllViews()
        val customView = LayoutInflater.from(ContextThemeWrapper(holder.itemView.context, actionItem.viewStyle)).inflate(actionItem.viewLayoutRes, holder.itemView, false).apply {
            id = actionItem.viewId
        }
        actionItem.itemView = customView
        actionItem.onReady()
        holder.itemView.addView(customView)
        holder.itemView.setOnClickListener { onActionItemClickedListener?.onActionItemClicked(actionItem) }
    }

    override fun getItemId(position: Int): Long = actionItems?.get(position)?.itemView?.toString()?.hashCode()?.toLong() ?: position.toLong()

    override fun getItemViewType(position: Int): Int = actionItems!![position].viewLayoutRes

    override fun getItemCount(): Int = actionItems?.size ?: 0

    /**
     * OnActionItemClickListener
     * @suppress
     */
    interface OnActionItemClickedListener {
        /**
         * Called when an action item has been clicked
         * @param actionItem ActionItem the clicked action item
         */
        fun onActionItemClicked(actionItem: ActionItem)
    }
}