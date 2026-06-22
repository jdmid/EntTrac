import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import LibraryPageLayout from '../../components/LibraryPageLayout'
import LibraryMediaCard from '../../components/LibraryMediaCard'
import { getTvLibrary, refreshAllTv, refreshOngoingTv } from '../../api/tvApi'
import { themes } from '../../theme/themes'
import { SERIES_STATUS_FILTERS, SORT_OPTIONS } from '../../utils/statusMapping'
import { sortTv } from '../../utils/sortUtils'

const TV_STATUS_FILTERS = [
  { value: 'ALL',       label: 'All' },
  { value: 'CONSUMING', label: 'Watching' },
  { value: 'PLANNED',   label: 'Plan to Watch' },
  { value: 'FINISHED',  label: 'Finished' },
  { value: 'DROPPED',   label: 'Dropped' },
]

function TvLibraryPage() {
  const navigate = useNavigate()

  const [library, setLibrary] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const [statusFilter, setStatusFilter] = useState('ALL')
  const [seriesStatusFilter, setSeriesStatusFilter] = useState('ALL')
  const [sortBy, setSortBy] = useState('MOST_UNREAD')

  useEffect(() => {
    setLoading(true)
    getTvLibrary()
      .then((res) => {
        setLibrary(res.data)
        setLoading(false)
        backgroundRefreshOngoing(res.data)
      })
      .catch((err) => {
        console.error('Failed to load library:', err)
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

    refreshOngoingTv()
      .then((res) => setLibrary(res.data))
      .catch((err) => console.warn('Background refresh failed:', err))
  }

  async function handleRefreshAll() {
    const res = await refreshAllTv()
    setLibrary(res.data)
  }

  return (
    <LibraryPageLayout
      activeMedia="tv"
      pageTitle="My TV Library"
      library={library}
      loading={loading}
      error={error}
      emptyMessage="Your library is empty. Search for TV shows to add!"
      emptyFilterMessage="No shows match the current filters."
      statusFilters={TV_STATUS_FILTERS}
      statusFilter={statusFilter}
      onStatusChange={setStatusFilter}
      seriesStatusFilters={SERIES_STATUS_FILTERS.tv}
      seriesStatusFilter={seriesStatusFilter}
      onSeriesStatusChange={setSeriesStatusFilter}
      sortOptions={SORT_OPTIONS.tv}
      sortBy={sortBy}
      onSortChange={setSortBy}
      sortFn={sortTv}
      onRefreshAll={handleRefreshAll}
      renderCard={(show) => {
        const creatorParts = [
          show.seriesType ?? null,
          show.firstAirYear ?? null,
          show.numberOfSeasons
            ? `${show.numberOfSeasons} season${show.numberOfSeasons !== 1 ? 's' : ''}`
            : null,
        ].filter(Boolean)

        return (
          <LibraryMediaCard
            key={show.tvId}
            title={show.title}
            score={show.score}
            status={show.status}
            progress={show.episodesWatched}
            total={show.totalEpisodes}
            totalLabel="Ep."
            seriesStatus={show.seriesStatus}
            coverUrl={show.coverUrl}
            theme={themes.tv}
            icon="📺"
            progressLabel={show.totalEpisodes > 0 ? 'Ep.' : null}
            medium="tv"
            creator={creatorParts.length > 0 ? creatorParts.join(' · ') : 'TBD'}
            onClick={() => navigate(`/tv/library/${show.tvId}`)}
          />
        )
      }}
    />
  )
}

export default TvLibraryPage