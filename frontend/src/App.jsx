import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import MangaLibraryPage from './pages/manga/MangaLibraryPage'
import MangaSearchPage from './pages/manga/MangaSearchPage'
import MangaDetailPage from './pages/manga/MangaDetailPage'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/manga/library" replace />} />
        <Route path="/manga/library" element={<MangaLibraryPage />} />
        <Route path="/manga/search" element={<MangaSearchPage />} />
        <Route path="/manga/library/:mangaId" element={<MangaDetailPage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
