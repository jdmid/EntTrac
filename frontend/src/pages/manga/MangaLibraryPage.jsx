import { useState, useEffect, useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../../components/Navbar'
import MediaCard from '../../components/MediaCard'
import FilterBar from '../../components/FilterBar'
import { getLibrary } from '../../api/mangaApi'
import { themes, statusStyles } from '../../theme/themes'

const MANGA_STATUS_FILTERS = [
  { value: 'ALL',       label: 'All' },
  { value: 'CONSUMING', label: 'Reading' },
  { value: 'PLANNED',   label: 'Plan to Read' },
  { value: 'FINISHED',  label: 'Finished' },
  { value: 'DROPPED',   label: 'Dropped' },
]

function sortManga(items, sortBy) {
  const arr = [...items]
  switch (sortBy) {
    case 'MOST_UNREAD':
      return arr.sort((a, b) => {
        const unreadA = (a.latestChapter ?? 0) - (a.chaptersRead ?? 0)
        const unreadB = (b.latestChapter ?? 0) - (b.chaptersRead ?? 0)
        return unreadB - unreadA
      })
    case 'ALPHA_AZ':
      return arr.sort((a, b) => a.title.localeCompare(b.title))
    case 'ALPHA_ZA':
      return arr.sort((a, b) => b.title.localeCompare(a.title))
    case 'SCORE_HIGH':
      return arr.sort((a, b) => (b.score ?? 0) - (a.score ?? 0))
    case 'SCORE_LOW':
      return arr.sort((a, b) => (a.score ?? 0) - (b.score ?? 0))
    case 'RECENTLY_UPDATED':
      return arr.sort((a, b) =>
        (b.updatedAt ?? '').localeCompare(a.updatedAt ?? '')
      )
    case 'RECENTLY_ADDED':
      return arr.sort((a, b) =>
        (b.mangaId ?? '').localeCompare(a.mangaId ?? '')
      )
    default:
      return arr
  }
}

function MangaLibraryPage() {
  const navigate = useNavigate()
  const theme = themes.manga

  // Data state
  const [library, setLibrary] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  // Filter state — lifted up so the card grid can react to it
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [seriesStatusFilter, setSeriesStatusFilter] = useState('ALL')
  const [sortBy, setSortBy] = useState('MOST_UNREAD')

  useEffect(() => {
    setLoading(true)
    getLibrary()
      .then((res) => {
        setLibrary(res.data)
        setLoading(false)
      })
      .catch((err) => {
        console.error(err)
        setError('Failed to load library.')
        setLoading(false)
      })
  }, [])

  const filtered = useMemo(() => {
    let items = library

    if (statusFilter !== 'ALL') {
      items = items.filter((m) => m.status === statusFilter)
    }

    return sortManga(items, sortBy)
  }, [library, statusFilter, sortBy])

  return (
    <div
      className="min-h-screen"
      style={{ background: theme.background }}
    >
      <Navbar activeMedia="manga" />

      <div className="p-5">
        {/* Header */}
        <div className="flex items-baseline gap-2.5 mb-3.5">
          <h1 className="text-[18px] font-medium text-[#e2e2f0] m-0">
            My Manga Library
          </h1>
          <p className="text-[13px] text-[#555566] m-0">
            {filtered.length} title{filtered.length !== 1 ? 's' : ''}
          </p>
        </div>

        {/* FilterBar — state lives here, passed down as props */}
        <FilterBar
          statusFilters={MANGA_STATUS_FILTERS}
          statusFilter={statusFilter}
          onStatusChange={setStatusFilter}
          seriesStatusFilter={seriesStatusFilter}
          onSeriesStatusChange={setSeriesStatusFilter}
          sortBy={sortBy}
          onSortChange={setSortBy}
          theme={theme}
        />

        {/* Loading */}
        {loading && (
          <div className="text-[13px] text-[#555566] text-center py-12">
            Loading library…
          </div>
        )}

        {/* Error */}
        {!loading && error && (
          <div className="text-[13px] text-[#f87171] text-center py-12">
            {error}
          </div>
        )}

        {/* Empty */}
        {!loading && !error && filtered.length === 0 && (
          <div className="text-[13px] text-[#555566] text-center py-12">
            {library.length === 0
              ? 'Your library is empty. Search for manga to add!'
              : 'No manga match the current filters.'}
          </div>
        )}

        {/* Card grid */}
        {!loading && !error && filtered.length > 0 && (
          <div className="grid grid-cols-4 gap-3">
            {filtered.map((manga) => (
              <MediaCard
                key={manga.mangaId}
                title={manga.title}
                score={manga.score}
                status={manga.status}
                progress={manga.chaptersRead}
                total={manga.latestChapter}
                theme={theme}
                icon="📖"
                progressLabel="Ch."
                coverUrl={manga.coverUrl}
                seriesStatus={manga.seriesStatus}
                onClick={() => navigate(`/manga/library/${manga.mangaId}`)}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default MangaLibraryPage