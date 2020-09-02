package com.gerardbradshaw.mixup.ui.editor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gerardbradshaw.mixup.R
import com.gerardbradshaw.mixup.utils.ColorPickerUtil

class ColorAdapter(context: Context, private val colorCount: Int) :
  RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

  private val inflater = LayoutInflater.from(context)
  private val colorPicker = ColorPickerUtil(colorCount)
  private var listener: ColorClickedListener? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
    val itemView = inflater.inflate(R.layout.color_list_item, parent, false)
    return ColorViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return colorCount
  }

  override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
    val color = colorPicker.getIthColor(position)
    holder.itemView.setBackgroundColor(color)

    holder.itemView.setOnClickListener {
      if (listener != null) listener!!.onColorClicked(color)
    }
  }

  fun setColorClickedListener(listener: ColorClickedListener) {
    this.listener = listener
  }

  class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

  interface ColorClickedListener {
    fun onColorClicked(color: Int)
  }
}