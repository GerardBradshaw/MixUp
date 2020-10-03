package com.gerardbradshaw.mixup.ui.editor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gerardbradshaw.mixup.R
import java.util.LinkedHashMap

class AspectRatioListAdapter(context: Context, ratios: LinkedHashMap<String, Float>)
  : RecyclerView.Adapter<AspectRatioListAdapter.AspectRatioViewHolder>() {

  private val inflater = LayoutInflater.from(context)
  private val ratioStrings = ratios.keys.toList()
  private val ratioValues = ratios.values.toList()
  private var listener: AspectRatioButtonClickedListener? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AspectRatioViewHolder {
    val itemView = inflater.inflate(R.layout.list_item_ratio, parent, false)
    return AspectRatioViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return ratioStrings.size
  }

  override fun onBindViewHolder(holder: AspectRatioViewHolder, position: Int) {
    val string = ratioStrings[position]
    holder.textView.text = string

    holder.itemView.setOnClickListener {
      val ratioValue = ratioValues[position]
      if (listener != null) listener!!.onAspectRatioButtonClicked(ratioValue)
    }
  }

  fun setButtonClickedListener(listener: AspectRatioButtonClickedListener) {
    this.listener = listener
  }

  class AspectRatioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textView: TextView = itemView.findViewById(R.id.aspect_ratio_text_view)
  }

  interface AspectRatioButtonClickedListener {
    fun onAspectRatioButtonClicked(newRatio: Float)
  }
}