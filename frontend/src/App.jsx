import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import MangaLibraryPage from './pages/manga/MangaLibraryPage'
import MangaSearchPage from './pages/manga/MangaSearchPage'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/manga/library" replace />} />
        <Route path="/manga/library" element={<MangaLibraryPage />} />
        <Route path="/manga/search" element={<MangaSearchPage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
