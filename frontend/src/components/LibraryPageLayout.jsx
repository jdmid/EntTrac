import { useState, useEffect, useMemo } from 'react'
import Navbar from './Navbar'
import FilterBar from './FilterBar'
import AttributionFooter from './AttributionFooter'
import { themes } from '../theme/themes'

function LibraryPageLayout({
  // Page identity
  activeMedia,
  pageTitle,

  // Data
  library,
  loading,
  error,
  emptyMessage,
  emptyFilterMessage,

  // Filter config — state lives in page
  statusFilters,
  statusFilter,
  onStatusChange,
  seriesStatusFilters,
  seriesStatusFilter,
  onSeriesStatusChange,
  sortOptions,
  sortBy,
  onSortChange,
  sortFn,

  // Refresh — optional
  onRefreshAll,

  // Cards
  renderCard,
}) {
  const theme = themes[activeMedia]

  const [refreshing, setRefreshing] = useState(false)
  const [refreshDone, setRefreshDone] = useState(false)
  const [cooldown, setCooldown] = useState(0)

  async function handleRefreshAll() {
    if (!onRefreshAll || refreshing || cooldown > 0) return

    setRefreshing(true)
    setRefreshDone(false)

    try {
      await onRefreshAll()
      setRefreshDone(true)
      setTimeout(() => {
        setRefreshDone(false)
        setCooldown(60)
        const interval = setInterval(() => {
          setCooldown((prev) => {
            if (prev <= 1) { clearInterval(interval); return 0 }
            return prev - 1
          })
        }, 1000)
      }, 5000)
    } catch (err) {
      console.error(err)
    } finally {
      setRefreshing(false)
    }
  }

  const filtered = useMemo(() => {
    let items = library ?? []

    if (statusFilter !== 'ALL') {
      items = items.filter((item) => item.status === statusFilter)
    }

    if (seriesStatusFilter !== 'ALL') {
      items = items.filter((item) =>
        item.seriesStatus?.toLowerCase() === seriesStatusFilter.toLowerCase()
      )
    }

    return sortFn ? sortFn(items, sortBy) : items
  }, [library, statusFilter, seriesStatusFilter, sortBy, sortFn])

  return (
    <div className="min-h-screen" style={{ background: theme.background }}>
      <Navbar activeMedia={activeMedia} />

      <div className="p-5">
        {/* Header */}
        <div className="flex items-center justify-between mb-3.5">
          <div className="flex items-baseline gap-2.5">
            <h1 className="text-[18px] font-medium text-[#e2e2f0] m-0">
              {pageTitle}
            </h1>
            <p className="text-[13px] text-[#555566] m-0">
              {filtered.length} title{filtered.length !== 1 ? 's' : ''}
              {refreshDone && ' · last refreshed just now'}
            </p>
          </div>

          {onRefreshAll && (
            <div className="flex items-center gap-2">
              {cooldown > 0 && !refreshing && !refreshDone && (
                <span className="text-[11px] text-[#555566]">
                  Available in {cooldown}s
                </span>
              )}
              <button
                onClick={handleRefreshAll}
                disabled={refreshing || cooldown > 0}
                className="flex items-center gap-1.5 px-3 py-[5px] text-[11px] rounded-lg transition-colors"
                style={{
                  background: refreshDone ? `${theme.accent}18` : `${theme.accent}18`,
                  border: `0.5px solid ${refreshDone ? theme.accentBorder : cooldown > 0 ? '#2a2a3a' : `${theme.accent}55`}`,
                  color: refreshDone ? theme.accent : cooldown > 0 ? '#555566' : theme.accent,
                  cursor: refreshing || cooldown > 0 ? 'not-allowed' : 'pointer',
                  opacity: refreshing || cooldown > 0 ? 0.6 : 1,
                }}
              >
                {refreshDone ? '✓ Done' : refreshing ? (
                  <span className="flex items-center gap-1.5">
                    <span
                      className="inline-block w-3 h-3 rounded-full animate-spin"
                      style={{
                        border: `2px solid ${theme.accent}33`,
                        borderTopColor: theme.accent,
                      }}
                    />
                    Refreshing…
                  </span>
                ) : 'Refresh all'}
              </button>
            </div>
          )}
        </div>

        <FilterBar
          statusFilters={statusFilters}
          statusFilter={statusFilter}
          onStatusChange={onStatusChange}
          seriesStatusFilters={seriesStatusFilters}
          seriesStatusFilter={seriesStatusFilter}
          onSeriesStatusChange={onSeriesStatusChange}
          sortOptions={sortOptions}
          sortBy={sortBy}
          onSortChange={onSortChange}
          theme={theme}
        />

        {loading && (
          <div className="text-[13px] text-[#555566] text-center py-12">
            Loading library…
          </div>
        )}

        {!loading && error && (
          <div className="text-[13px] text-[#f87171] text-center py-12">
            {error}
          </div>
        )}

        {!loading && !error && filtered.length === 0 && (
          <div className="text-[13px] text-[#555566] text-center py-12">
            {library?.length === 0 ? emptyMessage : emptyFilterMessage}
          </div>
        )}

        {!loading && !error && filtered.length > 0 && (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3 items-start">
            {filtered.map((item) => renderCard(item))}
          </div>
        )}

        <AttributionFooter />
      </div>
    </div>
  )
}

export default LibraryPageLayout