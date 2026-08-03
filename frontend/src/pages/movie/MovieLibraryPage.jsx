import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import LibraryPageLayout from '../../components/LibraryPageLayout'
import LibraryMediaCard from '../../components/LibraryMediaCard'
import { getMovieLibrary } from '../../api/movieApi'
import { themes } from '../../theme/themes'
import { SERIES_STATUS_FILTERS, SORT_OPTIONS } from '../../utils/statusMapping'
import { sortMovies } from '../../utils/sortUtils'
import { useMediaLibrary } from '../../hooks/useMediaLibrary'

const MOVIE_STATUS_FILTERS = [
  { value: 'ALL',       label: 'All' },
  { value: 'PLANNED',   label: 'Plan to Watch' },
  { value: 'FINISHED',  label: 'Watched' },
  { value: 'DROPPED',   label: 'Dropped' },
]

function MovieLibraryPage() {
  const navigate = useNavigate()

  const [statusFilter, setStatusFilter] = useState('ALL')
  const [seriesStatusFilter, setSeriesStatusFilter] = useState('ALL')
  const [sortBy, setSortBy] = useState('RECENTLY_ADDED')

  const { library, loading, error } = useMediaLibrary({ getLibrary: getMovieLibrary })

  return (
    <LibraryPageLayout
      activeMedia="movie"
      pageTitle="My Movie Library"
      library={library}
      loading={loading}
      error={error}
      emptyMessage="Your library is empty. Search for movies to add!"
      emptyFilterMessage="No movies match the current filters."
      statusFilters={MOVIE_STATUS_FILTERS}
      statusFilter={statusFilter}
      onStatusChange={setStatusFilter}
      seriesStatusFilters={SERIES_STATUS_FILTERS.movie}
      seriesStatusFilter={seriesStatusFilter}
      onSeriesStatusChange={setSeriesStatusFilter}
      sortOptions={SORT_OPTIONS.movie}
      sortBy={sortBy}
      onSortChange={setSortBy}
      sortFn={sortMovies}
      renderCard={(movie) => (
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
          theme={themes.movie}
          icon="🎬"
          medium="movie"
          onClick={() => navigate(`/movie/library/${movie.movieId}`)}
        />
      )}
    />
  )
}

export default MovieLibraryPage