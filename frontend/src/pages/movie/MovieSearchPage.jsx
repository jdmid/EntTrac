import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import SearchPageLayout from '../../components/SearchPageLayout'
import SearchMediaCard from '../../components/SearchMediaCard'
import { searchMovies, addMovieToLibrary, getMovieLibrary, getMovieDetails, getWorksByDirector, searchPeople } from '../../api/movieApi'
import { normalizeSeriesStatus } from '../../utils/statusMapping'
import { themes } from '../../theme/themes'

function MovieSearchPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const theme = themes.movie

  const [query, setQuery] = useState('')
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [addedIds, setAddedIds] = useState(new Set())

  const [creatorTab, setCreatorTab] = useState(false)
  const [creatorResults, setCreatorResults] = useState([])
  const [creatorLoading, setCreatorLoading] = useState(false)
  const [creatorName, setCreatorName] = useState('')
  const [directorMatches, setDirectorMatches] = useState([])

  useEffect(() => {
    getMovieLibrary()
      .then((res) => {
        const ids = new Set(res.data.map((m) => m.movieId))
        setAddedIds(ids)
      })
      .catch(console.error)
  }, [])

  useEffect(() => {
    const tab = searchParams.get('tab')
    const creatorId = searchParams.get('creatorId')
    const name = searchParams.get('creatorName')

    if (tab === 'creator' && creatorId) {
      setCreatorTab(true)
      setCreatorName(name ?? '')
      setCreatorLoading(true)
      getWorksByDirector(creatorId)
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

  function handleCreatorSearch(e) {
    e.preventDefault()
    if (!creatorName.trim()) return
    setCreatorLoading(true)
    setDirectorMatches([])
    setCreatorResults([])
    searchPeople(creatorName)
      .then((res) => {
        const matches = res.data
        if (matches.length === 1) {
          return getWorksByDirector(matches[0].id)
            .then((worksRes) => {
              setCreatorResults(worksRes.data)
              setCreatorLoading(false)
            })
        }
        setDirectorMatches(matches)
        setCreatorLoading(false)
      })
      .catch(() => setCreatorLoading(false))
  }

  function handleCreatorMatchSelect(match) {
    setCreatorLoading(true)
    setDirectorMatches([])
    getWorksByDirector(match.id)
      .then((res) => {
        setCreatorResults(res.data)
        setCreatorName(match.name)
        setCreatorLoading(false)
      })
      .catch(() => setCreatorLoading(false))
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
        directorId: full.directorId ?? movie.directorId ?? null,
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
    <SearchPageLayout
      activeMedia="movie"
      pageTitle="Search Movies"
      pageSubtitle="Find and add titles to your library"
      creatorTabLabel="By director"
      titlePlaceholder="Search for movies..."
      creatorPlaceholder="Search by director name..."
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
      creatorMatches={directorMatches}
      onCreatorSearch={handleCreatorSearch}
      onCreatorMatchSelect={handleCreatorMatchSelect}
      disambiguationLabel="Multiple directors found — select one"
      renderCard={(item) => (
        <SearchMediaCard
          key={item.id}
          title={item.title}
          creator={item.releaseYear}
          badge={item.status}
          coverUrl={item.coverUrl}
          theme={theme}
          icon="🎬"
          isAdded={addedIds.has(item.id)}
          onAdd={() => handleAdd(item)}
          onClick={() => navigate(`/movie/library/${item.id}`, { state: { from: 'search' } })}
        />
      )}
      addedCount={addedIds.size}
      onGoToLibrary={() => navigate('/movie/library')}
    />
  )
}

export default MovieSearchPage