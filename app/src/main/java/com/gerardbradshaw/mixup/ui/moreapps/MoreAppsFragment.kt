package com.gerardbradshaw.mixup.ui.moreapps

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gerardbradshaw.mixup.BaseApplication
import com.gerardbradshaw.mixup.models.AppInfo
import com.gerardbradshaw.mixup.R
import javax.inject.Inject
import javax.inject.Named

private const val TAG = "MoreAppsFragment"

class MoreAppsFragment : Fragment() {

  @Inject @Named("mater_info") lateinit var materInfo: AppInfo
  private lateinit var viewModel: MoreAppsViewModel

  override fun onCreateView(inflater: LayoutInflater,
                            container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {

    (requireActivity().application as BaseApplication).getAppComponent()
      .moreAppsComponent().create().inject(this)

    return inflater.inflate(R.layout.fragment_more_apps, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    viewModel = ViewModelProvider(this).get(MoreAppsViewModel::class.java)

    initRecycler()
  }

  private fun initRecycler() {
    val adapter = AppListAdapter(requireView().context)

    viewModel.addAppToList(materInfo)

    viewModel.getAppList().observe(requireActivity(), Observer { adapter.setAppList(it) })

    requireView().findViewById<RecyclerView>(R.id.apps_recycler).also {
      it.adapter = adapter
      it.layoutManager = LinearLayoutManager(
        requireView().context,
        LinearLayoutManager.VERTICAL,
        false)
    }
  }
}