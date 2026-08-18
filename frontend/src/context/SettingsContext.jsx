import { createContext, useContext, useEffect, useState } from 'react'
import { arrayMove } from '@dnd-kit/sortable'
import client from '../api/client'
import { useAuth } from './AuthContext'

const DEFAULT_TABS = [
  { id: 'manga',  label: 'Manga',  visible: true },
  { id: 'tv',     label: 'TV',     visible: true },
  { id: 'movie',  label: 'Movies', visible: true },
  { id: 'book',   label: 'Books',  visible: true },
  { id: 'game', label: 'Games', visible: true },
]

function mergeWithDefaults(stored) {
  const storedIds = new Set(stored.map((t) => t.id))
  const merged = DEFAULT_TABS.map((defaultTab) =>
    storedIds.has(defaultTab.id) ? stored.find((t) => t.id === defaultTab.id) : defaultTab
  )
  const storedOrder = stored
    .filter((t) => merged.find((m) => m.id === t.id))
    .map((t) => merged.find((m) => m.id === t.id))
  const newTabs = merged.filter((t) => !storedIds.has(t.id))
  return [...storedOrder, ...newTabs]
}

async function persistTabs(tabs) {
  try {
    await client.put('/settings', tabs)
  } catch {
    // best-effort — the UI already reflects the change locally
  }
}

const SettingsContext = createContext(null)

export function SettingsProvider({ children }) {
  const { status } = useAuth()
  const [tabs, setTabs] = useState(DEFAULT_TABS)

  useEffect(() => {
    if (status !== 'authenticated') return
    client.get('/settings')
      .then((res) => setTabs(mergeWithDefaults(res.data)))
      .catch(() => setTabs(DEFAULT_TABS)) // no saved settings yet
  }, [status])

  function reorderTabs(activeId, overId) {
    setTabs((prev) => {
      const oldIndex = prev.findIndex((t) => t.id === activeId)
      const newIndex = prev.findIndex((t) => t.id === overId)
      const reordered = arrayMove(prev, oldIndex, newIndex)
      persistTabs(reordered)
      return reordered
    })
  }

  function toggleTab(id) {
    setTabs((prev) => {
      const visibleCount = prev.filter((t) => t.visible).length
      const target = prev.find((t) => t.id === id)
      if (target?.visible && visibleCount === 1) return prev
      const updated = prev.map((t) => (t.id === id ? { ...t, visible: !t.visible } : t))
      persistTabs(updated)
      return updated
    })
  }

  function setTabsAndPersist(newTabs) {
    setTabs(newTabs)
    persistTabs(newTabs)
  }

  function firstVisibleTab() {
    return tabs.find((t) => t.visible)?.id ?? 'manga'
  }

  return (
    <SettingsContext.Provider value={{ tabs, reorderTabs, toggleTab, setTabsAndPersist, firstVisibleTab }}>
      {children}
    </SettingsContext.Provider>
  )
}

export function useSettings() {
  const ctx = useContext(SettingsContext)
  if (!ctx) throw new Error('useSettings must be used within SettingsProvider')
  return ctx
}