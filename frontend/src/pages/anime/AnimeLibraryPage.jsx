import { useState, useEffect, useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../../components/Navbar'
import MediaCard from '../../components/MediaCard'
import FilterBar from '../../components/FilterBar'
import { getAnimeLibrary } from '../../api/animeApi'
import { themes, statusStyles } from '../../theme/themes'
import { SERIES_STATUS_FILTERS } from '../../utils/statusMapping'

const ANIME_STATUS_FILTERS = [
  { value: 'ALL',       label: 'All' },
  { value: 'CONSUMING', label: 'Watching' },
  { value: 'PLANNED',   label: 'Plan to Watch' },
  { value: 'FINISHED',  label: 'Finished' },
  { value: 'DROPPED',   label: 'Dropped' },
]

function sortAnime(items, sortBy) {
  const arr = [...items]
  switch (sortBy) {
    case 'MOST_UNWATCHED':
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
        (b.updatedAt ?? '').localeCompare(a.updatedAt ?? '')
      )
    case 'RECENTLY_ADDED':
      return arr.sort((a, b) =>
        (b.animeId ?? '').localeCompare(a.animeId ?? '')
      )
    default:
      return arr
  }
}

function AnimeLibraryPage() {
  const navigate = useNavigate()
  const theme = themes.anime

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
      items = items.filter((a) => a.status === statusFilter)
    }

    if (seriesStatusFilter !== 'ALL') {
      items = items.filter((a) =>
        a.seriesStatus?.toLowerCase() === seriesStatusFilter.toLowerCase()
      )
    }

    return sortAnime(items, sortBy)
  }, [library, statusFilter, seriesStatusFilter, sortBy])

  return (
    <div
      className="min-h-screen"
      style={{ background: theme.background }}
    >
      <Navbar activeMedia="anime" />

      <div className="p-5">
        <div className="flex items-baseline gap-2.5 mb-3.5">
          <h1 className="text-[18px] font-medium text-[#e2e2f0] m-0">
            My Anime Library
          </h1>
          <p className="text-[13px] text-[#555566] m-0">
            {filtered.length} title{filtered.length !== 1 ? 's' : ''}
          </p>
        </div>

        <FilterBar
          statusFilters={ANIME_STATUS_FILTERS}
          statusFilter={statusFilter}
          onStatusChange={setStatusFilter}
          seriesStatusFilter={seriesStatusFilter}
          seriesStatusFilters={SERIES_STATUS_FILTERS.anime}
          onSeriesStatusChange={setSeriesStatusFilter}
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
              ? 'Your library is empty. Search for anime to add!'
              : 'No anime match the current filters.'}
          </div>
        )}

        {!loading && !error && filtered.length > 0 && (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
            {filtered.map((anime) => (
              <MediaCard
                key={anime.animeId}
                title={anime.title}
                score={anime.score}
                status={anime.status}
                progress={anime.episodesWatched}
                total={anime.totalEpisodes}
                totalLabel="Ep."
                seriesStatus={anime.seriesStatus}
                coverUrl={anime.coverUrl}
                theme={theme}
                icon="📺"
                progressLabel="Ep."
                onClick={() => navigate(`/anime/library/${anime.animeId}`)}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default AnimeLibraryPage