import { useState, useEffect, useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import LibraryPageLayout from '../../components/LibraryPageLayout'
import LibraryMediaCard from '../../components/LibraryMediaCard'
import { getLibrary, refreshAllManga, refreshOngoingManga } from '../../api/mangaApi'
import { themes } from '../../theme/themes'
import { SERIES_STATUS_FILTERS, SORT_OPTIONS } from '../../utils/statusMapping'
import { sortManga } from '../../utils/sortUtils'

const MANGA_STATUS_FILTERS = [
  { value: 'ALL',      label: 'All' },
  { value: 'CONSUMING', label: 'Reading' },
  { value: 'PLANNED',  label: 'Plan to Read' },
  { value: 'FINISHED', label: 'Finished' },
  { value: 'DROPPED',  label: 'Dropped' },
]

function MangaLibraryPage() {
  const navigate = useNavigate()

  const [library, setLibrary] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const [statusFilter, setStatusFilter] = useState('ALL')
  const [seriesStatusFilter, setSeriesStatusFilter] = useState('ALL')
  const [sortBy, setSortBy] = useState('MOST_UNREAD')

  useEffect(() => {
    setLoading(true)
    getLibrary()
      .then((res) => {
        setLibrary(res.data)
        setLoading(false)
        backgroundRefreshOngoing(res.data)
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

    refreshOngoingManga()
      .then((res) => setLibrary(res.data))
      .catch((err) => console.warn('Background refresh failed:', err))
  }

  async function handleRefreshAll() {
    const res = await refreshAllManga()
    setLibrary(res.data)
  }

  return (
    <LibraryPageLayout
      activeMedia="manga"
      pageTitle="My Manga Library"
      library={library}
      loading={loading}
      error={error}
      emptyMessage="Your library is empty. Search for manga to add!"
      emptyFilterMessage="No manga match the current filters."
      statusFilters={MANGA_STATUS_FILTERS}
      statusFilter={statusFilter}
      onStatusChange={setStatusFilter}
      seriesStatusFilters={SERIES_STATUS_FILTERS.manga}
      seriesStatusFilter={seriesStatusFilter}
      onSeriesStatusChange={setSeriesStatusFilter}
      sortOptions={SORT_OPTIONS.manga}
      sortBy={sortBy}
      onSortChange={setSortBy}
      sortFn={sortManga}
      onRefreshAll={handleRefreshAll}
      renderCard={(manga) => (
        <LibraryMediaCard
          key={manga.mangaId}
          title={manga.title}
          score={manga.score}
          status={manga.status}
          progress={manga.chaptersRead}
          total={manga.latestChapter}
          theme={themes.manga}
          icon="📖"
          progressLabel="Ch."
          coverUrl={manga.coverUrl}
          seriesStatus={manga.seriesStatus}
          medium="manga"
          onClick={() => navigate(`/manga/library/${manga.mangaId}`)}
        />
      )}
    />
  )
}

export default MangaLibraryPage