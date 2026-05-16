import { useNavigate, useLocation } from 'react-router-dom'
import { themes } from '../theme/themes'
import { Settings } from 'lucide-react'

function Navbar({ activeMedia = 'manga' }) {
  const navigate = useNavigate()
  const location = useLocation()
  const theme = themes[activeMedia]

  const isLibrary = location.pathname.includes('/library')
  const isSearch = location.pathname.includes('/search')

  return (
    <div>
      {/* Top bar */}
      <div style={{ background: theme.topBar }} className="border-b border-white/5 px-5 h-12 flex items-center">
        <span className="text-sm font-medium text-white/90 mr-5">
          Ent<span style={{ color: theme.accent }}>Trac</span>
        </span>

        {/* Media type tabs */}
        <button
          onClick={() => navigate('/manga/library')}
          className="h-12 px-4 text-sm border-b-2 transition-colors"
          style={
            activeMedia === 'manga'
              ? { color: theme.accent, borderColor: theme.accent }
              : { color: '#777788', borderColor: 'transparent' }
          }
        >
          Manga
        </button>

        <button
          onClick={() => navigate('/anime/library')}
          className="h-12 px-4 text-sm border-b-2 transition-colors"
          style={
            activeMedia === 'anime'
              ? { color: theme.accent, borderColor: theme.accent }
              : { color: '#777788', borderColor: 'transparent' }
          }
        >
          Anime
        </button>

        <button className="h-12 px-4 text-sm text-white/20 border-b-2 border-transparent">
          +
        </button>

        <button className="ml-auto text-white/25 hover:text-white/50 transition-colors">
          <Settings size={16} />
        </button>
      </div>

      {/* Sub nav */}
      <div style={{ background: theme.subNav }} className="border-b border-white/5 px-5 h-9 flex items-center">
        <button
          onClick={() => navigate(`/${activeMedia}/library`)}
          className="h-9 px-3 text-xs border-b-2 transition-colors"
          style={
            isLibrary
              ? { color: '#e2e2f0', borderColor: '#e2e2f0' }
              : { color: '#555566', borderColor: 'transparent' }
          }
        >
          Library
        </button>
        <button
          onClick={() => navigate(`/${activeMedia}/search`)}
          className="h-9 px-3 text-xs border-b-2 transition-colors"
          style={
            isSearch
              ? { color: '#e2e2f0', borderColor: '#e2e2f0' }
              : { color: '#555566', borderColor: 'transparent' }
          }
        >
          Search
        </button>
      </div>
    </div>
  )
}

export default Navbar