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
import LoginPage from './pages/auth/LoginPage'
import SettingsPage from './pages/settings/SettingsPage'
import OnboardingChoicePage from './pages/auth/OnboardingChoicePage'
import OnboardingPreferencesPage from './pages/auth/OnboardingPreferencesPage'
import { useSettings } from './context/SettingsContext'
import { useAuth } from './context/AuthContext'

function TabGuard({ tabId, children }) {
  const { tabs, firstVisibleTab } = useSettings()
  const tab = tabs.find((t) => t.id === tabId)
  if (tab?.visible) return children
  return <Navigate to={`/${firstVisibleTab()}/library`} replace />
}

function RequireAuth({ children }) {
  const { status } = useAuth()

  if (status === 'loading') {
    return <div className="flex min-h-screen items-center justify-center">Loading…</div>
  }
  if (status === 'unauthenticated') {
    return <Navigate to="/login" replace />
  }
  return children
}

function App() {
  const { firstVisibleTab } = useSettings()

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/onboarding" element={<RequireAuth><OnboardingChoicePage /></RequireAuth>} />
        <Route path="/onboarding/preferences" element={<RequireAuth><OnboardingPreferencesPage /></RequireAuth>} />

        <Route path="/" element={<RequireAuth><Navigate to={`/${firstVisibleTab()}/library`} replace /></RequireAuth>} />

        <Route path="/manga/library" element={<RequireAuth><TabGuard tabId="manga"><MangaLibraryPage /></TabGuard></RequireAuth>} />
        <Route path="/manga/search" element={<RequireAuth><TabGuard tabId="manga"><MangaSearchPage /></TabGuard></RequireAuth>} />
        <Route path="/manga/library/:mangaId" element={<RequireAuth><TabGuard tabId="manga"><MangaDetailPage /></TabGuard></RequireAuth>} />

        <Route path="/anime/library" element={<RequireAuth><TabGuard tabId="anime"><AnimeLibraryPage /></TabGuard></RequireAuth>} />
        <Route path="/anime/search" element={<RequireAuth><TabGuard tabId="anime"><AnimeSearchPage /></TabGuard></RequireAuth>} />
        <Route path="/anime/library/:animeId" element={<RequireAuth><TabGuard tabId="anime"><AnimeDetailPage /></TabGuard></RequireAuth>} />

        <Route path="/tv/library" element={<RequireAuth><TabGuard tabId="tv"><TvLibraryPage /></TabGuard></RequireAuth>} />
        <Route path="/tv/search" element={<RequireAuth><TabGuard tabId="tv"><TvSearchPage /></TabGuard></RequireAuth>} />
        <Route path="/tv/library/:tvId" element={<RequireAuth><TabGuard tabId="tv"><TvDetailPage /></TabGuard></RequireAuth>} />

        <Route path="/movie/library" element={<RequireAuth><TabGuard tabId="movie"><MovieLibraryPage /></TabGuard></RequireAuth>} />
        <Route path="/movie/search" element={<RequireAuth><TabGuard tabId="movie"><MovieSearchPage /></TabGuard></RequireAuth>} />
        <Route path="/movie/library/:movieId" element={<RequireAuth><TabGuard tabId="movie"><MovieDetailPage /></TabGuard></RequireAuth>} />

        <Route path="/book/library" element={<RequireAuth><TabGuard tabId="book"><BookLibraryPage /></TabGuard></RequireAuth>} />
        <Route path="/book/search" element={<RequireAuth><TabGuard tabId="book"><BookSearchPage /></TabGuard></RequireAuth>} />
        <Route path="/book/library/:bookId" element={<RequireAuth><TabGuard tabId="book"><BookDetailPage /></TabGuard></RequireAuth>} />

        <Route path="/game/library" element={<RequireAuth><TabGuard tabId="game"><GameLibraryPage /></TabGuard></RequireAuth>} />
        <Route path="/game/search" element={<RequireAuth><TabGuard tabId="game"><GameSearchPage /></TabGuard></RequireAuth>} />
        <Route path="/game/library/:gameId" element={<RequireAuth><TabGuard tabId="game"><GameDetailPage /></TabGuard></RequireAuth>} />
        <Route path="/game/dlc/:dlcId" element={<RequireAuth><TabGuard tabId="game"><GameDetailPage /></TabGuard></RequireAuth>} />

        <Route path="/settings" element={<RequireAuth><SettingsPage /></RequireAuth>} />
      </Routes>
    </BrowserRouter>
  )
}

export default App