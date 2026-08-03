import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import LibraryPageLayout from '../../components/LibraryPageLayout'
import LibraryMediaCard from '../../components/LibraryMediaCard'
import { getBookLibrary } from '../../api/bookApi'
import { themes } from '../../theme/themes'
import { SERIES_STATUS_FILTERS, SORT_OPTIONS } from '../../utils/statusMapping'
import { sortBooks } from '../../utils/sortUtils'
import { useMediaLibrary } from '../../hooks/useMediaLibrary'


const BOOK_STATUS_FILTERS = [
  { value: 'ALL',       label: 'All' },
  { value: 'CONSUMING', label: 'Reading' },
  { value: 'PLANNED',   label: 'Plan to Read' },
  { value: 'FINISHED',  label: 'Finished' },
  { value: 'DROPPED',   label: 'Dropped' },
]

function BookLibraryPage() {
  const navigate = useNavigate()

  const [statusFilter, setStatusFilter] = useState('ALL')
  const [seriesStatusFilter, setSeriesStatusFilter] = useState('ALL')
  const [sortBy, setSortBy] = useState('RECENTLY_ADDED')

  const { library, loading, error } = useMediaLibrary({ getLibrary: getBookLibrary })

  return (
    <LibraryPageLayout
      activeMedia="book"
      pageTitle="My Book Library"
      library={library}
      loading={loading}
      error={error}
      emptyMessage="Your library is empty. Search for books to add!"
      emptyFilterMessage="No books match the current filters."
      statusFilters={BOOK_STATUS_FILTERS}
      statusFilter={statusFilter}
      onStatusChange={setStatusFilter}
      seriesStatusFilters={SERIES_STATUS_FILTERS.book}
      seriesStatusFilter={seriesStatusFilter}
      onSeriesStatusChange={setSeriesStatusFilter}
      sortOptions={SORT_OPTIONS.book}
      sortBy={sortBy}
      onSortChange={setSortBy}
      sortFn={sortBooks}
      renderCard={(book) => (
        <LibraryMediaCard
          key={book.bookId}
          title={book.title}
          creator={book.authors?.map((a) => a.name).join(', ')}
          score={book.score}
          status={book.status}
          coverUrl={book.coverUrl}
          theme={themes.book}
          icon="📚"
          medium="book"
          progress={book.currentChapter ?? book.currentPage ?? null}
          progressLabel={
              book.currentChapter != null ? 'Ch.' :
              book.currentPage != null ? 'p.' :
              null
          }
          onClick={() => navigate(`/book/library/${book.bookId}`)}
        />
      )}
    />
  )
}

export default BookLibraryPage