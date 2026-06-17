import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import MangaLibraryPage from './pages/manga/MangaLibraryPage'
import MangaSearchPage from './pages/manga/MangaSearchPage'
import MangaDetailPage from './pages/manga/MangaDetailPage'
import AnimeLibraryPage from './pages/anime/AnimeLibraryPage'
import AnimeSearchPage from './pages/anime/AnimeSearchPage'
import AnimeDetailPage from './pages/anime/AnimeDetailPage'
import TvLibraryPage from './pages/tv/TvLibraryPage'
import TvSearchPage from './pages/tv/TvSearchPage'
import TvDetailPage from './pages/tv/TvDetailPage'
import MovieLibraryPage from './pages/movie/MovieLibraryPage'
import MovieSearchPage from './pages/movie/MovieSearchPage'
import MovieDetailPage from './pages/movie/MovieDetailPage'
import GameLibraryPage from './pages/game/GameLibraryPage'
import GameSearchPage from './pages/game/GameSearchPage'
import GameDetailPage from './pages/game/GameDetailPage'
import BookLibraryPage from './pages/book/BookLibraryPage'
import BookSearchPage from './pages/book/BookSearchPage'
import BookDetailPage from './pages/book/BookDetailPage'
import SettingsPage from './pages/settings/SettingsPage'
import { useSettings } from './context/SettingsContext'

function TabGuard({ tabId, children }) {
  const { tabs, firstVisibleTab } = useSettings()
  const tab = tabs.find((t) => t.id === tabId)
  if (tab?.visible) return children
  return <Navigate to={`/${firstVisibleTab()}/library`} replace />
}

function App() {
  const { firstVisibleTab } = useSettings()

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to={`/${firstVisibleTab()}/library`} replace />} />

        <Route path="/manga/library" element={<TabGuard tabId="manga"><MangaLibraryPage /></TabGuard>} />
        <Route path="/manga/search" element={<TabGuard tabId="manga"><MangaSearchPage /></TabGuard>} />
        <Route path="/manga/library/:mangaId" element={<TabGuard tabId="manga"><MangaDetailPage /></TabGuard>} />

        <Route path="/anime/library" element={<TabGuard tabId="anime"><AnimeLibraryPage /></TabGuard>} />
        <Route path="/anime/search" element={<TabGuard tabId="anime"><AnimeSearchPage /></TabGuard>} />
        <Route path="/anime/library/:animeId" element={<TabGuard tabId="anime"><AnimeDetailPage /></TabGuard>} />

        <Route path="/tv/library" element={<TabGuard tabId="tv"><TvLibraryPage /></TabGuard>} />
        <Route path="/tv/search" element={<TabGuard tabId="tv"><TvSearchPage /></TabGuard>} />
        <Route path="/tv/library/:tvId" element={<TabGuard tabId="tv"><TvDetailPage /></TabGuard>} />

        <Route path="/movie/library" element={<TabGuard tabId="movie"><MovieLibraryPage /></TabGuard>} />
        <Route path="/movie/search" element={<TabGuard tabId="movie"><MovieSearchPage /></TabGuard>} />
        <Route path="/movie/library/:movieId" element={<TabGuard tabId="movie"><MovieDetailPage /></TabGuard>} />

        <Route path="/book/library" element={<TabGuard tabId="book"><BookLibraryPage /></TabGuard>} />
        <Route path="/book/search" element={<TabGuard tabId="book"><BookSearchPage /></TabGuard>} />
        <Route path="/book/library/:bookId" element={<TabGuard tabId="book"><BookDetailPage /></TabGuard>} />

        <Route path="/game/library" element={<TabGuard tabId="game"><GameLibraryPage /></TabGuard>} />
        <Route path="/game/search" element={<TabGuard tabId="game"><GameSearchPage /></TabGuard>} />
        <Route path="/game/library/:gameId" element={<TabGuard tabId="game"><GameDetailPage /></TabGuard>} />
        <Route path="/game/dlc/:dlcId" element={<TabGuard tabId="game"><GameDetailPage /></TabGuard>} />
        
        <Route path="/settings" element={<SettingsPage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App