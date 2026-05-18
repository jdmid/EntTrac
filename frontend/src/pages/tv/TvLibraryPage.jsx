import { useState, useEffect, useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../../components/Navbar'
import MediaCard from '../../components/MediaCard'
import FilterBar from '../../components/FilterBar'
import { getTvLibrary, refreshAllTv } from '../../api/tvApi'
import { themes } from '../../theme/themes'
import { SERIES_STATUS_FILTERS, SORT_OPTIONS, normalizeSeriesStatus  } from '../../utils/statusMapping'

const TV_STATUS_FILTERS = [
  { value: 'ALL',       label: 'All' },
  { value: 'CONSUMING', label: 'Watching' },
  { value: 'PLANNED',   label: 'Plan to Watch' },
  { value: 'FINISHED',  label: 'Finished' },
  { value: 'DROPPED',   label: 'Dropped' },
]

function sortTv(items, sortBy) {
  const arr = [...items]
  switch (sortBy) {
    case 'MOST_UNREAD':
      return arr.sort((a, b) => {
        const unwatchedA = (a.totalEpisodes ?? 0) - (a.episodesWatched ?? 0)
        const unwatchedB = (b.totalEpisodes ?? 0) - (b.episodesWatched ?? 0)
        return unwatchedB - unwatchedA
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
        (b.updatedAt ?? '').localeCompare(a.updatedAt ?? ''))
    case 'RECENTLY_ADDED':
      return arr.sort((a, b) =>
        (b.tvId ?? '').localeCompare(a.tvId ?? ''))
    default:
      return arr
  }
}

function TvLibraryPage() {
  const navigate = useNavigate()
  const theme = themes.tv

  const [library, setLibrary] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const [statusFilter, setStatusFilter] = useState('ALL')
  const [seriesStatusFilter, setSeriesStatusFilter] = useState('ALL')
  const [sortBy, setSortBy] = useState('MOST_UNREAD')

  const [refreshing, setRefreshing] = useState(false)
  const [refreshProgress, setRefreshProgress] = useState(null)
  const [refreshDone, setRefreshDone] = useState(false)
  const [cooldown, setCooldown] = useState(0)

  useEffect(() => {
    setLoading(true)
    getTvLibrary()
      .then((res) => {
        setLibrary(res.data)
        setLoading(false)
        // Stale-while-revalidate — silently refresh ongoing shows
        // after initial data is displayed
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
      (item) => item.seriesStatus === 'ongoing' && item.status !== 'DROPPED'
    )
    if (ongoing.length === 0) return

    // Fire refresh-all silently — update state when it resolves
    refreshAllTv()
      .then((res) => {
        setLibrary(res.data)
      })
      .catch((err) => {
        // Silent failure — user still sees stored data
        console.warn('Background refresh failed:', err)
      })
  }

  async function handleRefreshAll() {
    if (refreshing || cooldown > 0) return

    setRefreshing(true)
    setRefreshDone(false)
    setRefreshProgress(`0 of ${library.length}`)

    try {
      for (let i = 0; i < library.length; i++) {
        setRefreshProgress(`${i + 1} of ${library.length}`)
        if (i > 0) await new Promise(r => setTimeout(r, 200))
      }

      const res = await refreshAllTv()
      setLibrary(res.data)
      setRefreshing(false)
      setRefreshProgress(null)
      setRefreshDone(true)

      setTimeout(() => {
        setRefreshDone(false)
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
      items = items.filter((t) => t.status === statusFilter)
    }

    if (seriesStatusFilter !== 'ALL') {
        items = items.filter((t) => {
            const normalized = normalizeSeriesStatus(t.seriesStatus, 'tv')
            return normalized === seriesStatusFilter.toLowerCase() ||
            t.seriesStatus?.toLowerCase() === seriesStatusFilter.toLowerCase()
        })
    }

    return sortTv(items, sortBy)
  }, [library, statusFilter, seriesStatusFilter, sortBy])

  return (
    <div className="min-h-screen" style={{ background: theme.background }}>
      <Navbar activeMedia="tv" />

      <div className="p-5">
        <div className="flex items-center justify-between mb-3.5">
          <div className="flex items-baseline gap-2.5">
            <h1 className="text-[18px] font-medium text-[#e2e2f0] m-0">
              My TV Library
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
                background: refreshDone ? '#1f4a3218' : `${theme.accent}18`,
                border: `0.5px solid ${refreshDone ? '#2a5a3a' : cooldown > 0 ? '#2a2a3a' : `${theme.accent}55`}`,
                color: refreshDone ? '#4ade80' : cooldown > 0 ? '#555566' : theme.accent,
                cursor: refreshing || cooldown > 0 ? 'not-allowed' : 'pointer',
                opacity: refreshing || cooldown > 0 ? 0.6 : 1,
              }}
            >
              {refreshDone ? '✓ Done' : refreshing ? '↻ Refreshing…' : '↻ Refresh all'}
            </button>
          </div>
        </div>

        <FilterBar
          statusFilters={TV_STATUS_FILTERS}
          statusFilter={statusFilter}
          onStatusChange={setStatusFilter}
          seriesStatusFilters={SERIES_STATUS_FILTERS.tv}
          seriesStatusFilter={seriesStatusFilter}
          onSeriesStatusChange={setSeriesStatusFilter}
          sortOptions={SORT_OPTIONS.tv}
          sortBy={sortBy}
          onSortChange={setSortBy}
          theme={theme}
        />

        {loading && (
          <div className="text-[13px] text-[#555566] text-center py-12">
            Loading library…
          </div>
        )}

        {!loading && error && (
          <div className="text-[13px] text-[#f87171] text-center py-12">
            {error}
          </div>
        )}

        {!loading && !error && filtered.length === 0 && (
          <div className="text-[13px] text-[#555566] text-center py-12">
            {library.length === 0
              ? 'Your library is empty. Search for TV shows to add!'
              : 'No shows match the current filters.'}
          </div>
        )}

        {!loading && !error && filtered.length > 0 && (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
            {filtered.map((show) => (
              <MediaCard
                key={show.tvId}
                title={show.title}
                score={show.score}
                status={show.status}
                progress={show.episodesWatched}
                total={show.totalEpisodes}
                totalLabel="Ep."
                seriesStatus={show.seriesStatus}
                coverUrl={show.coverUrl}
                theme={theme}
                icon="📺"
                progressLabel={show.totalEpisodes > 0 ? 'Ep.' : null}
                medium="tv"        
                creator={(() => {
                    const parts = [
                        show.seriesType ?? null,
                        show.firstAirYear ?? null,
                        show.numberOfSeasons
                        ? `${show.numberOfSeasons} season${show.numberOfSeasons !== 1 ? 's' : ''}`
                        : null,
                    ].filter(Boolean)
                    const result = parts.length > 0 ? parts.join(' · ') : 'TBD'
                    return result
                    })()}
                onClick={() => navigate(`/tv/library/${show.tvId}`)}
                />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default TvLibraryPage