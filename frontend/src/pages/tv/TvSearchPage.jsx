import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../../components/Navbar'
import SearchMediaCard from '../../components/SearchMediaCard'
import { searchTv, addTvToLibrary, getTvLibrary } from '../../api/tvApi'
import { normalizeSeriesStatus } from '../../utils/statusMapping'
import { themes } from '../../theme/themes'

function TvSearchPage() {
  const navigate = useNavigate()
  const theme = themes.tv

  const [query, setQuery] = useState('')
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [addedIds, setAddedIds] = useState(new Set())

  useEffect(() => {
    getTvLibrary()
      .then((res) => {
        const ids = new Set(res.data.map((t) => t.tvId))
        setAddedIds(ids)
      })
      .catch(console.error)
  }, [])

  function handleSearch(e) {
    e.preventDefault()
    if (!query.trim()) return

    setLoading(true)
    setError(null)

    searchTv(query)
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

  function handleAdd(show) {
    addTvToLibrary({
      tvId: show.id,
      title: show.title,
      status: 'PLANNED',
      episodesWatched: 0,
      currentSeason: 1,
      totalEpisodes: show.totalEpisodes,
      numberOfSeasons: show.numberOfSeasons,
      seasonEpisodes: show.seasonEpisodes,
      coverUrl: show.coverUrl,
      description: show.description,
      seriesStatus: normalizeSeriesStatus(show.status, 'tv'),
      network: show.network,
      genres: show.genres,
      firstAirYear: show.firstAirYear,
      seriesType: show.seriesType,
      communityRating: show.communityRating,
      nextEpisodeDate: show.nextEpisodeDate,
    })
      .then(() => {
        setAddedIds((prev) => new Set([...prev, show.id]))
      })
      .catch(console.error)
  }

  return (
    <div className="min-h-screen" style={{ background: theme.background }}>
      <Navbar activeMedia="tv" />

      <div className="p-5">
        <div className="flex items-baseline gap-2.5 mb-4">
          <h1 className="text-[18px] font-medium text-[#e2e2f0] m-0">
            Search TV Shows
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
            placeholder="Search for TV shows..."
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
            {results.map((show) => (
              <SearchMediaCard 
                key={show.id}
                title={show.title}
                creator={(() => {
                    const parts = [
                        show.seriesType ?? null,
                        show.firstAirYear ?? null,
                        show.numberOfSeasons
                        ? `${show.numberOfSeasons} season${show.numberOfSeasons !== 1 ? 's' : ''}`
                        : null,
                    ].filter(Boolean)
                    if (parts.length > 0) return parts.join(' · ')
                    if (show.status) return show.status
                    return 'TBD'
                })()}
                seriesStatus={show.status}
                coverUrl={show.coverUrl}
                theme={theme}
                icon="📺"
                medium="tv"
                isAdded={addedIds.has(show.id)}
                onAdd={() => handleAdd(show)}
                onClick={() => navigate(`/tv/library/${show.id}`, {
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
              onClick={() => navigate('/tv/library')}
            >
              go to library
            </span>
          </p>
        )}
      </div>
    </div>
  )
}

export default TvSearchPage