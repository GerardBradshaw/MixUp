package com.gerardbradshaw.mixup.ui.moreapps

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

private const val LOG_TAG = "MoreAppsFragment"

class MoreAppsFragment : Fragment() {
  private lateinit var moreAppsViewModel: MoreAppsViewModel
  private lateinit var appInfoList: ArrayList<AppInfo>

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_more_apps, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    moreAppsViewModel = ViewModelProvider(this).get(MoreAppsViewModel::class.java)

    appInfoList = moreAppsViewModel.getAppList()

    initRecycler()
  }

  private fun initRecycler() {
    val adapter = AppListAdapter(requireView().context, appInfoList)

    requireView().findViewById<RecyclerView>(R.id.apps_recycler).also {
      it.adapter = adapter
      it.layoutManager = LinearLayoutManager(
        requireView().context,
        LinearLayoutManager.VERTICAL,
        false)
    }
  }
}