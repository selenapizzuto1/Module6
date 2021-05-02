package com.raywenderlich.podplay.ui

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.text.method.ScrollingMovementMethod
import android.view.*
import com.bumptech.glide.Glide
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.adapter.EpisodeListAdapter
import com.raywenderlich.podplay.viewmodel.PodcastViewModel
import kotlinx.android.synthetic.main.fragment_podcast_details.*

class PodcastDetailsFragment : Fragment() {

  private lateinit var podcastViewModel: PodcastViewModel
  private lateinit var episodeListAdapter: EpisodeListAdapter

  companion object {
    fun newInstance(): PodcastDetailsFragment {
      return PodcastDetailsFragment()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
    setupViewModel()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_podcast_details, container, false)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    setupControls()
    updateControls()
  }

  override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
    super.onCreateOptionsMenu(menu, inflater)
    inflater?.inflate(R.menu.menu_details, menu)
  }

  private fun setupControls() {

    feedDescTextView.movementMethod = ScrollingMovementMethod()

    episodeRecyclerView.setHasFixedSize(true)

    val layoutManager = LinearLayoutManager(activity)
    episodeRecyclerView.layoutManager = layoutManager

    val dividerItemDecoration = android.support.v7.widget.DividerItemDecoration(episodeRecyclerView.context,
        layoutManager.orientation)
    episodeRecyclerView.addItemDecoration(dividerItemDecoration)

    episodeListAdapter = EpisodeListAdapter(podcastViewModel.activePodcastViewData?.episodes)
    episodeRecyclerView.adapter = episodeListAdapter
  }

  private fun updateControls() {
    val viewData = podcastViewModel.activePodcastViewData ?: return
    feedTitleTextView.text = viewData.feedTitle
    feedDescTextView.text = viewData.feedDesc
    activity?.let { activity ->
      Glide.with(activity).load(viewData.imageUrl).into(feedImageView)
    }
  }

  private fun setupViewModel() {
    activity?.let { activity ->
      podcastViewModel = ViewModelProviders.of(activity).get(PodcastViewModel::class.java)
    }
  }
}
