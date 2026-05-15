import { useState } from 'react'

const SORT_OPTIONS = [
  { value: 'MOST_UNREAD', label: 'Most unread' },
  { value: 'ALPHA_AZ', label: 'Alphabetical A–Z' },
  { value: 'ALPHA_ZA', label: 'Alphabetical Z–A' },
  { value: 'SCORE_HIGH', label: 'Score (high to low)' },
  { value: 'SCORE_LOW', label: 'Score (low to high)' },
  { value: 'RECENTLY_UPDATED', label: 'Recently updated' },
  { value: 'RECENTLY_ADDED', label: 'Recently added' },
]

function Chip({ label, isActive, accent, onClick }) {
  return (
    <button
      onClick={onClick}
      className="text-[11px] px-2.5 py-[3px] rounded-full cursor-pointer transition-colors whitespace-nowrap"
      style={{
        border: `0.5px solid ${isActive ? accent : '#2a2a3a'}`,
        color: isActive ? accent : '#777788',
        background: isActive ? `${accent}18` : '#16131f',
      }}
    >
      {label}
    </button>
  )
}

function FilterBar({ statusFilters, statusFilter, onStatusChange, seriesStatusFilter, onSeriesStatusChange, sortBy, onSortChange, theme }) {
  const [showMoreFilters, setShowMoreFilters] = useState(false)

  const SERIES_STATUS_FILTERS = [
    { value: 'ALL', label: 'All' },
    { value: 'ONGOING', label: 'Ongoing' },
    { value: 'COMPLETED', label: 'Completed' },
    { value: 'HIATUS', label: 'Hiatus' },
    { value: 'CANCELLED', label: 'Cancelled' },
  ]

  return (
    <div className="mb-4">

      {/* Status filter row */}
      <div className="flex items-center gap-2 mb-2 flex-wrap">
        <span className="text-[11px] text-[#555566] uppercase tracking-[0.05em] w-[70px] shrink-0">
          Status
        </span>

        {statusFilters.map((f) => (
          <Chip
            key={f.value}
            label={f.label}
            isActive={statusFilter === f.value}
            accent={theme.accent}
            onClick={() => onStatusChange(f.value)}
          />
        ))}

        {/* More filters toggle */}
        <button
          onClick={() => setShowMoreFilters((v) => !v)}
          className="text-[11px] text-[#555566] rounded-full px-2.5 py-[3px] cursor-pointer ml-1"
          style={{ background: 'none', border: '0.5px solid #2a2a3a' }}
        >
          More filters{' '}
          <span style={{ color: theme.accent }}>
            {showMoreFilters ? '▴' : '▾'}
          </span>
        </button>
      </div>

      {/* More filters panel */}
      {showMoreFilters && (
        <div
          className="rounded-lg px-3.5 py-2.5 mb-2"
          style={{
            background: theme.topBar,
            border: `0.5px solid ${theme.cardBorder}`,
          }}
        >
          <div className="flex items-center gap-2 flex-wrap">
            <span className="text-[11px] text-[#555566] uppercase tracking-[0.05em] w-[70px] shrink-0">
              Series
            </span>

            {SERIES_STATUS_FILTERS.map((f) => (
                <Chip
                key={f.value}
                label={f.label}
                isActive={seriesStatusFilter === f.value}
                accent={theme.accent}
                onClick={() => onSeriesStatusChange(f.value)}
              />
            ))}
          </div>
        </div>
      )}

      {/* Sort row */}
      <div className="flex items-center gap-2">
        <span className="text-[11px] text-[#555566] uppercase tracking-[0.05em] w-[70px] shrink-0">
          Sort by
        </span>

        <select
          value={sortBy}
          onChange={(e) => onSortChange(e.target.value)}
          className="text-[11px] px-2.5 py-[3px] rounded-full cursor-pointer outline-none"
          style={{
            border: '0.5px solid #2a2a3a',
            color: '#888898',
            background: '#16131f',
            appearance: 'none',
            paddingRight: '20px',
            backgroundImage: `url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='10' height='6' viewBox='0 0 10 6'%3E%3Cpath fill='%23${theme.accent.slice(1)}' d='M0 0l5 6 5-6z'/%3E%3C/svg%3E")`,
            backgroundRepeat: 'no-repeat',
            backgroundPosition: 'right 8px center',
          }}
        >
          {SORT_OPTIONS.map((o) => (
            <option key={o.value} value={o.value} style={{ background: '#16131f' }}>
              {o.label}
            </option>
          ))}
        </select>
      </div>

    </div>
  )
}

export default FilterBar