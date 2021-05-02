package com.raywenderlich.podplay.ui

import android.app.SearchManager
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.adapter.PodcastListAdapter
import com.raywenderlich.podplay.adapter.PodcastListAdapter.PodcastListAdapterListener
import com.raywenderlich.podplay.repository.ItunesRepo
import com.raywenderlich.podplay.repository.PodcastRepo
import com.raywenderlich.podplay.service.FeedService
import com.raywenderlich.podplay.service.ItunesService
import com.raywenderlich.podplay.viewmodel.PodcastViewModel
import com.raywenderlich.podplay.viewmodel.SearchViewModel
import kotlinx.android.synthetic.main.activity_podcast.*

class PodcastActivity : AppCompatActivity(), PodcastListAdapterListener {

  private lateinit var searchViewModel: SearchViewModel
  private lateinit var podcastListAdapter: PodcastListAdapter
  private lateinit var podcastViewModel: PodcastViewModel
  private lateinit var searchMenuItem: MenuItem

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_podcast)
    setupToolbar()
    setupViewModels()
    updateControls()
    handleIntent(intent)
    addBackStackListener()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    val inflater = menuInflater
    inflater.inflate(R.menu.menu_search, menu)

    searchMenuItem = menu.findItem(R.id.search_item)
    val searchView = searchMenuItem.actionView as SearchView

    val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
    searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

    if (supportFragmentManager.backStackEntryCount > 0) {
      podcastRecyclerView.visibility = View.INVISIBLE
    }

    if (podcastRecyclerView.visibility == View.INVISIBLE) {
      searchMenuItem.isVisible = false
    }

    return true
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleIntent(intent)
  }


  override fun onShowDetails(podcastSummaryViewData: SearchViewModel.PodcastSummaryViewData) {

    val feedUrl = podcastSummaryViewData.feedUrl ?: return

    showProgressBar()

    podcastViewModel.getPodcast(podcastSummaryViewData, {

      hideProgressBar()

      if (it != null) {
        showDetailsFragment()
      } else {
        showError("Error loading feed $feedUrl")
      }
    })
  }

  private fun performSearch(term: String) {
    showProgressBar()
    searchViewModel.searchPodcasts(term, { results ->
      hideProgressBar()
      toolbar.title = getString(R.string.search_results)
      podcastListAdapter.setSearchData(results)
    })
  }
  
  private fun handleIntent(intent: Intent) {
    if (Intent.ACTION_SEARCH == intent.action) {
      val query = intent.getStringExtra(SearchManager.QUERY)
      performSearch(query)
    }
  }
  

  private fun setupToolbar() {
    setSupportActionBar(toolbar)
  }

  private fun setupViewModels() {
    val service = ItunesService.instance
    searchViewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
    searchViewModel.iTunesRepo = ItunesRepo(service)
    podcastViewModel = ViewModelProviders.of(this).get(PodcastViewModel::class.java)
    val rssService = FeedService.instance
    podcastViewModel.podcastRepo = PodcastRepo(rssService)
  }

  private fun addBackStackListener()
  {
    supportFragmentManager.addOnBackStackChangedListener {
      if (supportFragmentManager.backStackEntryCount == 0) {
        podcastRecyclerView.visibility = View.VISIBLE
      }
    }
  }

  private fun updateControls() {
    podcastRecyclerView.setHasFixedSize(true)

    val layoutManager = LinearLayoutManager(this)
    podcastRecyclerView.layoutManager = layoutManager

    val dividerItemDecoration = android.support.v7.widget.DividerItemDecoration(
        podcastRecyclerView.context, layoutManager.orientation)
    podcastRecyclerView.addItemDecoration(dividerItemDecoration)

    podcastListAdapter = PodcastListAdapter(null, this, this)
    podcastRecyclerView.adapter = podcastListAdapter
  }


  private fun showDetailsFragment() {
    val podcastDetailsFragment = createPodcastDetailsFragment()

    supportFragmentManager.beginTransaction().add(R.id.podcastDetailsContainer,
        podcastDetailsFragment, TAG_DETAILS_FRAGMENT).addToBackStack("DetailsFragment").commit()
    podcastRecyclerView.visibility = View.INVISIBLE
    searchMenuItem.isVisible = false
  }

  private fun createPodcastDetailsFragment(): PodcastDetailsFragment {
    var podcastDetailsFragment = supportFragmentManager.findFragmentByTag(TAG_DETAILS_FRAGMENT) as
        PodcastDetailsFragment?

    if (podcastDetailsFragment == null) {
      podcastDetailsFragment = PodcastDetailsFragment.newInstance()
    }

    return podcastDetailsFragment
  }

  private fun showProgressBar() {
    progressBar.visibility = View.VISIBLE
  }
  
  private fun hideProgressBar() {
    progressBar.visibility = View.INVISIBLE
  }

  private fun showError(message: String) {
    AlertDialog.Builder(this)
        .setMessage(message)
        .setPositiveButton(getString(R.string.ok_button), null)
        .create()
        .show()
  }

  companion object {
    private val TAG_DETAILS_FRAGMENT = "DetailsFragment"
  }
}
