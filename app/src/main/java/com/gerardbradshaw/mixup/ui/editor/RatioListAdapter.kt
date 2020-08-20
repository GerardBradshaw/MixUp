package com.gerardbradshaw.mixup.ui.editor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gerardbradshaw.mixup.R
import java.util.LinkedHashMap

class RatioListAdapter(context: Context, ratios: LinkedHashMap<String, Float>)
  : RecyclerView.Adapter<RatioListAdapter.RatioViewHolder>() {

  private val inflater = LayoutInflater.from(context)
  private val ratioStrings = ratios.keys.toList()
  private val ratioValues = ratios.values.toList()
  var listener: RatioButtonClickedListener? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatioViewHolder {
    val itemView = inflater.inflate(R.layout.list_item_ratio, parent, false)
    return RatioViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return ratioStrings.size
  }

  override fun onBindViewHolder(holder: RatioViewHolder, position: Int) {
    val string = ratioStrings[position]
    holder.textView.text = string

    holder.itemView.setOnClickListener {
      val ratioValue = ratioValues[position]
      if (listener != null) listener!!.onRatioButtonClicked(ratioValue)
    }
  }

  fun setButtonClickedListener(listener: RatioButtonClickedListener) {
    this.listener = listener
  }

  class RatioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textView = itemView.findViewById<TextView>(R.id.ratio_text_view)
  }

  interface RatioButtonClickedListener {
    fun onRatioButtonClicked(ratio: Float)
  }
}