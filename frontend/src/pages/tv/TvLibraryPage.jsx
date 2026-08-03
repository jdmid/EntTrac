import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import LibraryPageLayout from '../../components/LibraryPageLayout'
import LibraryMediaCard from '../../components/LibraryMediaCard'
import { getTvLibrary, refreshAllTv, refreshOngoingTv } from '../../api/tvApi'
import { themes } from '../../theme/themes'
import { SERIES_STATUS_FILTERS, SORT_OPTIONS } from '../../utils/statusMapping'
import { sortTv } from '../../utils/sortUtils'
import { useMediaLibrary } from '../../hooks/useMediaLibrary'


const TV_STATUS_FILTERS = [
  { value: 'ALL',       label: 'All' },
  { value: 'CONSUMING', label: 'Watching' },
  { value: 'PLANNED',   label: 'Plan to Watch' },
  { value: 'FINISHED',  label: 'Finished' },
  { value: 'DROPPED',   label: 'Dropped' },
]

function TvLibraryPage() {
  const navigate = useNavigate()

  const [statusFilter, setStatusFilter] = useState('ALL')
  const [seriesStatusFilter, setSeriesStatusFilter] = useState('ALL')
  const [sortBy, setSortBy] = useState('MOST_UNREAD')

  const { library, loading, error, handleRefreshAll } = useMediaLibrary({
    getLibrary: getTvLibrary,
    refreshAll: refreshAllTv,
    refreshOngoing: refreshOngoingTv,
  })

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