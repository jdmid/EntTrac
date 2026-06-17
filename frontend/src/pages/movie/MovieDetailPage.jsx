import { useState, useEffect } from 'react'
import { useParams, useNavigate, useLocation } from 'react-router-dom'
import DetailPageLayout from '../../components/DetailPageLayout'
import RatingCard from '../../components/RatingCard'
import {
  getMovie, getMovieDetails, updateMovieScore, updateMovieStatus,
  refreshMovieRatings, removeMovieFromLibrary, addMovieToLibrary,
  updateMovieNotes, enrichMovieFromCache,
} from '../../api/movieApi'
import { themes } from '../../theme/themes'
import { normalizeSeriesStatus } from '../../utils/statusMapping'

function MovieDetailPage() {
  const { movieId } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const theme = themes.movie
  const fromSearch = location.state?.from === 'search'

  const [movie, setMovie] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [score, setScore] = useState(null)
  const [inLibrary, setInLibrary] = useState(true)
  const [refreshing, setRefreshing] = useState(false)

  useEffect(() => {
    getMovie(movieId)
      .then((res) => {
        setMovie(res.data)
        setScore(res.data.score ?? null)
        setLoading(false)
        // Enrich with OMDB scores if not already cached
        enrichMovieFromCache(movieId)
          .then((enriched) => setMovie(enriched.data))
          .catch((err) => console.error('Enrich failed:', err))
      })
      .catch(() => {
        getMovieDetails(movieId)
          .then((res) => {
            const data = res.data
            data.seriesStatus = normalizeSeriesStatus(data.status, 'movie')
            setMovie(data)
            setInLibrary(false)
            setLoading(false)
          })
          .catch(() => {
            setError('Failed to load movie.')
            setLoading(false)
          })
      })
  }, [movieId])

  if (loading) {
    return (
      <div className="min-h-screen" style={{ background: theme.background }}>
        <div className="text-[13px] text-[#555566] text-center py-12">
          Loading…
        </div>
      </div>
    )
  }

  if (error || !movie) {
    return (
      <div className="min-h-screen" style={{ background: theme.background }}>
        <div className="text-[13px] text-[#f87171] text-center py-12">
          {error ?? 'Movie not found.'}
        </div>
      </div>
    )
  }

  return (
    <DetailPageLayout
      activeMedia="movie"
      fromSearch={fromSearch}
      backPath={fromSearch ? '/movie/search' : '/movie/library'}
      title={movie.title}
      item={movie}
      inLibrary={inLibrary}
      score={score}
      theme={theme}
      icon="🎬"
      refreshLabel="Refresh ratings"
      refreshing={refreshing}
      metaLine={
        <div>
          <p className="text-[12px] text-[#555566] m-0 mb-1">
            {[
              movie.releaseYear,
              movie.runtime,
              movie.genres,
            ]
              .filter(Boolean)
              .join(' · ')}
          </p>
          {movie.director && (
            <p className="text-[12px] text-[#555566] m-0 mb-3">
              Directed by{' '}
              <span
                className="cursor-pointer inline-flex items-center gap-1"
                style={{ color: theme.accent }}
                onClick={() => navigate(
                  `/movie/search?tab=creator&creatorId=${movie.directorId}&creatorName=${encodeURIComponent(movie.director)}`
                )}
              >
                {movie.director}
                <svg width="10" height="10" viewBox="0 0 24 24" fill="none"
                  stroke="currentColor" strokeWidth="2" strokeLinecap="round"
                  strokeLinejoin="round" aria-hidden="true">
                  <path d="M18 13v6a2 2 0 01-2 2H5a2 2 0 01-2-2V8a2 2 0 012-2h6"/>
                  <polyline points="15 3 21 3 21 9"/>
                  <line x1="10" y1="14" x2="21" y2="3"/>
                </svg>
              </span>
            </p>
          )}
        </div>
      }
      progressSection={null}
      ratingsSection={
        <>
          <RatingCard value={movie.imdbRating} label="IMDb" color="#f5c518" theme={theme} />
          <RatingCard value={movie.rottenTomatoesRating} label="Rotten Tomatoes" color="#fa320a" theme={theme} />
          <RatingCard value={movie.metacriticRating} label="Metacritic" color="#66cc33" theme={theme} />
        </>
      }
      notesProgressLabel={null}
      onRefresh={() => {
        setRefreshing(true)
        refreshMovieRatings(movieId)
          .then((res) => setMovie(res.data))
          .finally(() => setRefreshing(false))
      }}
      onRemove={() =>
        removeMovieFromLibrary(movieId).then(() =>
          navigate(fromSearch ? '/movie/search' : '/movie/library')
        )
      }
      onAdd={() =>
        addMovieToLibrary({
          movieId: movie.id,
          title: movie.title,
          status: 'PLANNED',
          coverUrl: movie.coverUrl,
          description: movie.description,
          seriesStatus: normalizeSeriesStatus(movie.status, 'movie'),
          releaseYear: movie.releaseYear,
          runtime: movie.runtime,
          genres: movie.genres,
          director: movie.director,
          imdbRating: movie.imdbRating ?? null,
          rottenTomatoesRating: movie.rottenTomatoesRating ?? null,
          metacriticRating: movie.metacriticRating ?? null,
          tmdbScore: movie.tmdbScore,
          directorId: movie.directorId ?? null,
        }).then(() => setInLibrary(true))
      }
      onScoreSave={(n) =>
        updateMovieScore(movieId, n).then((res) => {
          setMovie(res.data)
          setScore(n)
        })
      }
      onStatusChange={(s) =>
        updateMovieStatus(movieId, s).then((res) => setMovie(res.data))
      }
      onNotesSave={(notes) =>
        updateMovieNotes(movieId, notes).then((res) => setMovie(res.data))
      }
    />
  )
}

export default MovieDetailPage