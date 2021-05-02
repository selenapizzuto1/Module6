package com.raywenderlich.podplay.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.raywenderlich.podplay.model.Episode
import com.raywenderlich.podplay.model.Podcast
import com.raywenderlich.podplay.repository.PodcastRepo
import com.raywenderlich.podplay.viewmodel.SearchViewModel.PodcastSummaryViewData
import java.util.*

class PodcastViewModel(application: Application) : AndroidViewModel
(application) {

  var podcastRepo: PodcastRepo? = null
  var activePodcastViewData: PodcastViewData? = null

  fun getPodcast(podcastSummaryViewData: PodcastSummaryViewData, callback: (PodcastViewData?) ->
  Unit) {

    val repo = podcastRepo ?: return
    val feedUrl = podcastSummaryViewData.feedUrl ?: return

    repo.getPodcast(feedUrl, {
      it?.let {
        it.feedTitle = podcastSummaryViewData.name ?: ""
        it.imageUrl = podcastSummaryViewData.imageUrl ?: ""
        activePodcastViewData = podcastToPodcastView(it)
        callback(activePodcastViewData)
      }
    })
  }

  private fun podcastToPodcastView(podcast: Podcast): PodcastViewData {
    return PodcastViewData(
        false,
        podcast.feedTitle,
        podcast.feedUrl,
        podcast.feedDesc,
        podcast.imageUrl,
        episodesToEpisodesView(podcast.episodes)
    )
  }

  private fun episodesToEpisodesView(episodes: List<Episode>): List<EpisodeViewData> {
    return episodes.map {
      EpisodeViewData(it.guid, it.title, it.description, it.mediaUrl,
          it.releaseDate, it.duration)
    }
  }

  data class PodcastViewData(
      var subscribed: Boolean = false,
      var feedTitle: String? = "",
      var feedUrl: String? = "",
      var feedDesc: String? = "",
      var imageUrl: String? = "",
      var episodes: List<EpisodeViewData>
  )

  data class EpisodeViewData (
      var guid: String? = "",
      var title: String? = "",
      var description: String? = "",
      var mediaUrl: String? = "",
      var releaseDate: Date? = null,
      var duration: String? = ""
  )
}
