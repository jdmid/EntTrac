import { useState, useEffect, useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../../components/Navbar'
import LibraryMediaCard from '../../components/LibraryMediaCard'
import FilterBar from '../../components/FilterBar'
import { getMovieLibrary } from '../../api/movieApi'
import { themes } from '../../theme/themes'
import { SERIES_STATUS_FILTERS, SORT_OPTIONS } from '../../utils/statusMapping'
import AttributionFooter from '../../components/AttributionFooter'

const MOVIE_STATUS_FILTERS = [
  { value: 'ALL',       label: 'All' },
  { value: 'CONSUMING', label: 'Watching' },
  { value: 'PLANNED',   label: 'Plan to Watch' },
  { value: 'FINISHED',  label: 'Watched' },
  { value: 'DROPPED',   label: 'Dropped' },
]

function sortMovies(items, sortBy) {
  const arr = [...items]
  switch (sortBy) {
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
        (b.createdAt ?? '').localeCompare(a.createdAt ?? ''))
    default:
      return arr
  }
}

function MovieLibraryPage() {
  const navigate = useNavigate()
  const theme = themes.movie

  const [library, setLibrary] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const [statusFilter, setStatusFilter] = useState('ALL')
  const [seriesStatusFilter, setSeriesStatusFilter] = useState('ALL')
  const [sortBy, setSortBy] = useState('RECENTLY_ADDED')

  useEffect(() => {
    setLoading(true)
    getMovieLibrary()
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

    if (seriesStatusFilter !== 'ALL') {
      items = items.filter((m) =>
        m.seriesStatus?.toLowerCase() === seriesStatusFilter.toLowerCase()
      )
    }

    return sortMovies(items, sortBy)
  }, [library, statusFilter, seriesStatusFilter, sortBy])

  return (
    <div className="min-h-screen" style={{ background: theme.background }}>
      <Navbar activeMedia="movie" />

      <div className="p-5">
        <div className="flex items-center justify-between mb-3.5">
          <div className="flex items-baseline gap-2.5">
            <h1 className="text-[18px] font-medium text-[#e2e2f0] m-0">
              My Movie Library
            </h1>
            <p className="text-[13px] text-[#555566] m-0">
              {filtered.length} title{filtered.length !== 1 ? 's' : ''}
            </p>
          </div>
        </div>

        <FilterBar
          statusFilters={MOVIE_STATUS_FILTERS}
          statusFilter={statusFilter}
          onStatusChange={setStatusFilter}
          seriesStatusFilters={SERIES_STATUS_FILTERS.movie}
          seriesStatusFilter={seriesStatusFilter}
          onSeriesStatusChange={setSeriesStatusFilter}
          sortOptions={SORT_OPTIONS.movie}
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
              ? 'Your library is empty. Search for movies to add!'
              : 'No movies match the current filters.'}
          </div>
        )}

        {!loading && !error && filtered.length > 0 && (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3 items-start">
            {filtered.map((movie) => (
              <LibraryMediaCard
                key={movie.movieId}
                title={movie.title}
                creator={[movie.director, movie.releaseYear]
                  .filter(Boolean)
                  .join(' · ')}
                score={movie.score}
                status={movie.status}
                seriesStatus={movie.seriesStatus}
                coverUrl={movie.coverUrl}
                theme={theme}
                icon="🎬"
                medium="movie"
                onClick={() => navigate(`/movie/library/${movie.movieId}`)}
              />
            ))}
          </div>
        )}

        <AttributionFooter />
      </div>
    </div>
  )
}

export default MovieLibraryPage