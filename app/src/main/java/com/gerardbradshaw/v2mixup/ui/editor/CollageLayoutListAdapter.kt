package com.gerardbradshaw.v2mixup.ui.editor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.gerardbradshaw.collageview.CollageViewFactory
import com.gerardbradshaw.v2mixup.R
import java.util.LinkedHashMap

class CollageLayoutListAdapter(
  context: Context,
  layouts: LinkedHashMap<Int, CollageViewFactory.CollageLayoutType>
) : RecyclerView.Adapter<CollageLayoutListAdapter.IconViewHolder>() {

  private val inflater = LayoutInflater.from(context)
  private val iconResIds = layouts.keys.toList()
  private var collageLayoutTypes = layouts.values.toList()
  private var listener: TypeClickedListener? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
    val itemView = inflater.inflate(R.layout.list_item_collage_layout, parent, false)
    return IconViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return iconResIds.size
  }

  override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
    holder.image.setImageResource(iconResIds[position])

    holder.itemView.setOnClickListener {
      if (listener != null) listener!!.onLayoutTypeClicked(collageLayoutTypes[position])
    }
  }

  class IconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val image: ImageView = itemView.findViewById(R.id.list_item_button)
  }

  interface TypeClickedListener {
    fun onLayoutTypeClicked(collageLayoutType: CollageViewFactory.CollageLayoutType)
  }

  fun setCollageTypeClickedListener(listener: TypeClickedListener) {
    this.listener = listener
  }
}