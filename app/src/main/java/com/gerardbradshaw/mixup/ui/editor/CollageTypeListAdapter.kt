package com.gerardbradshaw.mixup.ui.editor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.gerardbradshaw.collageview.CollageViewFactory
import com.gerardbradshaw.mixup.R
import java.util.LinkedHashMap

class CollageTypeListAdapter(context: Context, frames: LinkedHashMap<Int, CollageViewFactory.CollageType>) :
  RecyclerView.Adapter<CollageTypeListAdapter.IconViewHolder>() {

  private val inflater = LayoutInflater.from(context)
  private val iconResIds = frames.keys.toList()
  private var collageTypes = frames.values.toList()
  private var listener: TypeClickedListener? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
    val itemView = inflater.inflate(R.layout.list_item_frame, parent, false)
    return IconViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return iconResIds.size
  }

  override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
    holder.image.setImageResource(iconResIds[position])

    holder.itemView.setOnClickListener {
      if (listener != null) listener!!.onCollageTypeClicked(collageTypes[position])
    }
  }

  class IconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val image: ImageView = itemView.findViewById(R.id.list_item_button)
  }

  interface TypeClickedListener {
    fun onCollageTypeClicked(collageType: CollageViewFactory.CollageType)
  }

  fun setCollageTypeClickedListener(listener: TypeClickedListener) {
    this.listener = listener
  }
}