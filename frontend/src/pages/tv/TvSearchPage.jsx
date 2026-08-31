import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import SearchPageLayout from '../../components/SearchPageLayout'
import SearchMediaCard from '../../components/SearchMediaCard'
import { searchTv, addTvToLibrary, getTvLibrary, getTvDetails, getWorksByCreator, searchPeople, getWorksByStudio, searchStudios } from '../../api/tvApi'
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

  const [studioTab, setStudioTab] = useState(false)
  const [studioResults, setStudioResults] = useState([])
  const [studioLoading, setStudioLoading] = useState(false)
  const [studioName, setStudioName] = useState('')
  const [studioMatches, setStudioMatches] = useState([])

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

    const studioId = searchParams.get('studioId')
    const sName = searchParams.get('studioName')

    if (tab === 'studio' && studioId) {
      setStudioTab(true)
      setStudioName(sName ?? '')
      setStudioLoading(true)
      getWorksByStudio(studioId)
        .then((res) => {
          setStudioResults(res.data)
          setStudioLoading(false)
        })
        .catch(() => setStudioLoading(false))
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

  function handleStudioSearch(e) {
    e.preventDefault()
    if (!studioName.trim()) return
    setStudioLoading(true)
    setStudioMatches([])
    setStudioResults([])
    searchStudios(studioName)
      .then((res) => {
        const matches = res.data
        if (matches.length === 1) {
          return getWorksByStudio(matches[0].id)
            .then((worksRes) => {
              setStudioResults(worksRes.data)
              setStudioLoading(false)
            })
        }
        setStudioMatches(matches)
        setStudioLoading(false)
      })
      .catch((err) => {
        console.error('Failed to search TV shows by studio:', err)
        setStudioLoading(false)
      })
  }

  function handleStudioMatchSelect(match) {
    setStudioLoading(true)
    setStudioMatches([])
    getWorksByStudio(match.id)
      .then((res) => {
        setStudioResults(res.data)
        setStudioName(match.name)
        setStudioLoading(false)
      })
      .catch((err) => {
        console.error('Failed to load TV show works for studio:', err)
        setStudioLoading(false)
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
        studio: full.studio ?? null,
        studioId: full.studioId ?? null,
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
      studioTabLabel="By studio"
      titlePlaceholder="Search for TV shows..."
      creatorPlaceholder="Search by creator name..."
      studioPlaceholder="Search by studio name..."
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
      studioTab={studioTab}
      onStudioTabChange={setStudioTab}
      studioName={studioName}
      onStudioNameChange={setStudioName}
      studioResults={studioResults}
      studioLoading={studioLoading}
      studioMatches={studioMatches}
      onStudioSearch={handleStudioSearch}
      onStudioMatchSelect={handleStudioMatchSelect}
      studioDisambiguationLabel="Multiple studios found — select one"
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