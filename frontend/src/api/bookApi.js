import client from './client'

export const searchBooks = (query) =>
  client.get('/books/search', { params: { q: query } })

export const getBookLibrary = () =>
  client.get('/books/library')

export const getBook = (bookId) =>
  client.get(`/books/library/${bookId}`)

export const addBookToLibrary = (bookItem) =>
  client.post('/books/library', bookItem)

export const updateBookProgress = (bookId, currentChapter, currentPage) =>
  client.patch(`/books/library/${bookId}/progress`, null, {
    params: {
      ...(currentChapter != null && { currentChapter }),
      ...(currentPage != null && { currentPage }),
    },
  })

export const updateBookScore = (bookId, score) =>
  client.patch(`/books/library/${bookId}/score`, null, {
    params: { score },
  })

export const updateBookStatus = (bookId, status) =>
  client.patch(`/books/library/${bookId}/status`, null, {
    params: { status },
  })

export const updateBookNotes = (bookId, notes) =>
  client.patch(`/books/library/${bookId}/notes`, notes, {
    headers: { 'Content-Type': 'text/plain' },
  })

export const removeBookFromLibrary = (bookId) =>
  client.delete(`/books/library/${bookId}`)

export const getBookDetails = (bookId) =>
  client.get(`/books/details/${bookId}`)

export const getWorksByAuthor = (authorId) =>
  client.get(`/books/creator/${authorId}`)

export const searchAuthors = (name) =>
  client.get('/books/author-search', { params: { name } })

export const resetBookProgress = (bookId) =>
  client.delete(`/books/library/${bookId}/progress`)