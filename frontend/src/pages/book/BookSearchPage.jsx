import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import SearchPageLayout from '../../components/SearchPageLayout'
import SearchMediaCard from '../../components/SearchMediaCard'
import { searchBooks, addBookToLibrary, getBookLibrary, getWorksByAuthor, searchAuthors } from '../../api/bookApi'
import { themes } from '../../theme/themes'

function BookSearchPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const theme = themes.book

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
    getBookLibrary().then((res) => {
      const ids = new Set(res.data.map((b) => b.bookId))
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
    searchBooks(query)
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

  function handleAdd(book) {
    addBookToLibrary({
      bookId: book.id,
      title: book.title,
      status: 'PLANNED',
      coverUrl: book.coverUrl,
      description: book.description,
      authors: book.authors ?? [],
      firstPublishYear: book.firstPublishYear ?? null,
      genres: book.genres ?? null,
    })
      .then(() => setAddedIds((prev) => new Set([...prev, book.id])))
      .catch(console.error)
  }

  return (
    <SearchPageLayout
      activeMedia="book"
      pageTitle="Search Books"
      pageSubtitle="Find and add titles to your library"
      creatorTabLabel="By author"
      titlePlaceholder="Search for books..."
      creatorPlaceholder="Search by author name..."
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
          creator={item.authors?.map((a) => a.name).join(', ')}
          badge={item.firstPublishYear}
          coverUrl={item.coverUrl}
          theme={theme}
          icon="📚"
          isAdded={addedIds.has(item.id)}
          onAdd={() => handleAdd(item)}
          onClick={() => navigate(`/book/library/${item.id}`, { state: { from: 'search', item } })}        />
      )}
      addedCount={addedIds.size}
      onGoToLibrary={() => navigate('/book/library')}
    />
  )
}

export default BookSearchPage