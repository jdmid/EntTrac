import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import SearchPageLayout from '../../components/SearchPageLayout'
import SearchMediaCard from '../../components/SearchMediaCard'
import { searchTv, addTvToLibrary, getTvLibrary, getTvDetails, getWorksByCreator, searchPeople } from '../../api/tvApi'
import { normalizeSeriesStatus } from '../../utils/statusMapping'
import { themes } from '../../theme/themes'

function TvSearchPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const theme = themes.tv

  const [query, setQuery] = useState('')
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [addedIds, setAddedIds] = useState(new Set())

  const [creatorTab, setCreatorTab] = useState(false)
  const [creatorResults, setCreatorResults] = useState([])
  const [creatorLoading, setCreatorLoading] = useState(false)
  const [creatorName, setCreatorName] = useState('')
  const [creatorMatches, setCreatorMatches] = useState([])

  useEffect(() => {
    getTvLibrary()
      .then((res) => {
        const ids = new Set(res.data.map((t) => t.tvId))
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
      setCreatorLoading(true)
      getWorksByCreator(creatorId)
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
    searchTv(query)
      .then((res) => {
        setResults(res.data)
        setLoading(false)
      })
      .catch((err) => {
        console.error('Failed to search shows:', err)
        setError('Search failed. Is the backend running?')
        setLoading(false)
      })
  }

  function handleCreatorSearch(e) {
    e.preventDefault()
    if (!creatorName.trim()) return
    setCreatorLoading(true)
    setCreatorMatches([])
    setCreatorResults([])
    searchPeople(creatorName)
      .then((res) => {
        const matches = res.data
        if (matches.length === 1) {
          return getWorksByCreator(matches[0].id)
            .then((worksRes) => {
              setCreatorResults(worksRes.data)
              setCreatorLoading(false)
            })
        }
        setCreatorMatches(matches)
        setCreatorLoading(false)
      })
      .catch((err) => {
        console.error('Failed to search TV shows by creator:', err)
        setCreatorLoading(false)
      })
  }

  function handleCreatorMatchSelect(match) {
    setCreatorLoading(true)
    setCreatorMatches([])
    getWorksByCreator(match.id)
      .then((res) => {
        setCreatorResults(res.data)
        setCreatorName(match.name)
        setCreatorLoading(false)
      })
      .catch((err) => {
        console.error('Failed to load TV show works for creator:', err)
        setCreatorLoading(false)
      })
  }

  async function handleAdd(show) {
    try {
      const detailRes = await getTvDetails(show.id)
      const full = detailRes.data
      await addTvToLibrary({
        tvId: full.id ?? show.id,
        title: full.title ?? show.title,
        status: 'PLANNED',
        episodesWatched: 0,
        currentSeason: 1,
        totalEpisodes: full.totalEpisodes ?? show.totalEpisodes,
        numberOfSeasons: full.numberOfSeasons ?? show.numberOfSeasons,
        seasonEpisodes: full.seasonEpisodes ?? show.seasonEpisodes,
        coverUrl: full.coverUrl ?? show.coverUrl,
        description: full.description ?? show.description,
        seriesStatus: normalizeSeriesStatus(full.status ?? show.status, 'tv'),
        network: full.network ?? show.network,
        genres: full.genres ?? show.genres,
        firstAirYear: full.firstAirYear ?? show.firstAirYear,
        seriesType: full.seriesType ?? show.seriesType,
        tmdbRating: full.tmdbRating ?? show.tmdbRating,
        nextEpisodeDate: full.nextEpisodeDate,
        creatorName: full.creatorName ?? null,
        creatorId: full.creatorId ?? null,
      })
      setAddedIds((prev) => new Set([...prev, show.id]))
    } catch (err) {
      console.error('Failed to add TV show to library:', err)
    }
  }

  return (
    <SearchPageLayout
      activeMedia="tv"
      pageTitle="Search TV Shows"
      pageSubtitle="Find and add titles to your library"
      creatorTabLabel="By creator"
      titlePlaceholder="Search for TV shows..."
      creatorPlaceholder="Search by creator name..."
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
      creatorMatches={creatorMatches}
      onCreatorSearch={handleCreatorSearch}
      onCreatorMatchSelect={handleCreatorMatchSelect}
      disambiguationLabel="Multiple creators found — select one"
      renderCard={(item) => (
        <SearchMediaCard
          key={item.id}
          title={item.title}
          creator={(() => {
            const parts = [item.seriesType, item.firstAirYear].filter(Boolean)
            return parts.length > 0 ? parts.join(' · ') : null
          })()}
          badge={item.status}
          coverUrl={item.coverUrl}
          theme={theme}
          icon="📺"
          isAdded={addedIds.has(item.id)}
          onAdd={() => handleAdd(item)}
          onClick={() => navigate(`/tv/library/${item.id}`, { state: { from: 'search' } })}
        />
      )}
      addedCount={addedIds.size}
      onGoToLibrary={() => navigate('/tv/library')}
    />
  )
}

export default TvSearchPage