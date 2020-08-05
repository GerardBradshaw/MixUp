package com.gerardbradshaw.mixup.ui.moreapps

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gerardbradshaw.mixup.models.AppInfo
import com.gerardbradshaw.mixup.R
import java.lang.ClassCastException

private const val LOG_TAG = "MoreAppsFragment"
private const val DISPLAY_OPTIONS_MENU = "displayOptionsMenu"

class MoreAppsFragment : Fragment() {
  private lateinit var moreAppsViewModel: MoreAppsViewModel
  private lateinit var listener: OnFragmentCreatedListener
  private lateinit var rootView: View
  private lateinit var appInfoList: ArrayList<AppInfo>

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
    moreAppsViewModel = ViewModelProvider(this).get(MoreAppsViewModel::class.java)
    rootView = inflater.inflate(R.layout.fragment_more_apps, container, false)

    initData()
    initViews()

    return rootView
  }

  private fun initData() {
    appInfoList = moreAppsViewModel.getAppList()

  }

  private fun initViews() {
    val adapter = AppListAdapter(rootView.context, appInfoList)

    rootView.findViewById<RecyclerView>(R.id.apps_recycler).also {
      it.adapter = adapter
      it.layoutManager =
        LinearLayoutManager(rootView.context, LinearLayoutManager.VERTICAL, false)
    }
  }

  companion object {
    @JvmStatic
    fun newInstance(): MoreAppsFragment =
      MoreAppsFragment().apply {
        arguments = Bundle().apply {
          putBoolean(DISPLAY_OPTIONS_MENU, false)
        }
      }
  }

  interface OnFragmentCreatedListener {
    fun onFragmentChanged(shouldShowOptionsMenu: Boolean)
  }
}