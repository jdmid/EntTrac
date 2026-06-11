import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import LibraryPageLayout from '../../components/LibraryPageLayout'
import LibraryMediaCard from '../../components/LibraryMediaCard'
import { getAnimeLibrary, refreshAllAnime, refreshOngoingAnime } from '../../api/animeApi'
import { themes } from '../../theme/themes'
import { SERIES_STATUS_FILTERS, SORT_OPTIONS } from '../../utils/statusMapping'
import { sortAnime } from '../../utils/sortUtils'

const ANIME_STATUS_FILTERS = [
  { value: 'ALL',       label: 'All' },
  { value: 'CONSUMING', label: 'Watching' },
  { value: 'PLANNED',   label: 'Plan to Watch' },
  { value: 'FINISHED',  label: 'Finished' },
  { value: 'DROPPED',   label: 'Dropped' },
]

function AnimeLibraryPage() {
  const navigate = useNavigate()

  const [library, setLibrary] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const [statusFilter, setStatusFilter] = useState('ALL')
  const [seriesStatusFilter, setSeriesStatusFilter] = useState('ALL')
  const [sortBy, setSortBy] = useState('MOST_UNWATCHED')

  useEffect(() => {
    setLoading(true)
    getAnimeLibrary()
      .then((res) => {
        setLibrary(res.data)
        setLoading(false)
        setTimeout(() => backgroundRefreshOngoing(res.data), 300)
      })
      .catch((err) => {
        console.error(err)
        setError('Failed to load library.')
        setLoading(false)
      })
  }, [])

  function backgroundRefreshOngoing(items) {
    const ongoing = items.filter(
      (item) =>
        (item.seriesStatus === 'ongoing' || item.seriesStatus === 'hiatus') &&
        item.status !== 'DROPPED'
    )
    if (ongoing.length === 0) return

    refreshOngoingAnime()
      .then((res) => setLibrary(res.data))
      .catch((err) => console.warn('Background refresh failed:', err))
  }

  async function handleRefreshAll() {
    const res = await refreshAllAnime()
    setLibrary(res.data)
  }

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