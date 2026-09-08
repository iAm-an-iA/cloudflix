package com.lagradost.cloudstream3.ui.result

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.lagradost.cloudstream3.Actor
import com.lagradost.cloudstream3.ActorData
import com.lagradost.cloudstream3.CommonActivity
import com.lagradost.cloudstream3.DubStatus
import com.lagradost.cloudstream3.ShowStatus
import com.lagradost.cloudstream3.MainActivity.Companion.afterPluginsLoadedEvent
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.databinding.FragmentResultTvBinding
import com.lagradost.cloudstream3.mvvm.Resource
import com.lagradost.cloudstream3.mvvm.observe
import com.lagradost.cloudstream3.mvvm.observeNullable
import com.lagradost.cloudstream3.ui.BaseFragment
import com.lagradost.cloudstream3.ui.WatchType
import com.lagradost.cloudstream3.ui.player.ExtractorLinkGenerator
import com.lagradost.cloudstream3.ui.player.GeneratorPlayer
import com.lagradost.cloudstream3.ui.quicksearch.QuickSearchFragment
import com.lagradost.cloudstream3.ui.result.ResultFragment.getStoredData
import com.lagradost.cloudstream3.ui.result.ResultFragment.updateUIEvent
import com.lagradost.cloudstream3.ui.result.compose.MovieDetailsComposeScreen
import com.lagradost.cloudstream3.ui.result.compose.model.MovieDetailsAction
import com.lagradost.cloudstream3.ui.result.compose.model.MovieDetailsUiState
import com.lagradost.cloudstream3.ui.result.compose.model.MovieTrailerData
import androidx.compose.runtime.getValue
import com.lagradost.cloudstream4.rememberAppSettings
import com.lagradost.cloudstream4.theme.CloudStreamTheme
import com.lagradost.cloudstream4.theme.perfToColor
import com.lagradost.cloudstream4.theme.perfToMode
import com.mihon.presentation.settings.collectAsState
import kotlinx.collections.immutable.toPersistentList
import com.lagradost.cloudstream3.ui.search.SEARCH_ACTION_LOAD
import com.lagradost.cloudstream3.ui.search.SearchClickCallback
import com.lagradost.cloudstream3.ui.search.SearchHelper
import com.lagradost.cloudstream3.utils.AppContextUtils.loadCache
import com.lagradost.cloudstream3.utils.BackPressedCallbackHelper.detachBackPressedCallback
import com.lagradost.cloudstream3.utils.SingleSelectionHelper.showBottomDialog
import com.lagradost.cloudstream3.utils.SingleSelectionHelper.showBottomDialogInstant
import com.lagradost.cloudstream3.utils.UIHelper.dismissSafe
import com.lagradost.cloudstream3.utils.UIHelper.fixSystemBarsPadding
import com.lagradost.cloudstream3.utils.UIHelper.hideKeyboard
import com.lagradost.cloudstream3.utils.UIHelper.navigate
import com.lagradost.cloudstream3.utils.UIHelper.popCurrentPage
import com.lagradost.cloudstream3.utils.UIHelper.setNavigationBarColorCompat
import com.lagradost.cloudstream3.utils.UiText
import com.lagradost.cloudstream3.utils.txt

class ResultFragmentTv : BaseFragment<FragmentResultTvBinding>(
    BindingCreator.Inflate(FragmentResultTvBinding::inflate)
) {

    private lateinit var viewModel: ResultViewModel2

    private var uiState by mutableStateOf(MovieDetailsUiState())
    private var rawSeasons by mutableStateOf<List<Pair<UiText?, Int>>>(emptyList())
    private var dubSubSelections by mutableStateOf<List<Pair<UiText?, DubStatus>>>(emptyList())
    private var rangeSelections by mutableStateOf<List<Pair<UiText?, EpisodeRange>>>(emptyList())

    override fun onDestroyView() {
        updateUIEvent -= ::updateUI
        activity?.detachBackPressedCallback(this@ResultFragmentTv.toString())
        super.onDestroyView()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel =
            ViewModelProvider(this)[ResultViewModel2::class.java]
        viewModel.EPISODE_RANGE_SIZE = 50
        updateUIEvent += ::updateUI

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun updateUI(id: Int?) {
        viewModel.reloadEpisodes()
    }

    private var loadingDialog: Dialog? = null
    private var popupDialog: Dialog? = null

    private fun reloadViewModel(forceReload: Boolean) {
        if (!viewModel.hasLoaded() || forceReload) {
            val storedData = getStoredData() ?: return
            viewModel.load(
                activity,
                storedData.url,
                storedData.apiName,
                storedData.showFillers,
                storedData.dubStatus,
                storedData.start
            )
        }
    }

    override fun onResume() {
        activity?.setNavigationBarColorCompat(R.attr.primaryBlackBackground)
        afterPluginsLoadedEvent += ::reloadViewModel
        super.onResume()
    }

    override fun onStop() {
        afterPluginsLoadedEvent -= ::reloadViewModel
        super.onStop()
    }

    override fun fixLayout(view: View) {
        fixSystemBarsPadding(view, padTop = false)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindingCreated(binding: FragmentResultTvBinding) {
        // ===== setup =====
        val storedData = getStoredData() ?: return
        activity?.window?.decorView?.clearFocus()
        activity?.loadCache()
        hideKeyboard()
        if (storedData.restart || !viewModel.hasLoaded()) {
            viewModel.load(
                activity,
                storedData.url,
                storedData.apiName,
                storedData.showFillers,
                storedData.dubStatus,
                storedData.start
            )
        }
        // ===== ===== =====

        setupComposeView(binding, storedData)
        setupViewModelObservers(binding, storedData)
    }

    private fun handlePlayClick(storedData: ResultFragment.StoredData) {
        val resume = uiState.resumeStatus
        if (resume != null) {
            viewModel.handleAction(
                EpisodeClickEvent(
                    storedData.playerAction,
                    resume.result
                )
            )
        } else {
            val ep = uiState.episodes.firstOrNull()
            if (ep != null) {
                viewModel.handleAction(
                    EpisodeClickEvent(
                        storedData.playerAction,
                        ep
                    )
                )
            } else {
                (viewModel.movie.value as? Resource.Success)?.value?.let { (_, movieEp) ->
                    viewModel.handleAction(
                        EpisodeClickEvent(ACTION_CLICK_DEFAULT, movieEp)
                    )
                }
            }
        }
    }

    private fun handlePlayLongClick() {
        val resume = uiState.resumeStatus
        val ep = resume?.result ?: uiState.episodes.firstOrNull() ?: (viewModel.movie.value as? Resource.Success)?.value?.second
        if (ep != null) {
            viewModel.handleAction(
                EpisodeClickEvent(
                    ACTION_SHOW_OPTIONS,
                    ep
                )
            )
        }
    }

    private fun handleTrailerClick() {
        val trailersLinks = viewModel.trailers.value ?: emptyList()
        val extractedTrailerLinks = trailersLinks.flatMap { it.mirros }
            .map { (extractedTrailerLink, _) -> extractedTrailerLink }
        if (extractedTrailerLinks.isNotEmpty()) {
            activity.navigate(
                R.id.global_to_navigation_player,
                GeneratorPlayer.newInstance(
                    ExtractorLinkGenerator(
                        extractedTrailerLinks,
                        emptyList()
                    ),
                    0
                )
            )
        }
    }

    private fun handleAddToListClick() {
        val curStatus = viewModel.watchStatus.value ?: WatchType.NONE
        activity?.showBottomDialog(
            WatchType.entries.map { getString(it.stringRes) }.toList(),
            curStatus.ordinal,
            getString(R.string.action_add_to_bookmarks),
            showApply = false,
            {}
        ) {
            viewModel.updateWatchStatus(WatchType.entries[it], context)
        }
    }

    private fun handleLikeClick() {
        viewModel.toggleFavoriteStatus(context) { newStatus: Boolean? ->
            if (newStatus == null) return@toggleFavoriteStatus
            val message = if (newStatus) R.string.favorite_added else R.string.favorite_removed
            val name = (viewModel.page.value as? Resource.Success)?.value?.title
                ?: txt(R.string.no_data).asStringNull(context) ?: ""
            CommonActivity.showToast(txt(message, name), Toast.LENGTH_SHORT)
        }
    }

    private fun setupComposeView(binding: FragmentResultTvBinding, storedData: ResultFragment.StoredData) {
        binding.resultComposeView.setContent {
            val settings = rememberAppSettings()
            val mode by settings.ui.theme.collectAsState()
            val primaryColor by settings.ui.primaryColor.collectAsState()
            CloudStreamTheme(
                mode = perfToMode(mode),
                primaryColor = perfToColor(primaryColor),
            ) {
                MovieDetailsComposeScreen(
                    state = uiState,
                    onAction = { action ->
                        when (action) {
                            is MovieDetailsAction.SelectSeason -> {
                                val seasonNumber = rawSeasons.getOrNull(action.index)?.second ?: (action.index + 1)
                                viewModel.changeSeason(seasonNumber)
                            }
                            is MovieDetailsAction.SelectDub -> {
                                dubSubSelections.getOrNull(action.index)?.second?.let { dub ->
                                    viewModel.changeDubStatus(dub)
                                }
                            }
                            is MovieDetailsAction.SelectRange -> {
                                rangeSelections.getOrNull(action.index)?.second?.let { range ->
                                    viewModel.changeRange(range)
                                }
                            }
                            is MovieDetailsAction.ClickEpisode -> {
                                viewModel.handleAction(
                                    EpisodeClickEvent(
                                        storedData.playerAction,
                                        action.episode
                                    )
                                )
                            }
                            is MovieDetailsAction.LongClickEpisode -> {
                                viewModel.handleAction(
                                    EpisodeClickEvent(
                                        ACTION_SHOW_OPTIONS,
                                        action.episode
                                    )
                                )
                            }
                            is MovieDetailsAction.PlayPrimary -> {
                                handlePlayClick(storedData)
                            }
                            is MovieDetailsAction.PlayPrimaryLong -> {
                                handlePlayLongClick()
                            }
                            is MovieDetailsAction.ClickTrailer -> {
                                handleTrailerClick()
                            }
                            is MovieDetailsAction.ToggleBookmark -> {
                                handleAddToListClick()
                            }
                            is MovieDetailsAction.ToggleFavorite -> {
                                handleLikeClick()
                            }
                            is MovieDetailsAction.ClickSearch -> {
                                QuickSearchFragment.pushSearch(activity, uiState.title)
                            }
                            is MovieDetailsAction.ClickRecommendation -> {
                                SearchHelper.handleSearchClickCallback(
                                    SearchClickCallback(
                                        SEARCH_ACTION_LOAD,
                                        binding.root,
                                        0,
                                        action.response
                                    )
                                )
                            }
                            is MovieDetailsAction.ClickActor -> {
                                QuickSearchFragment.pushSearch(activity, action.actor.actor.name)
                            }
                        }
                    }
                )
            }
        }
    }

    private fun setupViewModelObservers(binding: FragmentResultTvBinding, storedData: ResultFragment.StoredData) {
        observeNullable(viewModel.resumeWatching) { resume ->
            uiState = uiState.copy(
                resumeStatus = resume,
                isMovie = uiState.episodes.isEmpty() || resume?.isMovie == true
            )
        }

        observe(viewModel.trailers) { trailersLinks ->
            val trailerItems = trailersLinks.flatMap { it.mirros }.mapNotNull { (extractedTrailerLink, _) ->
                MovieTrailerData(
                    title = extractedTrailerLink.name.ifBlank { getString(R.string.play_trailer) },
                    runtime = "",
                    rawTrailer = extractedTrailerLink
                )
            }.toPersistentList()
            uiState = uiState.copy(trailers = trailerItems)
        }

        observeNullable(viewModel.favoriteStatus) { isFav ->
            uiState = uiState.copy(isFavorite = isFav == true)
        }

        observe(viewModel.watchStatus) { watchType ->
            uiState = uiState.copy(isInWatchList = watchType != WatchType.NONE)
        }

        observePopupsAndLoading()
        observeSelectionsAndContent(binding, storedData)
    }

    private fun observePopupsAndLoading() {
        observeNullable(viewModel.selectPopup) { popup ->
            if (popup == null) {
                popupDialog?.dismissSafe(activity)
                popupDialog = null
                return@observeNullable
            }

            popupDialog?.dismissSafe(activity)
            popupDialog = activity?.let { act ->
                val options = popup.getOptions(act)
                val title = popup.getTitle(act)

                act.showBottomDialogInstant(
                    options, title, {
                        popupDialog = null
                        popup.callback(null)
                    }, {
                        popupDialog = null
                        popup.callback(it)
                    }
                )
            }
        }

        observeNullable(viewModel.loadedLinks) { load ->
            if (load == null) {
                loadingDialog?.dismissSafe(activity)
                loadingDialog = null
                return@observeNullable
            }
            if (loadingDialog?.isShowing != true) {
                loadingDialog?.dismissSafe(activity)
                loadingDialog = null
            }
            loadingDialog = loadingDialog ?: context?.let { ctx ->
                val builder = BottomSheetDialog(ctx)
                builder.setContentView(R.layout.bottom_loading)
                builder.setOnDismissListener {
                    loadingDialog = null
                    viewModel.cancelLinks()
                }
                builder.setCanceledOnTouchOutside(true)
                builder.show()
                builder
            }
            loadingDialog?.findViewById<MaterialButton>(R.id.overlay_loading_skip_button)?.apply {
                if (load.linksLoaded <= 0) {
                    isInvisible = true
                } else {
                    setOnClickListener {
                        viewModel.skipLoading()
                    }
                    isVisible = true
                    text = "${context.getString(R.string.skip_loading)} (${load.linksLoaded})"
                }
            }
        }
    }

    private fun observeSelectionsAndContent(binding: FragmentResultTvBinding, storedData: ResultFragment.StoredData) {
        observe(viewModel.selectedSeasonIndex) { selected ->
            uiState = uiState.copy(selectedSeasonIndex = selected)
        }

        observe(viewModel.seasonSelections) {
            rawSeasons = it
            uiState = uiState.copy(seasons = it.map { s -> s.first?.asStringNull(context) ?: "" }.toPersistentList())
        }

        observe(viewModel.dubSubSelections) {
            dubSubSelections = it
            uiState = uiState.copy(dubs = it.map { d -> d.first?.asStringNull(context) ?: "" }.toPersistentList())
        }

        observe(viewModel.selectedDubStatusIndex) {
            uiState = uiState.copy(selectedDubIndex = it)
        }

        observe(viewModel.rangeSelections) {
            rangeSelections = it
            uiState = uiState.copy(ranges = it.map { r -> r.first?.asStringNull(context) ?: "" }.toPersistentList())
        }

        observe(viewModel.selectedRangeIndex) {
            uiState = uiState.copy(selectedRangeIndex = it)
        }

        observe(viewModel.recommendations) { recommendations ->
            uiState = uiState.copy(recommendations = recommendations.toPersistentList())
        }

        observeNullable(viewModel.episodes) { episodes ->
            if (episodes == null) return@observeNullable
            if (episodes is Resource.Success) {
                val epList = episodes.value.toPersistentList()
                uiState = uiState.copy(
                    episodes = epList,
                    isMovie = epList.isEmpty() || uiState.resumeStatus?.isMovie == true
                )
            }
        }

        observeNullable(viewModel.page) { data ->
            if (data == null) return@observeNullable
            binding.apply {
                when (data) {
                    is Resource.Success -> {
                        val d = data.value
                        val title = d.titleText.asStringNull(context) ?: d.title
                        val provider = d.apiName.asStringNull(context) ?: storedData.apiName
                        val plot = d.plotText.asStringNull(context) ?: ""
                        val year = d.yearText?.asStringNull(context)
                        val duration = d.durationText?.asStringNull(context)
                        val rating = d.ratingText?.asStringNull(context)
                        val contentRating = d.contentRatingText?.asStringNull(context)
                        val genres = d.tags.toPersistentList()
                        val castNames = d.actorsText?.asStringNull(context)
                            ?.replace(Regex("""^Cast:\s*""", RegexOption.IGNORE_CASE), "")
                            ?.split(",")
                            ?.map { it.trim() }
                            ?.filter { it.isNotBlank() }
                            ?: emptyList()
                        val actors = (d.actors ?: castNames.map { ActorData(Actor(it)) }).toPersistentList()
                        val ongoingInfo = d.onGoingText?.asStringNull(context)
                        val isOngoing = d.isOngoing ?: (d.showStatus?.let { it == ShowStatus.Ongoing } == true)

                        uiState = uiState.copy(
                            title = title,
                            providerName = provider,
                            backdropUrl = d.posterBackgroundImage ?: d.posterImage,
                            posterUrl = d.posterImage,
                            logoUrl = d.logoUrl,
                            matchScore = rating,
                            releaseYear = year,
                            seasonsCount = duration,
                            maturityRating = contentRating,
                            statusText = ongoingInfo,
                            isOngoing = isOngoing,
                            nextAiringUnixTime = d.nextAiringUnixTime,
                            nextAiringEpisode = d.nextAiringEpisode?.asStringNull(context),
                            nextAiringDate = d.nextAiringDate?.asStringNull(context),
                            synopsis = plot,
                            genres = genres,
                            actors = actors,
                            comingSoon = d.comingSoon,
                            isLoaded = true
                        )

                        resultComposeView.isVisible = true
                        resultLoading.isGone = true
                        resultLoadingError.isGone = true
                    }

                    is Resource.Loading -> {
                        resultComposeView.isGone = true
                        resultLoading.isVisible = true
                        resultLoadingError.isGone = true
                    }

                    is Resource.Failure -> {
                        resultComposeView.isGone = true
                        resultLoading.isGone = true
                        resultLoadingError.isVisible = true
                        resultErrorText.text = storedData.url.plus("\n") + data.errorString
                    }
                }
            }
        }
    }
}
