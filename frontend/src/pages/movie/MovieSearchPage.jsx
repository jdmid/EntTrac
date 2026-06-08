import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../../components/Navbar'
import SearchMediaCard from '../../components/SearchMediaCard'
import { searchMovies, addMovieToLibrary, getMovieLibrary, getMovieDetails } from '../../api/movieApi'
import { normalizeSeriesStatus } from '../../utils/statusMapping'
import { themes } from '../../theme/themes'
import AttributionFooter from '../../components/AttributionFooter'

function MovieSearchPage() {
  const navigate = useNavigate()
  const theme = themes.movie

  const [query, setQuery] = useState('')
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [addedIds, setAddedIds] = useState(new Set())

  useEffect(() => {
    getMovieLibrary()
      .then((res) => {
        const ids = new Set(res.data.map((m) => m.movieId))
        setAddedIds(ids)
      })
      .catch(console.error)
  }, [])

  function handleSearch(e) {
    e.preventDefault()
    if (!query.trim()) return

    setLoading(true)
    setError(null)

    searchMovies(query)
      .then((res) => {
        setResults(res.data)
        setLoading(false)
      })
      .catch((err) => {
        console.error(err)
        setError('Search failed. Is the backend running?')
        setLoading(false)
      })
  }

  async function handleAdd(movie) {
    try {
      const detailRes = await getMovieDetails(movie.id)
      const full = detailRes.data
      await addMovieToLibrary({
        movieId: full.id ?? movie.id,
        title: full.title ?? movie.title,
        status: 'PLANNED',
        coverUrl: full.coverUrl ?? movie.coverUrl,
        description: full.description ?? movie.description,
        seriesStatus: normalizeSeriesStatus(full.status ?? movie.status, 'movie'),
        releaseYear: full.releaseYear ?? movie.releaseYear,
        runtime: full.runtime ?? movie.runtime,
        genres: full.genres ?? movie.genres,
        director: full.director ?? movie.director,
        imdbRating: full.imdbRating ?? null,
        rottenTomatoesRating: full.rottenTomatoesRating ?? null,
        metacriticRating: full.metacriticRating ?? null,
        tmdbRating: full.communityRating ?? movie.communityRating,
      })
      setAddedIds((prev) => new Set([...prev, movie.id]))
    } catch (err) {
      console.error(err)
    }
  }

  return (
    <div className="min-h-screen" style={{ background: theme.background }}>
      <Navbar activeMedia="movie" />

      <div className="p-5">
        <div className="flex items-baseline gap-2.5 mb-4">
          <h1 className="text-[18px] font-medium text-[#e2e2f0] m-0">
            Search Movies
          </h1>
          <p className="text-[13px] text-[#555566] m-0">
            Find and add titles to your library
          </p>
        </div>

        <form onSubmit={handleSearch} className="flex gap-2 mb-6">
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search for movies..."
            className="flex-1 px-3 py-2 text-[13px] text-[#e2e2f0] rounded-lg outline-none"
            style={{
              background: theme.topBar,
              border: `0.5px solid ${theme.cardBorder}`,
            }}
          />
          <button
            type="submit"
            className="px-4 py-2 text-[13px] font-medium rounded-lg transition-colors"
            style={{
              background: theme.accent,
              color: '#ffffff',
            }}
          >
            Search
          </button>
        </form>

        {loading && (
          <div className="text-[13px] text-[#555566] text-center py-12">
            Searching…
          </div>
        )}

        {!loading && error && (
          <div className="text-[13px] text-[#f87171] text-center py-12">
            {error}
          </div>
        )}

        {!loading && !error && results.length === 0 && query && (
          <div className="text-[13px] text-[#555566] text-center py-12">
            No results found for "{query}"
          </div>
        )}

        {!loading && results.length > 0 && (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3 items-start">
            {results.map((movie) => (
              <SearchMediaCard
                key={movie.id}
                title={movie.title}
                creator={movie.releaseYear}
                seriesStatus={movie.status}
                coverUrl={movie.coverUrl}
                theme={theme}
                icon="🎬"
                isAdded={addedIds.has(movie.id)}
                onAdd={() => handleAdd(movie)}
                onClick={() => navigate(`/movie/library/${movie.id}`, {
                  state: { from: 'search' }
                })}
              />
            ))}
          </div>
        )}

        {addedIds.size > 0 && (
          <p className="text-[11px] text-[#555566] text-center mt-4">
            {addedIds.size} title{addedIds.size !== 1 ? 's' : ''} added —{' '}
            <span
              className="cursor-pointer"
              style={{ color: theme.accent }}
              onClick={() => navigate('/movie/library')}
            >
              go to library
            </span>
          </p>
        )}

        <AttributionFooter />
      </div>
    </div>
  )
}

export default MovieSearchPage