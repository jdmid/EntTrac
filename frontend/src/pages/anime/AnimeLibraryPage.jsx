import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import LibraryPageLayout from '../../components/LibraryPageLayout'
import LibraryMediaCard from '../../components/LibraryMediaCard'
import { getAnimeLibrary, refreshAllAnime, refreshOngoingAnime } from '../../api/animeApi'
import { themes } from '../../theme/themes'
import { SERIES_STATUS_FILTERS, SORT_OPTIONS } from '../../utils/statusMapping'
import { sortAnime } from '../../utils/sortUtils'
import { useMediaLibrary } from '../../hooks/useMediaLibrary'


const ANIME_STATUS_FILTERS = [
  { value: 'ALL',       label: 'All' },
  { value: 'CONSUMING', label: 'Watching' },
  { value: 'PLANNED',   label: 'Plan to Watch' },
  { value: 'FINISHED',  label: 'Finished' },
  { value: 'DROPPED',   label: 'Dropped' },
]

function AnimeLibraryPage() {
  const navigate = useNavigate()

  const [statusFilter, setStatusFilter] = useState('ALL')
  const [seriesStatusFilter, setSeriesStatusFilter] = useState('ALL')
  const [sortBy, setSortBy] = useState('MOST_UNWATCHED')

  const { library, loading, error, handleRefreshAll } = useMediaLibrary({
    getLibrary: getAnimeLibrary,
    refreshAll: refreshAllAnime,
    refreshOngoing: refreshOngoingAnime,
    refreshDelay: 300, // AniList rate limit
  })

  return (
    <LibraryPageLayout
      activeMedia="anime"
      pageTitle="My Anime Library"
      library={library}
      loading={loading}
      error={error}
      emptyMessage="Your library is empty. Search for anime to add!"
      emptyFilterMessage="No anime match the current filters."
      statusFilters={ANIME_STATUS_FILTERS}
      statusFilter={statusFilter}
      onStatusChange={setStatusFilter}
      seriesStatusFilters={SERIES_STATUS_FILTERS.anime}
      seriesStatusFilter={seriesStatusFilter}
      onSeriesStatusChange={setSeriesStatusFilter}
      sortOptions={SORT_OPTIONS.anime}
      sortBy={sortBy}
      onSortChange={setSortBy}
      sortFn={sortAnime}
      onRefreshAll={handleRefreshAll}
      renderCard={(anime) => (
        <LibraryMediaCard
          key={anime.animeId}
          title={anime.title}
          score={anime.score}
          status={anime.status}
          progress={anime.episodesWatched}
          total={anime.totalEpisodes}
          totalLabel="Ep."
          seriesStatus={anime.seriesStatus}
          coverUrl={anime.coverUrl}
          theme={themes.anime}
          icon="🎞️"
          progressLabel="Ep."
          medium="anime"
          onClick={() => navigate(`/anime/library/${anime.animeId}`)}
        />
      )}
    />
  )
}

export default AnimeLibraryPage