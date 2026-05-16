import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import MangaLibraryPage from './pages/manga/MangaLibraryPage'
import MangaSearchPage from './pages/manga/MangaSearchPage'
import MangaDetailPage from './pages/manga/MangaDetailPage'
import AnimeLibraryPage from './pages/anime/AnimeLibraryPage'
import AnimeSearchPage from './pages/anime/AnimeSearchPage'
import AnimeDetailPage from './pages/anime/AnimeDetailPage'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/manga/library" replace />} />
        <Route path="/manga/library" element={<MangaLibraryPage />} />
        <Route path="/manga/search" element={<MangaSearchPage />} />
        <Route path="/manga/library/:mangaId" element={<MangaDetailPage />} />
        <Route path="/anime/library" element={<AnimeLibraryPage />} />
        <Route path="/anime/search" element={<AnimeSearchPage />} />
        <Route path="/anime/library/:animeId" element={<AnimeDetailPage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App