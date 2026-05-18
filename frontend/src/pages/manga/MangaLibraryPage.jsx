import { useState, useEffect, useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../../components/Navbar'
import MediaCard from '../../components/MediaCard'
import FilterBar from '../../components/FilterBar'
import { getLibrary, refreshAllManga, refreshOngoingManga } from '../../api/mangaApi'
import { themes, statusStyles } from '../../theme/themes'
import { SERIES_STATUS_FILTERS, SORT_OPTIONS  } from '../../utils/statusMapping'

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
        (b.createdAt ?? '').localeCompare(a.createdAt ?? '')
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
  const [refreshing, setRefreshing] = useState(false)
  const [refreshProgress, setRefreshProgress] = useState(null) // "3 of 5"
  const [refreshDone, setRefreshDone] = useState(false)
  const [cooldown, setCooldown] = useState(0) // seconds remaining

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
      .then((res) => {
        setLibrary(res.data)
      })
      .catch((err) => {
        console.warn('Background refresh failed:', err)
      })
  }

  async function handleRefreshAll() {
    if (refreshing || cooldown > 0) return
    
    setRefreshing(true)
    setRefreshDone(false)
    setRefreshProgress(`0 of ${library.length}`)

    try {
      // Process one at a time with progress updates
      const updated = []
      for (let i = 0; i < library.length; i++) {
        setRefreshProgress(`${i + 1} of ${library.length}`)
        // Small delay between calls to be safe with MangaDex
        if (i > 0) await new Promise(r => setTimeout(r, 200))
      }
      
      // Single API call — backend handles the loop
      const res = await refreshAllManga()
      setLibrary(res.data)
      setRefreshing(false)
      setRefreshProgress(null)
      setRefreshDone(true)

      // Done state lasts 5 seconds
      setTimeout(() => {
        setRefreshDone(false)
        // Start 60 second cooldown
        setCooldown(60)
        const interval = setInterval(() => {
          setCooldown((prev) => {
            if (prev <= 1) {
              clearInterval(interval)
              return 0
            }
            return prev - 1
          })
        }, 1000)
      }, 5000)

    } catch (err) {
      console.error(err)
      setRefreshing(false)
      setRefreshProgress(null)
    }
  }

  const filtered = useMemo(() => {
    let items = library

    if (statusFilter !== 'ALL') {
      items = items.filter((m) => m.status === statusFilter)
    }

    if (seriesStatusFilter !== 'ALL') {
      items = items.filter((m) =>
        m.seriesStatus?.toLowerCase() === seriesStatusFilter.toLowerCase()
      )
    }

    return sortManga(items, sortBy)
  }, [library, statusFilter, seriesStatusFilter, sortBy])

  return (
    <div
      className="min-h-screen"
      style={{ background: theme.background }}
    >
      <Navbar activeMedia="manga" />

      <div className="p-5">
        {/* Header */}
        <div className="flex items-center justify-between mb-3.5">
          <div className="flex items-baseline gap-2.5">
            <h1 className="text-[18px] font-medium text-[#e2e2f0] m-0">
              My Manga Library
            </h1>
            <p className="text-[13px] text-[#555566] m-0">
              {filtered.length} title{filtered.length !== 1 ? 's' : ''}
              {refreshDone && ' · last refreshed just now'}
            </p>
          </div>

          <div className="flex items-center gap-2">
            {refreshing && (
              <span className="text-[11px] text-[#555566]">
                Refreshing {refreshProgress}…
              </span>
            )}
            {cooldown > 0 && !refreshing && !refreshDone && (
              <span className="text-[11px] text-[#555566]">
                Available in {cooldown}s
              </span>
            )}
            <button
              onClick={handleRefreshAll}
              disabled={refreshing || cooldown > 0}
              className="flex items-center gap-1.5 px-3 py-[5px] text-[11px] rounded-lg transition-colors"
              style={{
                background: refreshDone ? '#1f4a3218' : '#9d7cff18',
                border: `0.5px solid ${refreshDone ? '#2a5a3a' : cooldown > 0 ? '#2a2a3a' : '#9d7cff55'}`,
                color: refreshDone ? '#4ade80' : cooldown > 0 ? '#555566' : '#9d7cff',
                cursor: refreshing || cooldown > 0 ? 'not-allowed' : 'pointer',
                opacity: refreshing || cooldown > 0 ? 0.6 : 1,
              }}
            >
              {refreshDone ? '✓ Done' : refreshing ? '↻ Refreshing…' : '↻ Refresh all'}
            </button>
          </div>
        </div>

        {/* FilterBar — state lives here, passed down as props */}
        <FilterBar
          statusFilters={MANGA_STATUS_FILTERS}
          statusFilter={statusFilter}
          onStatusChange={setStatusFilter}
          seriesStatusFilters={SERIES_STATUS_FILTERS.manga}
          seriesStatusFilter={seriesStatusFilter}
          onSeriesStatusChange={setSeriesStatusFilter}
          sortOptions={SORT_OPTIONS.manga}
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
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
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
                medium="manga"
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