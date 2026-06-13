import { useState, useEffect } from 'react'
import { useParams, useNavigate, useLocation } from 'react-router-dom'
import DetailPageLayout from '../../components/DetailPageLayout'
import BookProgressSection from '../../components/BookProgressSection'
import {
  getBook, getBookDetails, updateBookScore, updateBookStatus,
  removeBookFromLibrary, addBookToLibrary, updateBookNotes,
  updateBookProgress, resetBookProgress
} from '../../api/bookApi'
import { themes } from '../../theme/themes'

function BookDetailPage() {
  const { bookId } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const theme = themes.book
  const fromSearch = location.state?.from === 'search'

  const [book, setBook] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [score, setScore] = useState(null)
  const [inLibrary, setInLibrary] = useState(true)

  useEffect(() => {
    getBook(bookId)
        .then((res) => {
        setBook(res.data)
        setScore(res.data.score ?? null)
        setLoading(false)
        getBookDetails(bookId)
            .then((details) => {
            if (details.data?.description) {
                setBook((prev) => ({ ...prev, description: details.data.description }))
            }
            })
            .catch((err) => console.error('Details fetch failed:', err))
        })
        .catch(() => {
        // Use search result data from navigation state if available
        const stateItem = location.state?.item
        if (stateItem) {
            setBook(stateItem)
            setInLibrary(false)
            setLoading(false)
            // Still fetch description from works endpoint
            getBookDetails(bookId)
            .then((details) => {
                if (details.data?.description) {
                setBook((prev) => ({ ...prev, description: details.data.description }))
                }
            })
            .catch((err) => console.error('Details fetch failed:', err))
            return
        }
        getBookDetails(bookId)
            .then((res) => {
            setBook(res.data)
            setInLibrary(false)
            setLoading(false)
            })
            .catch(() => {
            setError('Failed to load book.')
            setLoading(false)
            })
        })
    }, [bookId])

  if (loading) {
    return (
      <div className="min-h-screen" style={{ background: theme.background }}>
        <div className="text-[13px] text-[#555566] text-center py-12">
          Loading…
        </div>
      </div>
    )
  }

  if (error || !book) {
    return (
      <div className="min-h-screen" style={{ background: theme.background }}>
        <div className="text-[13px] text-[#f87171] text-center py-12">
          {error ?? 'Book not found.'}
        </div>
      </div>
    )
  }

  const authors = book.authors ?? []

  return (
    <DetailPageLayout
      activeMedia="book"
      fromSearch={fromSearch}
      backPath={fromSearch ? '/book/search' : '/book/library'}
      title={book.title}
      item={book}
      inLibrary={inLibrary}
      score={score}
      theme={theme}
      icon="📚"
      metaLine={
        <div>
          {book.firstPublishYear && (
            <p className="text-[12px] text-[#555566] m-0 mb-1">
              {book.firstPublishYear}
              {book.genres ? ` · ${book.genres}` : ''}
            </p>
          )}
          {authors.length > 0 && (
            <p className="text-[12px] text-[#555566] m-0 mb-3">
              By{' '}
              {authors.map((author, index) => (
                <span key={author.id ?? index}>
                  {index > 0 && <span> · </span>}
                  <span
                    className="cursor-pointer inline-flex items-center gap-1"
                    style={{ color: theme.accent }}
                    onClick={() => navigate(
                      `/book/search?tab=creator&creatorId=${author.id}&creatorName=${encodeURIComponent(author.name)}`
                    )}
                  >
                    {author.name}
                    <svg width="10" height="10" viewBox="0 0 24 24" fill="none"
                      stroke="currentColor" strokeWidth="2" strokeLinecap="round"
                      strokeLinejoin="round" aria-hidden="true">
                      <path d="M18 13v6a2 2 0 01-2 2H5a2 2 0 01-2-2V8a2 2 0 012-2h6"/>
                      <polyline points="15 3 21 3 21 9"/>
                      <line x1="10" y1="14" x2="21" y2="3"/>
                    </svg>
                  </span>
                </span>
              ))}
            </p>
          )}
        </div>
      }
      progressSection={
        <BookProgressSection
            currentChapter={book.currentChapter}
            currentPage={book.currentPage}
            theme={theme}
            onUpdate={(chapter, page) =>
            updateBookProgress(bookId, chapter, page).then((res) => setBook(res.data))
            }
            onReset={() =>
            resetBookProgress(bookId).then((res) => setBook(res.data))
            }
        />
        }
      notesProgressLabel={
        book.currentChapter != null ? 'Ch.' : book.currentPage != null ? 'p.' : null
      }
      onRemove={() =>
        removeBookFromLibrary(bookId).then(() =>
          navigate(fromSearch ? '/book/search' : '/book/library')
        )
      }
      onAdd={() =>
        addBookToLibrary({
          bookId: book.id,
          title: book.title,
          status: 'PLANNED',
          coverUrl: book.coverUrl,
          description: book.description,
          authors: book.authors ?? [],
          firstPublishYear: book.firstPublishYear ?? null,
          genres: book.genres ?? null,
        }).then(() => setInLibrary(true))
      }
      onScoreSave={(n) =>
        updateBookScore(bookId, n).then((res) => {
          setBook(res.data)
          setScore(n)
        })
      }
      onStatusChange={(s) =>
        updateBookStatus(bookId, s).then((res) => setBook(res.data))
      }
      onNotesSave={(notes) =>
        updateBookNotes(bookId, notes).then((res) => setBook(res.data))
      }
    />
  )
}

export default BookDetailPage