import { useNavigate, useLocation } from 'react-router-dom'
import { themes } from '../theme/themes'
import { useSettings } from '../context/SettingsContext'

function Navbar({ activeMedia = 'manga' }) {
  const navigate = useNavigate()
  const location = useLocation()
  const { tabs } = useSettings()

  const isSettings = location.pathname === '/settings'
  const isLibrary = location.pathname.includes('/library')
  const isSearch = location.pathname.includes('/search')

  // Use brand theme when on settings, otherwise use active media theme
  const theme = isSettings ? themes.brand : themes[activeMedia]

  const visibleTabs = tabs.filter((t) => t.visible)

  return (
    <div>
      <div
        className="border-b border-white/5 px-5 h-12 flex items-center"
        style={{ background: theme.topBar }}
      >
        <span className="text-sm font-medium text-white/90 mr-5">
          Ent<span style={{ color: theme.accent }}>Trac</span>
        </span>

        {visibleTabs.map((tab) => (
          <button
            key={tab.id}
            onClick={() => navigate(`/${tab.id}/library`)}
            className="h-12 px-4 text-sm border-b-2 transition-colors"
            style={
              !isSettings && activeMedia === tab.id
                ? { color: themes[tab.id].accent, borderColor: themes[tab.id].accent }
                : { color: '#777788', borderColor: 'transparent' }
            }
          >
            {tab.label}
          </button>
        ))}

        <button
          onClick={() => navigate('/settings')}
          className="ml-auto h-12 flex items-center gap-1.5 px-3 text-sm border-b-2 transition-colors"
          style={
            isSettings
              ? {
                  color: themes.brand.accent,
                  borderColor: themes.brand.accent,
                  background: themes.brand.accentBg,
                }
              : {
                  color: '#555566',
                  borderColor: 'transparent',
                  background: 'transparent',
                }
          }
        >
          <svg
            width="15"
            height="15"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
          >
            <path d="M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z"/>
            <circle cx="12" cy="12" r="3"/>
          </svg>
          {isSettings && (
            <span>Settings</span>
          )}
        </button>
      </div>

      {!isSettings && (
        <div
          className="border-b border-white/5 px-5 h-9 flex items-center"
          style={{ background: theme.subNav }}
        >
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
      )}
    </div>
  )
}

export default Navbar