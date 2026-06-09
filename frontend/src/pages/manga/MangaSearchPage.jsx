import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import SearchPageLayout from '../../components/SearchPageLayout'
import SearchMediaCard from '../../components/SearchMediaCard'
import { searchManga, addToLibrary, getLibrary, getWorksByAuthor, searchAuthors } from '../../api/mangaApi'
import { normalizeSeriesStatus } from '../../utils/statusMapping'
import { themes } from '../../theme/themes'

function MangaSearchPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const theme = themes.manga

  const [query, setQuery] = useState('')
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [addedIds, setAddedIds] = useState(new Set())

  const [creatorTab, setCreatorTab] = useState(false)
  const [creatorResults, setCreatorResults] = useState([])
  const [creatorLoading, setCreatorLoading] = useState(false)
  const [creatorName, setCreatorName] = useState('')
  const [authorMatches, setAuthorMatches] = useState([])

  useEffect(() => {
    getLibrary().then((res) => {
      const ids = new Set(res.data.map((m) => m.mangaId))
      setAddedIds(ids)
    }).catch(console.error)
  }, [])

  useEffect(() => {
    const tab = searchParams.get('tab')
    const creatorId = searchParams.get('creatorId')
    const name = searchParams.get('creatorName')

    if (tab === 'creator' && creatorId) {
      setCreatorTab(true)
      setCreatorName(name ?? '')
      setCreatorLoading(true)
      getWorksByAuthor(creatorId)
        .then((res) => {
          setCreatorResults(res.data)
          setCreatorLoading(false)
        })
        .catch(() => setCreatorLoading(false))
    }
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

  function handleCreatorSearch(e) {
    e.preventDefault()
    if (!creatorName.trim()) return
    setCreatorLoading(true)
    setAuthorMatches([])
    setCreatorResults([])
    searchAuthors(creatorName)
      .then((res) => {
        const matches = res.data
        if (matches.length === 1) {
          return getWorksByAuthor(matches[0].id)
            .then((worksRes) => {
              setCreatorResults(worksRes.data)
              setCreatorLoading(false)
            })
        }
        setAuthorMatches(matches)
        setCreatorLoading(false)
      })
      .catch(() => setCreatorLoading(false))
  }

  function handleCreatorMatchSelect(match) {
    setCreatorLoading(true)
    setAuthorMatches([])
    getWorksByAuthor(match.id)
      .then((res) => {
        setCreatorResults(res.data)
        setCreatorName(match.name)
        setCreatorLoading(false)
      })
      .catch(() => setCreatorLoading(false))
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
      seriesStatus: normalizeSeriesStatus(manga.status, 'manga'),
      authorId: manga.authorId ?? null,
      artistId: manga.artistId ?? null,
    })
      .then(() => setAddedIds((prev) => new Set([...prev, manga.id])))
      .catch(console.error)
  }

  return (
    <SearchPageLayout
      activeMedia="manga"
      pageTitle="Search Manga"
      pageSubtitle="Find and add titles to your library"
      creatorTabLabel="By creator"
      titlePlaceholder="Search for manga..."
      creatorPlaceholder="Search by author or artist name..."
      results={results}
      loading={loading}
      error={error}
      query={query}
      onQueryChange={setQuery}
      onTitleSearch={handleSearch}
      creatorTab={creatorTab}
      onCreatorTabChange={setCreatorTab}
      creatorName={creatorName}
      onCreatorNameChange={setCreatorName}
      creatorResults={creatorResults}
      creatorLoading={creatorLoading}
      creatorMatches={authorMatches}
      onCreatorSearch={handleCreatorSearch}
      onCreatorMatchSelect={handleCreatorMatchSelect}
      disambiguationLabel="Multiple authors found — select one"
      renderCard={(item) => (
        <SearchMediaCard
          key={item.id}
          title={item.title}
          creator={item.author}
          seriesStatus={item.status}
          coverUrl={item.coverUrl}
          theme={theme}
          icon="📖"
          isAdded={addedIds.has(item.id)}
          onAdd={() => handleAdd(item)}
          onClick={() => navigate(`/manga/library/${item.id}`, { state: { from: 'search' } })}
        />
      )}
      addedCount={addedIds.size}
      onGoToLibrary={() => navigate('/manga/library')}
    />
  )
}

export default MangaSearchPage