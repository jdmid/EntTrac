import { useState, useEffect } from 'react'

function NotesDialog({ isOpen, onClose, title, initialNotes, currentProgress, progressLabel, onSave }) {
  const [notes, setNotes] = useState(initialNotes ?? '')
  const [saving, setSaving] = useState(false)
  const [saved, setSaved] = useState(false)

  useEffect(() => {
    if (isOpen) {
      setNotes(initialNotes ?? '')
      setSaved(false)
    }
  }, [isOpen, initialNotes])

  function handleSave() {
    setSaving(true)
    onSave(notes)
      .then(() => {
        setSaving(false)
        setSaved(true)
        setTimeout(() => setSaved(false), 2000)
      })
      .catch(() => setSaving(false))
  }

  function handleBackdropClick(e) {
    if (e.target === e.currentTarget) onClose()
  }

  if (!isOpen) return null

  return (
    <div
      onClick={handleBackdropClick}
      className="fixed inset-0 z-50 flex items-center justify-center"
      style={{ background: 'rgba(0,0,0,0.6)' }}
    >
      <div
        className="w-full max-w-[480px] mx-4 rounded-xl p-6"
        style={{
          background: '#130f1f',
          border: '0.5px solid #2a1f4a',
        }}
      >
        {/* Header */}
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <span style={{ fontSize: 16, color: '#9d7cff' }}>📝</span>
            <span className="text-[15px] font-medium text-[#e2e2f0]">
              Notes
            </span>
          </div>
          <button
            onClick={onClose}
            className="text-[#555566] hover:text-[#e2e2f0] transition-colors"
            style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 4 }}
          >
            ✕
          </button>
        </div>

        {/* Context */}
        <p className="text-[12px] text-[#555566] m-0 mb-3 truncate">
          {title}
          {currentProgress != null && progressLabel && (
            <span> · {progressLabel} {currentProgress}</span>
          )}
        </p>

        {/* Textarea */}
        <textarea
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
          placeholder="Write anything — reactions, characters to remember, things to look up, where you left off..."
          className="w-full text-[13px] text-[#e2e2f0] rounded-lg p-3 outline-none resize-none leading-relaxed"
          style={{
            background: '#0d0b14',
            border: '0.5px solid #2a1f4a',
            minHeight: '160px',
            fontFamily: 'inherit',
          }}
        />

        {/* Footer */}
        <div className="flex items-center justify-between mt-3">
          <span className="text-[11px] text-[#555566]">
            {notes.length > 0 ? `${notes.length} characters` : 'No notes yet'}
          </span>
          <button
            onClick={handleSave}
            disabled={saving}
            className="px-4 py-1.5 text-[12px] font-medium rounded-lg transition-colors"
            style={{
              background: saved ? '#1f4a32' : '#9d7cff',
              color: saved ? '#4ade80' : '#fff',
              border: 'none',
              cursor: saving ? 'not-allowed' : 'pointer',
              opacity: saving ? 0.7 : 1,
            }}
          >
            {saving ? 'Saving…' : saved ? '✓ Saved' : 'Save'}
          </button>
        </div>
      </div>
    </div>
  )
}

export default NotesDialog