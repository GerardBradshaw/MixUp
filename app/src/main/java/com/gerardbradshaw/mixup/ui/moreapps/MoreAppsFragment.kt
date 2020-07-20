package com.gerardbradshaw.mixup.ui.moreapps

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gerardbradshaw.mixup.R
import java.lang.ClassCastException

// TODO: Rename
private const val DISPLAY_OPTIONS_MENU = "displayOptionsMenu"
private const val ARG_PARAM2 = "param2"

class MoreAppsFragment : Fragment() {
  // TODO: Rename and change types
  private var param1: String? = null
  private var param2: String? = null
  private lateinit var listener: OnFragmentCreatedListener

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    arguments?.let {
      param2 = it.getString(ARG_PARAM2)
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context is OnFragmentCreatedListener) listener = context
    else throw ClassCastException(context.toString() + "must implement OnFragmentCreatedListener")

    listener.onFragmentChanged(false)
  }

  override fun onDetach() {
    super.onDetach()
    listener.onFragmentChanged(true)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    val rootView = inflater.inflate(R.layout.fragment_more_apps, container, false)
    return rootView
  }

  companion object {
    @JvmStatic
    fun newInstance(param1: String, param2: String): MoreAppsFragment =
      MoreAppsFragment().apply {
        arguments = Bundle().apply {
          putBoolean(DISPLAY_OPTIONS_MENU, false)
        }
      }
  }

  interface OnFragmentCreatedListener {
    fun onFragmentChanged(isOptionsMenuVisible: Boolean)
  }
}