import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Sparkles, BookMarked, Tv, Film, BookOpen, Gamepad2 } from 'lucide-react'
import client from '../../api/client'
import { useAuth } from '../../context/AuthContext'
import { useSettings } from '../../context/SettingsContext'
import { themes } from '../../theme/themes'
import Wordmark from '../../components/Wordmark'

const ICONS = { anime: Sparkles, manga: BookMarked, tv: Tv, movie: Film, book: BookOpen, game: Gamepad2 }

function ShelfTile({ tab, selected, onToggle }) {
  const Icon = ICONS[tab.id]
  const theme = themes[tab.id]
  return (
    <button
      type="button"
      onClick={onToggle}
      aria-pressed={selected}
      className="flex flex-col items-center justify-end gap-3 rounded-t px-3 pb-4 pt-6 transition-all duration-300"
      style={{
        height: selected ? 160 : 112,
        background: selected ? theme.cardCover : 'rgba(255,255,255,0.03)',
        borderTop: `4px solid ${selected ? theme.accent : 'transparent'}`,
        opacity: selected ? 1 : 0.5,
      }}
    >
      <Icon size={22} color={selected ? theme.accent : '#555580'} />
      <span className="text-xs font-medium tracking-wide" style={{ color: 'rgba(255,255,255,0.9)' }}>
        {tab.label}
      </span>
    </button>
  )
}

export default function OnboardingPreferencesPage() {
  const { user, markOnboarded } = useAuth()
  const { tabs, setTabsAndPersist } = useSettings()
  const navigate = useNavigate()

  const [selected, setSelected] = useState(() =>
    Object.fromEntries(tabs.map((t) => [t.id, t.visible]))
  )
  const [displayName, setDisplayName] = useState(user?.displayName ?? '')
  const [saving, setSaving] = useState(false)

  function toggle(id) {
    setSelected((prev) => {
      const next = { ...prev, [id]: !prev[id] }
      const stillHasOneVisible = Object.values(next).some(Boolean)
      return stillHasOneVisible ? next : prev
    })
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setSaving(true)

    const updatedTabs = tabs.map((t) => ({ ...t, visible: selected[t.id] }))
    setTabsAndPersist(updatedTabs)

    if (displayName.trim()) {
      await client.patch('/auth/profile', { displayName: displayName.trim() })
    }

    await markOnboarded()
    navigate('/', { replace: true })
  }

  return (
    <div className="flex min-h-screen items-center justify-center px-4" style={{ background: themes.brand.background }}>
      <form onSubmit={handleSubmit} className="flex w-full max-w-md flex-col gap-10">
        <div className="flex flex-col items-center gap-2 text-center">
          <Wordmark size={22} />
          <p className="text-[13px]" style={{ color: '#777788' }}>
            Pick what you're tracking. You can change this later in Settings.
          </p>
        </div>

        <label className="flex flex-col gap-1">
          <span className="text-[11px] font-medium uppercase tracking-wide" style={{ color: '#777788' }}>
            Display name
          </span>
          <input
            type="text"
            value={displayName}
            onChange={(e) => setDisplayName(e.target.value)}
            placeholder="What should we call you?"
            className="rounded px-3 py-2 text-sm"
            style={{ background: themes.brand.cardCover, border: `0.5px solid ${themes.brand.cardBorder}`, color: 'rgba(255,255,255,0.9)' }}
          />
        </label>

        <div className="flex items-end justify-center gap-3">
          {tabs.map((tab) => (
            <ShelfTile key={tab.id} tab={tab} selected={selected[tab.id]} onToggle={() => toggle(tab.id)} />
          ))}
        </div>

        <button
          type="submit"
          disabled={saving}
          className="rounded px-4 py-3 text-sm font-medium"
          style={{ background: themes.brand.accent, color: '#ffffff', opacity: saving ? 0.6 : 1 }}
        >
          {saving ? 'Saving…' : 'Finish setup'}
        </button>
      </form>
    </div>
  )
}