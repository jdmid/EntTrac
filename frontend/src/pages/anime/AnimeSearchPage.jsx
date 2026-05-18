import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../../components/Navbar'
import MediaCard from '../../components/MediaCard'
import { searchAnime, addAnimeToLibrary, getAnimeLibrary } from '../../api/animeApi'
import { normalizeSeriesStatus } from '../../utils/statusMapping'
import { themes } from '../../theme/themes'

function AnimeSearchPage() {
  const navigate = useNavigate()
  const theme = themes.anime

  const [query, setQuery] = useState('')
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [addedIds, setAddedIds] = useState(new Set())

  useEffect(() => {
    getAnimeLibrary().then((res) => {
      const ids = new Set(res.data.map((a) => a.animeId))
      setAddedIds(ids)
    }).catch(console.error)
  }, [])

  function handleSearch(e) {
    e.preventDefault()
    if (!query.trim()) return

    setLoading(true)
    setError(null)

    searchAnime(query)
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

  function handleAdd(anime) {
    addAnimeToLibrary({
      animeId: anime.id,
      title: anime.title,
      status: 'PLANNED',
      episodesWatched: 0,
      totalEpisodes: anime.totalEpisodes,
      coverUrl: anime.coverUrl,
      description: anime.description,
      seriesStatus: normalizeSeriesStatus(anime.status, 'anime'),
      studio: anime.studio,
      season: anime.season,
      communityRating: anime.communityRating,
    })
      .then(() => {
        setAddedIds((prev) => new Set([...prev, anime.id]))
      })
      .catch(console.error)
  }

  return (
    <div
      className="min-h-screen"
      style={{ background: theme.background }}
    >
      <Navbar activeMedia="anime" />

      <div className="p-5">
        <div className="flex items-baseline gap-2.5 mb-4">
          <h1 className="text-[18px] font-medium text-[#e2e2f0] m-0">
            Search Anime
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
            placeholder="Search for anime..."
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
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
            {results.map((anime) => (
              <MediaCard
                key={anime.id}
                title={anime.title}
                creator={anime.studio}
                seriesStatus={anime.status}
                coverUrl={anime.coverUrl}
                theme={theme}
                icon="🎞️"
                isAdded={addedIds.has(anime.id)}
                onAdd={() => handleAdd(anime)}
                onClick={() => navigate(`/anime/library/${anime.id}`, { state: { from: 'search' } })}
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
              onClick={() => navigate('/anime/library')}
            >
              go to library
            </span>
          </p>
        )}
      </div>
    </div>
  )
}

export default AnimeSearchPage