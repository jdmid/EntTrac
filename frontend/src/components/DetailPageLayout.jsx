import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from './Navbar'
import NotesDialog from './NotesDialog'
import { statusStyles, statusLabels } from '../theme/themes'

const DEFAULT_STATUS_OPTIONS = [
  { value: 'CONSUMING' },
  { value: 'PLANNED' },
  { value: 'FINISHED' },
  { value: 'DROPPED' },
]

function DetailPageLayout({
  // Navigation
  activeMedia,
  fromSearch,
  backPath,
  title,

  // Data
  item,
  inLibrary,
  communityRating,
  communityRatingLabel,
  score,

  // Display
  theme,
  icon,
  refreshLabel,
  metaLine,
  progressSection,

  // Notes
  notesProgressLabel,

  // Handlers
  onRefresh,
  onRemove,
  onAdd,
  onScoreSave,
  onStatusChange,
  onNotesSave,
}) {
  const navigate = useNavigate()
  const [descExpanded, setDescExpanded] = useState(false)
  const [notesOpen, setNotesOpen] = useState(false)

  const description = item?.description ?? 'No description available.'
  const isLongDesc = description.length > 300

  const statusOptions = DEFAULT_STATUS_OPTIONS.map((opt) => ({
    value: opt.value,
    label: statusLabels[activeMedia]?.[opt.value] ?? opt.value,
  }))

  return (
    <div className="min-h-screen" style={{ background: theme.background }}>
      <Navbar activeMedia={activeMedia} />

      <div className="p-5">

        {/* Breadcrumb + Notes */}
        <div className="flex items-center justify-between mb-5">
          <div className="flex items-center gap-2 text-[12px]">
            <span
              className="cursor-pointer transition-colors"
              style={{ color: theme.accent }}
              onClick={() => navigate(backPath)}
            >
              ← {fromSearch ? 'Search' : 'Library'}
            </span>
            <span style={{ color: '#333344' }}>/</span>
            <span style={{ color: '#777788' }}>{title}</span>
          </div>

          {inLibrary && onNotesSave && (
            <button
              onClick={() => setNotesOpen(true)}
              className="flex items-center gap-1.5 px-3 py-1.5 text-[11px] rounded-lg transition-colors"
              style={{
                background: theme.accentBg,
                border: `0.5px solid ${theme.accentBorder}`,
                color: theme.accent,
              }}
            >
              📝 Notes
            </button>
          )}
        </div>

        <div className="flex flex-col md:flex-row gap-6">

          {/* Left col — cover */}
          <div className="shrink-0 w-full md:w-[180px]">
            <div
              className="w-full rounded-lg overflow-hidden flex items-center justify-center"
              style={{ background: theme.cardCover }}
            >
              {item?.coverUrl ? (
                <img
                  src={item.coverUrl}
                  alt={title}
                  className="w-full object-contain"
                />
              ) : (
                <span style={{ fontSize: 40, color: theme.cardIcon }}>
                  {icon}
                </span>
              )}
            </div>

            {inLibrary && onRefresh && (
              <button
                onClick={onRefresh}
                className="w-full mt-2 py-1.5 text-[11px] rounded transition-colors"
                style={{
                  background: theme.accentBg,
                  border: `0.5px solid ${theme.accentBorder}`,
                  color: theme.accent,
                }}
              >
                {refreshLabel}
              </button>
            )}

            {inLibrary ? (
              <button
                onClick={onRemove}
                className="w-full mt-2 py-1.5 text-[11px] rounded transition-colors"
                style={{
                  background: '#2e1212',
                  border: '0.5px solid #501c1c',
                  color: '#f87171',
                }}
              >
                Remove from library
              </button>
            ) : (
              <button
                onClick={onAdd}
                className="w-full mt-2 py-1.5 text-[11px] rounded transition-colors"
                style={{
                  background: theme.accentBg,
                  border: `0.5px solid ${theme.accentBorder}`,
                  color: theme.accent,
                }}
              >
                + Add to library
              </button>
            )}
          </div>

          {/* Right col */}
          <div className="flex-1 min-w-0">

            {/* Title */}
            <h1 className="text-[22px] font-medium text-[#e2e2f0] m-0 mb-1">
              {title}
            </h1>

            {/* Medium-specific meta */}
            {metaLine}

            {/* Description */}
            <div className="mb-4">
              <p className="text-[11px] text-[#555566] uppercase tracking-[0.05em] mb-1.5">
                Description
              </p>
              <p className="text-[13px] text-[#9a9aaa] m-0 leading-relaxed">
                {isLongDesc && !descExpanded
                  ? description.slice(0, 300) + '…'
                  : description}
              </p>
              {isLongDesc && (
                <span
                  onClick={() => setDescExpanded((v) => !v)}
                  className="text-[12px] cursor-pointer"
                  style={{ color: theme.accent }}
                >
                  {descExpanded ? 'Show less' : 'Read more'}
                </span>
              )}
            </div>

            {/* Score */}
            <div className="mb-4">
              <p className="text-[11px] text-[#555566] uppercase tracking-[0.05em] mb-1.5">
                Score
              </p>
              <div className="flex gap-2">
                {inLibrary && item?.status !== 'PLANNED' && (
                <div className="rounded-lg p-3 text-center w-fit"
                    style={{
                      background: theme.topBar,
                      border: `0.5px solid ${theme.cardBorder}`,
                    }}
                  >
                    <p className="text-[28px] font-medium m-0 mb-0.5"
                      style={{ color: '#fbbf24' }}>
                      {score ?? '—'}
                    </p>
                    <p className="text-[11px] text-[#555566] m-0">Your score</p>
                    <div className="flex justify-center gap-1 mt-2">
                      {[1,2,3,4,5,6,7,8,9,10].map((n) => (
                        <span
                          key={n}
                          onClick={() => onScoreSave(n)}
                          className="cursor-pointer text-[16px] transition-colors"
                          style={{
                            color: score != null && n <= score
                              ? '#fbbf24' : '#333344'
                          }}
                          title={`Score ${n}`}
                        >
                          ★
                        </span>
                      ))}
                    </div>
                  </div>
                )}

                {communityRating != null && (
                  <div
                    className="rounded-lg p-3 text-center w-fit"
                    style={{
                      background: theme.topBar,
                      border: `0.5px solid ${theme.cardBorder}`,
                    }}
                  >
                    <p className="text-[28px] font-medium m-0 mb-0.5"
                      style={{ color: '#fbbf24' }}>
                      {communityRating}
                    </p>
                    <p className="text-[11px] text-[#555566] m-0">
                      {communityRatingLabel}
                    </p>
                    <p className="text-[11px] text-[#333344] m-0 mt-1">
                      Community rating
                    </p>
                  </div>
                )}
              </div>
            </div>

            {/* Progress slot */}
            {inLibrary && progressSection && (
              <div className="mb-4">
                <p className="text-[11px] text-[#555566] uppercase tracking-[0.05em] mb-1.5">
                  Progress
                </p>
                {progressSection}
              </div>
            )}

            {/* Status */}
            {inLibrary && (
              <div className="mb-6">
                <p className="text-[11px] text-[#555566] uppercase tracking-[0.05em] mb-1.5">
                  Status
                </p>
                <div className="flex gap-2 flex-wrap">
                  {statusOptions.map((opt) => (
                    <button
                      key={opt.value}
                      onClick={() => onStatusChange(opt.value)}
                      className="px-3 py-1 text-[11px] rounded-full transition-colors"
                      style={{
                        background: item?.status === opt.value
                          ? statusStyles[opt.value].badge.background
                          : theme.topBar,
                        color: item?.status === opt.value
                          ? statusStyles[opt.value].badge.color
                          : '#555566',
                        border: `0.5px solid ${item?.status === opt.value
                          ? statusStyles[opt.value].border
                          : '#2a2a3a'}`,
                      }}
                    >
                      {opt.label}
                    </button>
                  ))}
                </div>
              </div>
            )}

          </div>
        </div>
      </div>

      {onNotesSave && (
        <NotesDialog
          isOpen={notesOpen}
          onClose={() => setNotesOpen(false)}
          title={title}
          initialNotes={item?.notes}
          currentProgress={item?.chaptersRead ?? item?.episodesWatched}
          progressLabel={notesProgressLabel}
          onSave={onNotesSave}
        />
      )}
    </div>
  )
}

export default DetailPageLayout