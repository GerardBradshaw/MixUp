package com.gerardbradshaw.mixup.ui.editor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.gerardbradshaw.mixup.R

class FrameListAdapter(context: Context, private val frames: HashMap<Int, Int>) :
  RecyclerView.Adapter<FrameListAdapter.ToolViewHolder>() {

  private val inflater = LayoutInflater.from(context)
  private val imageResIds = frames.keys.toList()
  private val layoutResIds = frames.values.toList()
  var listener: ToolButtonClickedListener? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolViewHolder {
    val itemView = inflater.inflate(R.layout.list_item_tool_options, parent, false)
    return ToolViewHolder(itemView, this)
  }

  override fun getItemCount(): Int {
    return imageResIds.size
  }

  override fun onBindViewHolder(holder: ToolViewHolder, position: Int) {
    val imageResId = imageResIds[position]
    holder.image.setImageResource(imageResId)

    holder.itemView.setOnClickListener {
      val layoutResId = layoutResIds[position]
      if (listener != null) listener!!.onToolButtonClicked(layoutResId)
    }
  }

  class ToolViewHolder(itemView: View, private var adapter: FrameListAdapter) :
    RecyclerView.ViewHolder(itemView) {

    val image: ImageView = itemView.findViewById(R.id.list_item_button)
  }

  interface ToolButtonClickedListener {
    fun onToolButtonClicked(resId: Int?)
  }

  fun setButtonClickedListener(listener: ToolButtonClickedListener) {
    this.listener = listener
  }
}