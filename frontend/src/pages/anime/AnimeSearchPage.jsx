import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import SearchPageLayout from '../../components/SearchPageLayout'
import SearchMediaCard from '../../components/SearchMediaCard'
import { searchAnime, addAnimeToLibrary, getAnimeLibrary, getWorksByProducer, searchProducers } from '../../api/animeApi'
import { normalizeSeriesStatus } from '../../utils/statusMapping'
import { themes } from '../../theme/themes'

function AnimeSearchPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const theme = themes.anime

  const [query, setQuery] = useState('')
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [addedIds, setAddedIds] = useState(new Set())

  const [creatorTab, setCreatorTab] = useState(false)
  const [creatorResults, setCreatorResults] = useState([])
  const [creatorLoading, setCreatorLoading] = useState(false)
  const [creatorName, setCreatorName] = useState('')
  const [studioMatches, setStudioMatches] = useState([])
  const [creatorHasNextPage, setCreatorHasNextPage] = useState(false)
  const [creatorPage, setCreatorPage] = useState(1)
  const [currentCreatorId, setCurrentCreatorId] = useState(null)

  useEffect(() => {
    getAnimeLibrary().then((res) => {
      const ids = new Set(res.data.map((a) => a.animeId))
      setAddedIds(ids)
    }).catch((err) => console.error('Failed to load library for added state:', err))
  }, [])

  useEffect(() => {
    const tab = searchParams.get('tab')
    const creatorId = searchParams.get('creatorId')
    const name = searchParams.get('creatorName')

    if (tab === 'creator' && creatorId) {
      setCreatorTab(true)
      setCreatorName(name ?? '')
      setCurrentCreatorId(creatorId)
      setCreatorLoading(true)
      getWorksByProducer(creatorId, 1, name)
        .then((res) => {
          setCreatorResults(res.data.items)
          setCreatorHasNextPage(res.data.hasNextPage)
          setCreatorPage(1)
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
    searchAnime(query)
      .then((res) => {
        setResults(res.data)
        setLoading(false)
      })
      .catch((err) => {
        console.error('Failed to search anime:', err)
        setError('Search failed. Is the backend running?')
        setLoading(false)
      })
  }

  function handleCreatorSearch(e) {
    e.preventDefault()
    if (!creatorName.trim()) return
    setCreatorLoading(true)
    setStudioMatches([])
    setCreatorResults([])
    setCreatorPage(1)
    searchProducers(creatorName)
      .then((res) => {
        const matches = res.data
        if (matches.length === 1) {
          setCurrentCreatorId(matches[0].id)
          return getWorksByProducer(matches[0].id, 1, matches[0].name)
            .then((worksRes) => {
              setCreatorResults(worksRes.data.items)
              setCreatorHasNextPage(worksRes.data.hasNextPage)
              setCreatorLoading(false)
            })
        }
        setStudioMatches(matches)
        setCreatorLoading(false)
      })
      .catch((err) => {
        console.error('Failed to search anime by creator:', err)
        setCreatorLoading(false)
      })
  }

  function handleCreatorMatchSelect(match) {
    setCreatorLoading(true)
    setStudioMatches([])
    setCurrentCreatorId(match.id)
    getWorksByProducer(match.id, 1, match.name)
      .then((res) => {
        setCreatorResults(res.data.items)
        setCreatorHasNextPage(res.data.hasNextPage)
        setCreatorName(match.name)
        setCreatorPage(1)
        setCreatorLoading(false)
      })
      .catch((err) => {
        console.error('Failed to load anime works for creator:', err)
        setCreatorLoading(false)
      })
  }

  function handleLoadMore() {
    const nextPage = creatorPage + 1
    setCreatorPage(nextPage)
    getWorksByProducer(currentCreatorId, nextPage, creatorName)
      .then((res) => {
        setCreatorResults((prev) => [...prev, ...res.data.items])
        setCreatorHasNextPage(res.data.hasNextPage)
      })
      .catch(console.error)
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
      malRating: anime.communityRating,
      studioId: anime.studioId ?? null,
    })
      .then(() => setAddedIds((prev) => new Set([...prev, anime.id])))
      .catch((err) => console.error('Failed to add anime to library:', err))
  }

  return (
    <SearchPageLayout
      activeMedia="anime"
      pageTitle="Search Anime"
      pageSubtitle="Find and add titles to your library"
      creatorTabLabel="By studio"
      titlePlaceholder="Search for anime..."
      creatorPlaceholder="Search by studio name..."
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
      creatorMatches={studioMatches}
      onCreatorSearch={handleCreatorSearch}
      onCreatorMatchSelect={handleCreatorMatchSelect}
      disambiguationLabel="Multiple studios found — select one"
      creatorHasNextPage={creatorHasNextPage}
      onLoadMore={handleLoadMore}
      renderCard={(item) => (
        <SearchMediaCard
          key={item.id}
          title={item.title}
          creator={item.studio}
          badge={item.status}
          coverUrl={item.coverUrl}
          theme={theme}
          icon="🎞️"
          isAdded={addedIds.has(item.id)}
          onAdd={() => handleAdd(item)}
          onClick={() => navigate(`/anime/library/${item.id}`, { state: { from: 'search' } })}
        />
      )}
      addedCount={addedIds.size}
      onGoToLibrary={() => navigate('/anime/library')}
    />
  )
}

export default AnimeSearchPage