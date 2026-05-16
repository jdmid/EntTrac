import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../../components/Navbar'
import MediaCard from '../../components/MediaCard'
import { searchManga, addToLibrary, getLibrary } from '../../api/mangaApi'
import { themes } from '../../theme/themes'

function MangaSearchPage() {
  const navigate = useNavigate()
  const theme = themes.manga

  const [query, setQuery] = useState('')
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [addedIds, setAddedIds] = useState(new Set())

  useEffect(() => {
    getLibrary().then((res) => {
      const ids = new Set(res.data.map((m) => m.mangaId))
      setAddedIds(ids)
    }).catch(console.error)
  }, [])

  function handleSearch(e) {
    e.preventDefault()
    if (!query.trim()) return

    setLoading(true)
    setError(null)

    searchManga(query)
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

  function handleAdd(manga) {
    addToLibrary({
      mangaId: manga.id,
      title: manga.title,
      status: 'PLANNED',
      chaptersRead: 0,
      latestChapter: manga.latestChapter,
      coverUrl: manga.coverUrl,
      author: manga.author,
      artist: manga.artist,
      description: manga.description,
      seriesStatus: manga.status,
    })
      .then(() => {
        setAddedIds((prev) => new Set([...prev, manga.id]))
      })
      .catch((err) => {
        console.error(err)
      })
  }

  return (
    <div
      className="min-h-screen"
      style={{ background: theme.background }}
    >
      <Navbar activeMedia="manga" />

      <div className="p-5">
        {/* Header */}
        <div className="flex items-baseline gap-2.5 mb-4">
          <h1 className="text-[18px] font-medium text-[#e2e2f0] m-0">
            Search Manga
          </h1>
          <p className="text-[13px] text-[#555566] m-0">
            Find and add titles to your library
          </p>
        </div>

        {/* Search input */}
        <form onSubmit={handleSearch} className="flex gap-2 mb-6">
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search for manga..."
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

        {/* Loading */}
        {loading && (
          <div className="text-[13px] text-[#555566] text-center py-12">
            Searching…
          </div>
        )}

        {/* Error */}
        {!loading && error && (
          <div className="text-[13px] text-[#f87171] text-center py-12">
            {error}
          </div>
        )}

        {/* No results */}
        {!loading && !error && results.length === 0 && query && (
          <div className="text-[13px] text-[#555566] text-center py-12">
            No results found for "{query}"
          </div>
        )}

        {/* Results grid */}
        {!loading && results.length > 0 && (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
            {results.map((manga) => (
              <MediaCard
                key={manga.id}
                title={manga.title}
                creator={manga.author}
                seriesStatus={manga.status}
                coverUrl={manga.coverUrl}
                total={manga.latestChapter ?? null}
                theme={theme}
                icon="📖"
                isAdded={addedIds.has(manga.id)}
                onAdd={() => handleAdd(manga)}
                onClick={() => navigate(`/manga/library/${manga.id}`, { state: { from: 'search' } })}
              />
            ))}
          </div>
        )}

        {/* Already added indicator */}
        {addedIds.size > 0 && (
          <p className="text-[11px] text-[#555566] text-center mt-4">
            {addedIds.size} title{addedIds.size !== 1 ? 's' : ''} added —{' '}
            <span
              className="cursor-pointer"
              style={{ color: theme.accent }}
              onClick={() => navigate('/manga/library')}
            >
              go to library
            </span>
          </p>
        )}
      </div>
    </div>
  )
}

export default MangaSearchPage