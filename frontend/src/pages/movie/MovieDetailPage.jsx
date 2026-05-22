import { useState, useEffect } from 'react'
import { useParams, useNavigate, useLocation } from 'react-router-dom'
import DetailPageLayout from '../../components/DetailPageLayout'
import {
  getMovie, getMovieDetails, updateMovieScore, updateMovieStatus,
  refreshMovieRatings, removeMovieFromLibrary, addMovieToLibrary,
  getMovieCommunityRating, updateMovieNotes, enrichMovieFromCache,
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
  const [communityRating, setCommunityRating] = useState(null)
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
        getMovieCommunityRating(movieId)
          .then((res) => setCommunityRating(res.data))
          .catch(() => setCommunityRating(null))
      })
      .catch(() => {
        getMovieDetails(movieId)
          .then((res) => {
            const data = res.data
            data.seriesStatus = normalizeSeriesStatus(data.status, 'movie')
            setMovie(data)
            setInLibrary(false)
            setCommunityRating(data.communityRating ?? null)
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
      communityRating={null}
      communityRatingLabel={null}
      score={score}
      theme={theme}
      icon="🎬"
      refreshLabel="↻ Refresh ratings"
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
              <span style={{ color: theme.accent }}>{movie.director}</span>
            </p>
          )}
        </div>
      }
      progressSection={null}
      ratingsSection={
        inLibrary && (communityRating != null || movie.imdbRating || movie.rottenTomatoesRating || movie.metacriticRating) ? (
            <div className="flex gap-2 flex-wrap">
            {communityRating != null && (
                <div
                    className="rounded-lg p-3 text-center"
                    style={{
                    background: theme.topBar,
                    border: `0.5px solid ${theme.cardBorder}`,
                    minWidth: '80px',
                    }}
                >
                    <p className="text-[20px] font-medium m-0 mb-0.5"
                    style={{ color: '#01b4e4' }}>
                    {communityRating}
                    </p>
                    <p className="text-[10px] text-[#555566] m-0">TMDB</p>
                </div>
            )}    
            {movie.imdbRating != null && (
                <div
                className="rounded-lg p-3 text-center"
                style={{
                    background: theme.topBar,
                    border: `0.5px solid ${theme.cardBorder}`,
                    minWidth: '80px',
                }}
                >
                <p className="text-[20px] font-medium m-0 mb-0.5"
                    style={{ color: '#f5c518' }}>
                    {movie.imdbRating}
                </p>
                <p className="text-[10px] text-[#555566] m-0">IMDb</p>
                </div>
            )}
            {movie.rottenTomatoesRating && (
                <div
                className="rounded-lg p-3 text-center"
                style={{
                    background: theme.topBar,
                    border: `0.5px solid ${theme.cardBorder}`,
                    minWidth: '80px',
                }}
                >
                <p className="text-[20px] font-medium m-0 mb-0.5"
                    style={{ color: '#fa320a' }}>
                    {movie.rottenTomatoesRating}
                </p>
                <p className="text-[10px] text-[#555566] m-0">Rotten Tomatoes</p>
                </div>
            )}
            {movie.metacriticRating && (
                <div
                className="rounded-lg p-3 text-center"
                style={{
                    background: theme.topBar,
                    border: `0.5px solid ${theme.cardBorder}`,
                    minWidth: '80px',
                }}
                >
                <p className="text-[20px] font-medium m-0 mb-0.5"
                    style={{ color: '#66cc33' }}>
                    {movie.metacriticRating}
                </p>
                <p className="text-[10px] text-[#555566] m-0">Metacritic</p>
                </div>
            )}
            </div>
        ) : null
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
          communityRating: movie.communityRating,
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