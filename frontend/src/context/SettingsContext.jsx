import { createContext, useContext, useState } from 'react'
import { arrayMove } from '@dnd-kit/sortable'

const DEFAULT_TABS = [
  { id: 'manga',  label: 'Manga',  visible: true },
  { id: 'anime',  label: 'Anime',  visible: true },
  { id: 'tv',     label: 'TV',     visible: true },
  { id: 'movie',  label: 'Movies', visible: true },
  { id: 'book',   label: 'Books',  visible: true },
]

const STORAGE_KEY = 'enttrac-settings'

function loadFromStorage() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return DEFAULT_TABS
    const parsed = JSON.parse(raw)
    // Guard against stale storage missing tabs added in future releases
    const storedIds = new Set(parsed.map((t) => t.id))
    const merged = DEFAULT_TABS.map((defaultTab) => {
      if (storedIds.has(defaultTab.id)) {
        return parsed.find((t) => t.id === defaultTab.id)
      }
      return defaultTab
    })
    // Preserve stored order for tabs that exist in both
    const storedOrder = parsed
      .filter((t) => merged.find((m) => m.id === t.id))
      .map((t) => merged.find((m) => m.id === t.id))
    const newTabs = merged.filter((t) => !storedIds.has(t.id))
    return [...storedOrder, ...newTabs]
  } catch {
    return DEFAULT_TABS
  }
}

function saveToStorage(tabs) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(tabs))
  } catch {
    // localStorage unavailable — silently continue
  }
}

const SettingsContext = createContext(null)

export function SettingsProvider({ children }) {
  const [tabs, setTabs] = useState(loadFromStorage)

  function reorderTabs(activeId, overId) {
    setTabs((prev) => {
      const oldIndex = prev.findIndex((t) => t.id === activeId)
      const newIndex = prev.findIndex((t) => t.id === overId)
      const reordered = arrayMove(prev, oldIndex, newIndex)
      saveToStorage(reordered)
      return reordered
    })
  }

  function toggleTab(id) {
    setTabs((prev) => {
      const visibleCount = prev.filter((t) => t.visible).length
      const target = prev.find((t) => t.id === id)
      // Prevent hiding the last visible tab
      if (target?.visible && visibleCount === 1) return prev
      const updated = prev.map((t) =>
        t.id === id ? { ...t, visible: !t.visible } : t
      )
      saveToStorage(updated)
      return updated
    })
  }

  function firstVisibleTab() {
    return tabs.find((t) => t.visible)?.id ?? 'manga'
  }

  return (
    <SettingsContext.Provider value={{ tabs, reorderTabs, toggleTab, firstVisibleTab }}>
      {children}
    </SettingsContext.Provider>
  )
}

export function useSettings() {
  const ctx = useContext(SettingsContext)
  if (!ctx) throw new Error('useSettings must be used within SettingsProvider')
  return ctx
}